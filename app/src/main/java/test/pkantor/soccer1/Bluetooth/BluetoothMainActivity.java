package test.pkantor.soccer1.Bluetooth;

/**
 * Created by Pawel on 27.11.2017.
 */

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.UUID;

import test.pkantor.soccer1.Game;
import test.pkantor.soccer1.GlobalSocket;
import test.pkantor.soccer1.R;


public class BluetoothMainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String TAG = "MainActivity";

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private String mConnectedDeviceName = null;

    BluetoothAdapter mBluetoothAdapter;
    Button btnEnableDisable_Discoverable;
    StringBuffer mOutStringBuffer;

    BluetoothConnectionService mBluetoothConnectionService = null;

    Button btnStartConnection;
    Button btnSend;
    Button bFindUnpairedDevices;
//    Button btnScan;

    EditText etSend;
    EditText mInEditText;
    EditText mOutEditText;

    AlertDialog dialog;
    Resources res;
    boolean imFirstPlayer = false;
    boolean imReady = false;
    boolean secondPlayerReady = false;
    ProgressDialog mProgressDialog;
    String player1Name;
    String player2Name;
    String goalPoints;

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    BluetoothDevice mBTDevice;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public ArrayAdapter<String> mBTDevicesAdapter = null;

    public DeviceListAdapter mDeviceListAdapter;

    ListView lvNewDevices;

    @Override
    public void onStart()
    {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 3);
        }
        else if (mBluetoothConnectionService == null)
            setupConnectionService();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mBluetoothConnectionService != null)
            mBluetoothConnectionService.stop();
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
    }

    private void setupConnectionService()
    {
        lvNewDevices.setAdapter(mBTDevicesAdapter);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v != null)
                {
                    String message = mInEditText.getText().toString();
                    sendMessage(message);
                }
            }
        });

        mBluetoothConnectionService = new BluetoothConnectionService(getApplicationContext(), mHandler);

        mOutStringBuffer = new StringBuffer("");

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

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
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
                                showSetNames();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data);
                }
                break;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_conn);
        Button btnONOFF = (Button) findViewById(R.id.btnONOFF);
        btnEnableDisable_Discoverable = (Button) findViewById(R.id.btnDiscoverable_on_off);
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();
        mBTDevicesAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.device_name);
        btnStartConnection = (Button) findViewById(R.id.btnStartConnection);
        btnSend = (Button) findViewById(R.id.btnSend);
        etSend = (EditText) findViewById(R.id.etIn);
        res = getResources();

        mInEditText = (EditText) findViewById(R.id.etIn);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // imFirstPlayer = true;
                Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
            }
        });

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

    }

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

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
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

    public void btnDiscover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            //IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            //registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            //IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            //registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its very memory intensive.
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
//        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){ // JELLY_BEAN_MR2
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(i).createBond();

            mBTDevice = mBTDevices.get(i);
            mBluetoothConnectionService = new BluetoothConnectionService(BluetoothMainActivity.this, mHandler);
//        }
    }

    public void showSetNames()
    {
        dialog = null;
        final Intent intent = new Intent();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                                if (src.toString().matches("[a-zA-Z]+"))
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

                                if (src.toString().matches("[a-zA-Z]+"))
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

                mProgressDialog.show(BluetoothMainActivity.this, res.getString(R.string.waiting_for_other_player), res.getString(R.string.please_wait), true);

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
            ((EditText) mView.findViewById(R.id.etFirstPlayerName)).setText(res.getString(R.string.EnterYourName));
            p2Name.setVisibility(View.GONE);
        }
        else
        {
            mView.findViewById(R.id.tvFirstPlayerName).setVisibility(View.GONE);
            p1Name.setVisibility(View.GONE);
            mView.findViewById(R.id.tvGoalPoints).setVisibility(View.GONE);
            ((EditText) mView.findViewById(R.id.etSecondPlayerName)).setText(res.getString(R.string.EnterYourName));
            np.setVisibility(View.GONE);
        }

        dialog.show();
    }
}
