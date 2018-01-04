package com.pawel.kantor.praca.inzynierska.Bluetooth;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.pawel.kantor.praca.inzynierska.Game;
import com.pawel.kantor.praca.inzynierska.GlobalSocket;

import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import com.pawel.kantor.praca.inzynierska.R;

/**
 * Created by Pawel on 20.11.2017.
 */

public class BluetoothMainActivity extends Activity {

    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothAdapter mBtAdapter;

    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("01186fa9-b3ed-455c-aea0-cb44a655ed1f");
    private static final int REQUEST_ENABLE_BT = 3;


    BluetoothAdapter mBluetoothAdapter;
    BluetoothConnectionService mBluetoothConnectionService = null;
    StringBuffer mOutStringBuffer;
    private String mConnectedDeviceName = null;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public ArrayAdapter<String> mBTDevicesAdapter = null;
    ProgressDialog mProgressDialog;
    String player1Name;
    String player2Name;
    String goalPoints;
    AlertDialog dialog;
    Resources res;
    boolean imFirstPlayer = false;
    boolean imReady = false;
    boolean secondPlayerReady = false;
    Button btnEnableDisable_Discoverable;
    GlobalSocket gSocket;
    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter<String> pairedDevicesArrayAdapter;
    SharedPreferences sharedPreferences;

    @Override
    public void onBackPressed()
    {
        System.exit(0);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else if (mBluetoothConnectionService == null)
            setupConnectionService();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBluetoothConnectionService != null) {
            if (mBluetoothConnectionService.getState() == BluetoothConnectionService.STATE_NONE) {
                mBluetoothConnectionService.start();
            }
        }
        else
            setupConnectionService();

        if ((mBluetoothAdapter != null) && (mBluetoothAdapter.isEnabled()))
        {
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                pairedDevicesArrayAdapter.clear();
                findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
                for (BluetoothDevice device : pairedDevices) {
                    pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
            if (mBluetoothConnectionService == null)
                setupConnectionService();
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_list);

        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preferences_soccer), Context.MODE_PRIVATE);

        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
            }
        });

        pairedDevicesArrayAdapter = new ArrayAdapter<String>(BluetoothMainActivity.this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(BluetoothMainActivity.this, R.layout.device_name);

        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        pairedDevices = mBtAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDevicesArrayAdapter.add(noDevices);
        }

        res = getResources();
        btnEnableDisable_Discoverable = (Button) findViewById(R.id.btnDiscoverable_on_off);
        mBTDevices = new ArrayList<>();
        mBTDevicesAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.device_name);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btnEnableDisable_Discoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ensureDiscoverable();
            }
        });

        if (mBluetoothAdapter == null)
        {
            this.finish();
        }

        gSocket = (GlobalSocket) getApplicationContext();
        dialog = null;
    }

    private void connectDevice(Intent data) {
        String address = data.getExtras()
                .getString(BluetoothMainActivity.EXTRA_DEVICE_ADDRESS);

        imFirstPlayer = true;
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        mBluetoothConnectionService.connect(device, MY_UUID_INSECURE);
    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        if (dialog != null)
            dialog.dismiss();
        this.unregisterReceiver(mReceiver);
    }


    private void setupConnectionService()
    {
        mBluetoothConnectionService = new BluetoothConnectionService(getApplicationContext(), mHandler);
        gSocket.setBluetoothHandler(mHandler);

        mOutStringBuffer = new StringBuffer("");
    }

    private void doDiscovery() {
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        mBtAdapter.startDiscovery();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            mBtAdapter.cancelDiscovery();

            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            Toast.makeText(getApplicationContext(), getString(R.string.connecting, info.substring(0, (info.length() - 18))), Toast.LENGTH_LONG).show();
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            connectDevice(intent);
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

    private void setStatus(int resId) {
        if (null == getApplicationContext()) {
            return;
        }
        final ActionBar actionBar = this.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    private void setStatus(CharSequence subTitle) {
        if (null == getApplicationContext()) {
            return;
        }
        final ActionBar actionBar = this.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    public void showSetNames(Context context)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View mView = getLayoutInflater().inflate(R.layout.activity_dialog_setnames, null);
        final EditText p1Name = (EditText) mView.findViewById(R.id.etFirstPlayerName);
        final EditText p2Name = (EditText) mView.findViewById(R.id.etSecondPlayerName);
        Button saveNames = (Button) mView.findViewById(R.id.bSaveNames);

        TextView tvP1Name = (TextView) mView.findViewById(R.id.tvFirstPlayerName);
        TextView tvP2Name = (TextView) mView.findViewById(R.id.tvSecondPlayerName);

        tvP1Name.setText(R.string.enter_your_name);
        tvP2Name.setText(R.string.enter_your_name);

        final NumberPicker np = (NumberPicker) mView.findViewById(R.id.np);
        np.setMinValue(1);
        np.setMaxValue(9);
        np.setWrapSelectorWheel(true);

        if (imFirstPlayer)
        {
            mView.findViewById(R.id.tvSecondPlayerName).setVisibility(View.GONE);
            ((EditText) mView.findViewById(R.id.etFirstPlayerName)).setText(sharedPreferences.getString(getString(R.string.SP_player_name), getString(R.string.default_fplayer)));
            p2Name.setVisibility(View.GONE);
        }
        else
        {
            mView.findViewById(R.id.tvFirstPlayerName).setVisibility(View.GONE);
            p1Name.setVisibility(View.GONE);
            mView.findViewById(R.id.tvGoalPoints).setVisibility(View.GONE);
            ((EditText) mView.findViewById(R.id.etSecondPlayerName)).setText(sharedPreferences.getString(getString(R.string.SP_player_name), getString(R.string.default_splayer)));
            np.setVisibility(View.GONE);
        }

        p1Name.setFilters(new InputFilter[]
                {
                        new InputFilter() {
                            public CharSequence filter(CharSequence src, int start, int end, Spanned dst, int dstart, int dend)
                            {
                                if (src.toString().matches(getString(R.string.allowed_words)))
                                    return src;
                                return "";
                            }
                        },
                        new InputFilter.LengthFilter(9)
                });

        p2Name.setFilters(new InputFilter[]
                {
                        new InputFilter() {
                            public CharSequence filter(CharSequence src, int start, int end, Spanned dst, int dstart, int dend)
                            {

                                if (src.toString().matches(getString(R.string.allowed_words)))
                                    return src;
                                return "";
                            }
                        },
                        new InputFilter.LengthFilter(9)
                });

        saveNames.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();

                mProgressDialog = ProgressDialog.show(BluetoothMainActivity.this, res.getString(R.string.waiting_for_other_player), res.getString(R.string.please_wait), true);

                imReady = true;

                if (imFirstPlayer)
                {
                    player1Name = p1Name.getText().toString();
                    if (player1Name.equals(""))
                        player1Name = res.getString(R.string.default_fplayer);

                    goalPoints = String.valueOf(np.getValue());
                }
                else
                {
                    player2Name = p2Name.getText().toString();
                    if (player2Name.equals(""))
                        player2Name = res.getString(R.string.default_splayer);
                }

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String message;
                        if (imFirstPlayer)
                            message = player1Name + ":" + goalPoints;
                        else
                            message = player2Name;

                        message += ":ready";
                        sendMessage(message);
                    }
                }, 1000);


                if ((imReady) && (secondPlayerReady))
                    startGame();

            }
        });

        builder.setView(mView);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mBluetoothConnectionService.stop();
                mBluetoothConnectionService.start();
            }
        });

        dialog.show();

    }

    public void startGame()
    {
        Intent intent = new Intent(this, Game.class);

        if (mProgressDialog != null)
            mProgressDialog.dismiss();

        GlobalSocket globalSocket = (GlobalSocket) getApplicationContext();
        globalSocket.setBluetoothConnectionService(mBluetoothConnectionService);
        globalSocket.setBluetoothHandler(mBluetoothConnectionService.getHandler());


        if (imFirstPlayer)
            intent.putExtra("amIFirst", imFirstPlayer);

        intent.putExtra("p1Name", player1Name);
        intent.putExtra("p2Name", player2Name);
        intent.putExtra("goalPoints", Integer.valueOf(goalPoints));
        intent.putExtra("gameMode", Game.BLUETOOTH);
        startActivity(intent);
    }

    private void sendMessage(String message) {
        if (mBluetoothConnectionService.getState() != BluetoothConnectionService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            mBluetoothConnectionService.write(send);

            mOutStringBuffer.setLength(0);
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    switch (msg.arg1) {
                        case BluetoothConnectionService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mBTDevicesAdapter.clear();
                            if (mBluetoothConnectionService.mState == 3)
                                showSetNames(BluetoothMainActivity.this);
                            else
                                Toast.makeText(getApplicationContext(), "Zly stan " + mBluetoothConnectionService.mState, Toast.LENGTH_LONG).show();

                            break;
                        case BluetoothConnectionService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothConnectionService.STATE_LISTEN:
                            break;
                        case BluetoothConnectionService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            imFirstPlayer = false;
                            break;
                    }
                    break;
                case 3:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    mBTDevicesAdapter.add("Me:  " + writeMessage);
                    break;
                case 2:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mBTDevicesAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    checkIfPlayerReady(readMessage);
                    break;
                case 4:
                    mConnectedDeviceName = msg.getData().getString("device_name");
                    if (null != getApplicationContext()) {
                        Toast.makeText(getApplicationContext(), "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 5:
                    if (null != getApplicationContext()) {
                        if (msg.getData().getInt("toast") == 0)
                            Toast.makeText(getApplicationContext(), getString(R.string.cannot_connect_to_device_error),
                                    Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getApplicationContext(), getString(R.string.connection_lost),
                                    Toast.LENGTH_SHORT).show();

                        if (!gSocket.getAmIConnected())
                        {
                            if ((dialog != null) && (dialog.isShowing()))
                                dialog.dismiss();
                            if ((mProgressDialog != null) && (mProgressDialog.isShowing()))
                                mProgressDialog.dismiss();

                            imFirstPlayer = false;
                            imReady = false;

                        }
                    }
                    break;
            }
        }
    };

    public void checkIfPlayerReady(String msg)
    {
        StringTokenizer tokenizer = new StringTokenizer(msg, ":");

        if (imFirstPlayer)
            player2Name = tokenizer.nextToken();
        else
        {
            player1Name = tokenizer.nextToken();
            goalPoints = tokenizer.nextToken();
        }

        if (tokenizer.nextToken().equals("ready"))
            secondPlayerReady = true;

        if ((imReady) && (secondPlayerReady))
            startGame();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    setupConnectionService();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    this.finish();
                }
        }
    }
}
