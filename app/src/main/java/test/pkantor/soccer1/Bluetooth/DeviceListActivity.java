package test.pkantor.soccer1.Bluetooth;

/**
 * Created by Pawel on 20.11.2017.
 */

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import test.pkantor.soccer1.DialogExit;
import test.pkantor.soccer1.Game;
import test.pkantor.soccer1.GlobalSocket;
import test.pkantor.soccer1.Menu;
import test.pkantor.soccer1.R;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class DeviceListActivity extends Activity {

    /**
     * Tag for Log
     */
    private static final String TAG = "DeviceListActivity";

    /**
     * Return Intent extra
     */
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    /**
     * Member fields
     */
    private BluetoothAdapter mBtAdapter;

    /**
     * Newly discovered devices
     */
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private static final int REQUEST_ENABLE_BT = 3;


    BluetoothAdapter mBluetoothAdapter;
    BluetoothConnectionService mBluetoothConnectionService = null;
    StringBuffer mOutStringBuffer;
    private String mConnectedDeviceName = null;
    BluetoothDevice mBTDevice;
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
        if (mBluetoothConnectionService != null)
            mBluetoothConnectionService.stop();
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
//        imFirstPlayer = false;
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothConnectionService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothConnectionService.getState() == BluetoothConnectionService.STATE_NONE) {
                // Start the Bluetooth chat services
                mBluetoothConnectionService.start();
            }
        }

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

        // Setup the window
//        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_list);

        // Set result CANCELED in case the user backs out
        //setResult(Activity.RESULT_CANCELED);
        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preferences_soccer), Context.MODE_PRIVATE);


        // Initialize the button to perform device discovery
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
//                v.setVisibility(View.GONE);
            }
        });

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        pairedDevicesArrayAdapter = new ArrayAdapter<String>(DeviceListActivity.this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(DeviceListActivity.this, R.layout.device_name);

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
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
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        imFirstPlayer = true;
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        // Attempt to connect to the device
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

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        if (dialog != null)
            dialog.dismiss();
        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }


    private void setupConnectionService()
    {
        mBluetoothConnectionService = new BluetoothConnectionService(getApplicationContext(), mHandler);

        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        // Turn on sub-title for new devices
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    /**
     * The on-click listener for all devices in the ListViews
     */
    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            connectDevice(intent);
//            intent.putExtra("firstPlayer", true);

            // Set result and finish this Activity
            //setResult(Activity.RESULT_OK, intent);
            //finish();
        }
    };

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                // When discovery is finished, change the Activity title
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


        tvP1Name.setText(R.string.EnterYourName);
        tvP2Name.setText(R.string.EnterYourName);

        final NumberPicker np = (NumberPicker) mView.findViewById(R.id.np);
        np.setMinValue(1);
        np.setMaxValue(9);
        np.setWrapSelectorWheel(true);

        p1Name.setFilters(new InputFilter[]
                {
                        new InputFilter() {
                            public CharSequence filter(CharSequence src, int start, int end, Spanned dst, int dstart, int dend)
                            {
                                if (src.toString().matches(getString(R.string.allowedWords)))
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

                                if (src.toString().matches(getString(R.string.allowedWords)))
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

                mProgressDialog = ProgressDialog.show(DeviceListActivity.this, res.getString(R.string.waiting_for_other_player), res.getString(R.string.please_wait), true);

                imReady = true;

                if (imFirstPlayer)
                {
//                    if (p1Name.getText().length() != 0)
//                        player1Name = res.getString(R.string.pl_DefaultPlayer1);
//                    else
                    player1Name = p1Name.getText().toString();
                    if (player1Name.equals(""))
                        player1Name = res.getString(R.string.DefaultPlayer1);

                    goalPoints = String.valueOf(np.getValue());
                }
                else
                {
                    player2Name = p2Name.getText().toString();
                    if (player2Name.equals(""))
                        player2Name = res.getString(R.string.DefaultPlayer2);
                }

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("handler", "sending message");
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
//                    Toast.makeText(GameModes.this, "blbelel", Toast.LENGTH_SHORT).show()
        });

        builder.setView(mView);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        if (imFirstPlayer)
        {
            mView.findViewById(R.id.tvSecondPlayerName).setVisibility(View.GONE);
            ((EditText) mView.findViewById(R.id.etFirstPlayerName)).setText(sharedPreferences.getString(getString(R.string.SPplayerName), getString(R.string.DefaultPlayer1)));
            p2Name.setVisibility(View.GONE);
        }
        else
        {
            mView.findViewById(R.id.tvFirstPlayerName).setVisibility(View.GONE);
            p1Name.setVisibility(View.GONE);
            mView.findViewById(R.id.tvGoalPoints).setVisibility(View.GONE);
            ((EditText) mView.findViewById(R.id.etSecondPlayerName)).setText(sharedPreferences.getString(getString(R.string.SPplayerName), getString(R.string.DefaultPlayer2)));
            np.setVisibility(View.GONE);
        }

        dialog.show();

    }

    public void startGame()
    {
        Intent intent = new Intent(this, Game.class);

        if (mProgressDialog != null)
            mProgressDialog.dismiss();

        GlobalSocket gSocket = (GlobalSocket) getApplicationContext();
        gSocket.setBluetoothConnectionService(mBluetoothConnectionService);
        gSocket.setBluetoothHandler(mBluetoothConnectionService.getHandler());


        if (imFirstPlayer)
            intent.putExtra("amIFirst", imFirstPlayer);

        intent.putExtra("p1Name", player1Name);
        intent.putExtra("p2Name", player2Name);
        intent.putExtra("goalPoints", Integer.valueOf(goalPoints));
        intent.putExtra("gameMode", Game.BLUETOOTH);
        startActivity(intent);
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mBluetoothConnectionService.getState() != BluetoothConnectionService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBluetoothConnectionService.write(send);

            // Reset out string buffer to zero and clear the edit text field
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
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName)); // TODO connected
                            mBTDevicesAdapter.clear();
                            if (mBluetoothConnectionService.mState == 3)
                                showSetNames(DeviceListActivity.this);
                            else
                                Toast.makeText(getApplicationContext(), "cos sie popsulo", Toast.LENGTH_LONG).show();

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
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mBTDevicesAdapter.add("Me:  " + writeMessage);
                    break;
                case 2:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mBTDevicesAdapter.add(mConnectedDeviceName + ":  " + readMessage);

                    checkIfPlayerReady(readMessage);
                    break;
                case 4:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString("device_name");
                    if (null != getApplicationContext()) {
                        Toast.makeText(getApplicationContext(), "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 5:
                    if (null != getApplicationContext()) {
                        Toast.makeText(getApplicationContext(), msg.getData().getString("toast"),
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
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupConnectionService();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getApplicationContext(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    this.finish();
                }
        }
    }
}
