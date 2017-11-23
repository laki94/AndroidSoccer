package test.pkantor.soccer1;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothService extends AppCompatActivity {


    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    ListView listView;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private BluetoothDevice mDevice;
    private UUID mDeviceUUID;
    ProgressDialog mProgressDialog;
    private ConnectedThread mConnectedThread;
    Context mContext;
    private static String appName = "Soccer1";
    private static final UUID UUID_INS = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private int mState;
    private int mNewState;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    public BluetoothService(Context context)
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mContext = context;
        mNewState = mState;
        start(); // TODO tego ni ma
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connection);

        //bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //start();
       // listView = (ListView) findViewById(R.id.lvPaired);

       // turnOnBluetooth();
        //showPaired();
//        if (bluetoothAdapter.is)
    }

    public void clickShowPaired(View v)
    {
        showPaired();
    }

    public void turnOnBluetooth()
    {
        if(!mBluetoothAdapter.isEnabled())
        {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Wlaczono Bluetooth", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth już jest włączone", Toast.LENGTH_SHORT).show();
    }

    public void turnOffBluetooth(View v)
    {
        mBluetoothAdapter.disable();
        Toast.makeText(getApplicationContext(), "Wyłączono Bluetooth", Toast.LENGTH_SHORT).show();
    }

    public void beVisible(View v)
    {
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }

    public void showPaired()
    {
        pairedDevices = mBluetoothAdapter.getBondedDevices();

        ArrayList list = new ArrayList();

        for(BluetoothDevice device : pairedDevices)
            list.add(device.getName() + "\n" + device.getAddress());

        final ArrayAdapter adapter = new ArrayAdapter(this, R.layout.listview_bt_paired, list);

        listView.setAdapter(adapter);
    }

    public synchronized void stop() {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        mState = STATE_NONE;
    }

    private class AcceptThread extends  Thread
    {
        private final BluetoothServerSocket mmServerSocket;
        private String mmSocketType;

        public AcceptThread()
        {
            BluetoothServerSocket tmp = null;
            mmSocketType = "Insecure";

            try
            {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, UUID_INS);
                Log.d("AcceptThread:", "Setting up srv using: " + UUID_INS );
            } catch(IOException e)
            {

            }
            mmServerSocket = tmp;
            mState = STATE_LISTEN;
        }

        public void run()
        {
            BluetoothSocket socket = null;

            while(mState != STATE_CONNECTED)
            {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
//                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
//            try {
//                Log.d("run:", "acceptThread running");
//
//                socket = mServerSocket.accept();
//
//                Log.d("run:", "rfcom server start");
//            }catch(IOException e)
//            {
//                Log.d("RunExc:", e.getMessage());
//            }

//            if (socket != null)
//                connected(socket, mDevice);

            Log.i("End", "acceptThread");
        }

        public void cancel()
        {
            try{
                mmServerSocket.close();
            }catch (IOException e)
            {
                Log.e("cancelExc", e.getMessage());
            }
        }
    }

    private class ConnectThread extends Thread
    {
        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device, UUID uuid)
        {
            BluetoothSocket tmp = null;
            mmDevice = device;
            mDeviceUUID = uuid;

            try
            {
                tmp = device.createInsecureRfcommSocketToServiceRecord(UUID_INS);
            }catch(IOException e)
            {
                Log.e("Exc", e.getMessage());
            }

            mmSocket = tmp;
            mState = STATE_CONNECTING;
        }

        public void run()
        {
            mBluetoothAdapter.cancelDiscovery();

            try
            {
                mmSocket.connect();
            }catch (IOException e)
            {
                try
                {
                    mmSocket.close();
                }catch (IOException e2)
                {
                    Log.d("exc", "Unable to close " + e.getMessage());
                }
                connectionFailed();
                return;
            }

            synchronized (BluetoothService.this)
            {
                mConnectThread = null;
            }

            connected(mmSocket, mmDevice);

//            BluetoothSocket tmp = null;
//
//            try {
//                tmp = mDevice.createRfcommSocketToServiceRecord(deviceUUID);
//            } catch(IOException e)
//            {
//                Log.e("ConnectExc", e.getMessage());
//            }
//            mSocket = tmp;
//
//            bluetoothAdapter.cancelDiscovery();
//
//            try
//            {
//                mSocket.connect();
//            }catch (IOException e)
//            {
//                try {
//                    mSocket.close();
//                    Log.d("socketExc", e.getMessage());
//                }catch (IOException ex)
//                {
//                    Log.e("exc", ex.getMessage());
//                }
//            }
//
//            connected(mSocket, mDevice);

        }

        public void cancel()
        {
            try
            {
                mmSocket.close();
            }catch (IOException e)
            {
                Log.e("exc", e.getMessage());
            }
        }
    }

    public synchronized void start()
    {
        if (mConnectThread != null)
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null)
        {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread == null)
        {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device, UUID uuid)
    {
        mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth", "Please wait...", true);

        if (mState == STATE_CONNECTING)
        {
            if (mConnectThread != null)
            {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread != null)
        {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInputStream;
        private final OutputStream mmOutputStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try{
                mProgressDialog.dismiss();
            }catch (NullPointerException e)
            {
                e.printStackTrace();
            }


            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            }catch (IOException e)
            {
                Log.e("exc", e.getMessage());
            }
            mmInputStream = tmpIn;
            mmOutputStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        public void run(){
            byte[] buffer = new byte[1024];

            int bytes;

            while(mState == STATE_CONNECTED)
            {
                try{
                bytes = mmInputStream.read(buffer);
                String incomingMessage = new String(buffer, 0, bytes);
                Log.d("IncMsg", incomingMessage);
                }catch (IOException e)
                {
                    Log.e("exc", e.getMessage());
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] bytes)
        {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d("OutMsg", text);
            try{
                mmOutputStream.write(bytes);
            }catch (IOException e)
            {
                Log.e("exc", e.getMessage());
            }
        }

        public void cancel()
        {
            try{
                mmSocket.close();
            }catch (IOException e)
            {
                Log.e("exc", e.getMessage());
            }
        }
    }

    private void connected(BluetoothSocket socket, BluetoothDevice device)
    {
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }

    public void write(byte[] out)
    {
        ConnectedThread tmp;

        mConnectedThread.write(out);
    }

    private void connectionFailed()
    {
//        Toast.makeText(this, "Unable to connect device", Toast.LENGTH_LONG).show();
        mState = STATE_NONE;

        BluetoothService.this.start();
    }

    private void connectionLost()
    {
       // Toast.makeText(this, "Connection lost", Toast.LENGTH_LONG).show();
        mState = STATE_NONE;

        BluetoothService.this.start();
    }

}
