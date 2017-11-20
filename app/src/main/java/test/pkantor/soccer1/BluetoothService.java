package test.pkantor.soccer1;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
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


    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    ListView listView;
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private BluetoothDevice mDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;
    private ConnectedThread connectedThread;

    private static String appName = "Soccer1";
    private static final UUID UUID_INS = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connection);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        listView = (ListView) findViewById(R.id.lvPaired);

        turnOnBluetooth();
        //showPaired();
//        if (bluetoothAdapter.is)
    }

    public void clickShowPaired(View v)
    {
        showPaired();
    }

    public void turnOnBluetooth()
    {
        if(!bluetoothAdapter.isEnabled())
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
        bluetoothAdapter.disable();
        Toast.makeText(getApplicationContext(), "Wyłączono Bluetooth", Toast.LENGTH_SHORT).show();
    }

    public void beVisible(View v)
    {
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }

    public void showPaired()
    {
        pairedDevices = bluetoothAdapter.getBondedDevices();

        ArrayList list = new ArrayList();

        for(BluetoothDevice device : pairedDevices)
            list.add(device.getName() + "\n" + device.getAddress());

        final ArrayAdapter adapter = new ArrayAdapter(this, R.layout.listview_bt_paired, list);

        listView.setAdapter(adapter);
    }

    private class AcceptThread extends  Thread
    {
        private final BluetoothServerSocket mServerSocket;

        public AcceptThread()
        {
            BluetoothServerSocket tmp = null;

            try
            {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, UUID_INS);
                Log.d("AcceptThread:", "Setting up srv using: " + UUID_INS );
            } catch(IOException e)
            {

            }

            mServerSocket = tmp;
        }

        public void run()
        {
            BluetoothSocket socket = null;

            try {


                Log.d("run:", "acceptThread running");

                socket = mServerSocket.accept();

                Log.d("run:", "rfcom server start");
            }catch(IOException e)
            {
                Log.d("RunExc:", e.getMessage());
            }

            // TODO later
            if (socket != null)
                connected(socket, mDevice);

            Log.i("End", "acceptThread");
        }

        public void cancel()
        {
            try{
                mServerSocket.close();
            }catch (IOException e)
            {
                Log.e("cancelExc", e.getMessage());
            }
        }
    }

    private class ConnectThread extends Thread
    {
        private BluetoothSocket mSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid)
        {
            mDevice = device;
            deviceUUID = uuid;
        }

        public void run()
        {
            BluetoothSocket tmp = null;

            try {
                tmp = mDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch(IOException e)
            {
                Log.e("ConnectExc", e.getMessage());
            }
            mSocket = tmp;

            bluetoothAdapter.cancelDiscovery();

            try
            {
                mSocket.connect();
            }catch (IOException e)
            {
                try {
                    mSocket.close();
                }catch (IOException ex)
                {
                    Log.e("exc", ex.getMessage());
                }
            }

            connected(mSocket, mDevice);

        }

        public void cancel()
        {
            try
            {
                mSocket.close();
            }catch (IOException e)
            {
                Log.e("exc", e.getMessage());
            }
        }
    }

    public synchronized void start()
    {
        if (connectThread != null)
        {
            connectThread.cancel();
            connectThread = null;
        }
        if (acceptThread == null)
        {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device, UUID uuid)
    {
        mProgressDialog = ProgressDialog.show(getApplicationContext(), "Connecting Bluetooth", "Please wait...", true);
        connectThread = new ConnectThread(device, uuid);
        connectThread.start();
    }

    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            mProgressDialog.dismiss();

            try {
                tmpIn = mSocket.getInputStream();
                tmpOut = mSocket.getOutputStream();
            }catch (IOException e)
            {
                Log.e("exc", e.getMessage());
            }
            mInputStream = tmpIn;
            mOutputStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];

            int bytes;

            while(true)
            {
                try{
                bytes = mInputStream.read(buffer);
                String incomingMessage = new String(buffer, 0, bytes);
                Log.d("IncMsg", incomingMessage);
                }catch (IOException e)
                {
                    Log.e("exc", e.getMessage());
                    break;
                }
            }
        }

        public void write(byte[] bytes)
        {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d("OutMsg", text);
            try{
                mOutputStream.write(bytes);
            }catch (IOException e)
            {
                Log.e("exc", e.getMessage());
            }
        }

        public void cancel()
        {
            try{
                mSocket.close();
            }catch (IOException e)
            {
                Log.e("exc", e.getMessage());
            }
        }
    }

    private void connected(BluetoothSocket socket, BluetoothDevice device)
    {
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    public void write(byte[] out)
    {
        ConnectedThread tmp;

        connectedThread.write(out);
    }

}
