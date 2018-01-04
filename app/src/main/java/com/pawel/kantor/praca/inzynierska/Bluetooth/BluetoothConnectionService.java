package com.pawel.kantor.praca.inzynierska.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pawel.kantor.praca.inzynierska.GlobalSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by Pawel on 19.20.2017.
 */

public class BluetoothConnectionService {
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("01186fa9-b3ed-455c-aea0-cb44a655ed1f");

    private static final String appName = "MyApp";

    private final BluetoothAdapter mBluetoothAdapter;

    public static final int CONNECTION_FAILED = 0;

    public static final int CONNECTION_LOST = 1;

    Context mContext;
    int mState;
    int mNewState;

    private AcceptThread mInsecureAcceptThread;

    private ConnectThread mConnectThread;
    private UUID deviceUUID;

    private Handler mHandler;
    private ConnectedThread mConnectedThread;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    public BluetoothConnectionService(Context context, Handler handler) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mNewState = mState = STATE_NONE;
        mHandler = handler;
    }

    public Handler getHandler()
    {
        return mHandler;
    }

    public void setHandler(Handler handler)
    {
        mHandler = handler;
    }

    private synchronized void updateUserInterfaceTitle() {
        mState = getState();
        mNewState = mState;

        mHandler.obtainMessage(1, mNewState, -1).sendToTarget();
    }

    public int getState() {return mState;}

    public synchronized void start() {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }

        updateUserInterfaceTitle();
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

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        mState = STATE_NONE;
        updateUserInterfaceTitle();
    }

    public synchronized void connect(BluetoothDevice device, UUID uuid) {

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
        updateUserInterfaceTitle();
    }

    private class AcceptThread extends Thread {

        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

            }catch (IOException e){
                Log.e("ConnectionService", "AcceptThread exception, " + e.getMessage() );
            }

            mmServerSocket = tmp;
            mState = STATE_LISTEN;
        }

        public void run(){
            BluetoothSocket socket = null;

            if (mBluetoothAdapter.isEnabled())
            while (mState != STATE_CONNECTED)
            {
                try
                {
                    socket = mmServerSocket.accept();
                }catch (IOException e)
                {
                    Log.e("ConnectionService", "AcceptThread Failed, " + e.getMessage());
                    break;
                }

                if (socket != null)
                {
                    synchronized (BluetoothConnectionService.this)
                    {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e("ConnectionService", "Could not close unwanted socket, " + e.getMessage());
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                if (mmServerSocket != null)
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e("ConnectionService", "cancel AcceptThread: ServerSocket close failed, " + e.getMessage() );
            }
        }

    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            deviceUUID = uuid;

            try
            {
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            }catch(IOException e)
            {
                Log.e("ConnectionService", "ConnectThread create fail, " + e.getMessage());
            }
            mmSocket = tmp;
            mState = STATE_CONNECTING;
        }

        public void run(){

            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();

            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException e1) {
                    Log.e("ConnectionService", "ConnectThread run, unable to close connection in socket, " + e1.getMessage());
                }
                connectionFailed();
                return;
            }

            synchronized (BluetoothConnectionService.this)
            {
                mConnectThread = null;
            }

            connected(mmSocket,mmDevice);
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("ConnectionService", "ConnectThread cancel, close() of mmSocket in failed, " + e.getMessage());
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        public void run(){
            byte[] buffer = new byte[1024];

            int bytes;

            while (mState == STATE_CONNECTED) {
                try {
                    bytes = mmInStream.read(buffer);

                    mHandler.obtainMessage(2, bytes, -1, buffer).sendToTarget();

                    String incomingMessage = new String(buffer, 0, bytes);
                } catch (IOException e) {
                    Log.e("ConnectionService", "ConnectedThread write, error reading InputStream, " + e.getMessage() );
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            try {
                mmOutStream.write(bytes);

                mHandler.obtainMessage(3, -1, -1, bytes).sendToTarget();
            } catch (IOException e) {
                Log.e("ConnectionService", "ConnectedThread write, error writing to OutputStream, " + e.getMessage() );
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("ConnectionService", "ConnectedThread close, cannot close mmSocket, " + e.getMessage());
            }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();

        Message msg = mHandler.obtainMessage(4);
        Bundle bundle = new Bundle();
        bundle.putString("device_name", mmDevice.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        updateUserInterfaceTitle();
    }

    public void write(byte[] out) {
        ConnectedThread r;

        synchronized (this)
        {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    private void connectionFailed() {
        Message msg = mHandler.obtainMessage(5);
        Bundle bundle = new Bundle();
        bundle.putInt("toast", CONNECTION_FAILED);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;
        updateUserInterfaceTitle();

        BluetoothConnectionService.this.start();
    }

    private void connectionLost() {
        Message msg = mHandler.obtainMessage(5);
        Bundle bundle = new Bundle();
        bundle.putInt("toast", CONNECTION_LOST);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;
        updateUserInterfaceTitle();

        GlobalSocket gSocket = (GlobalSocket) mContext;
        gSocket.setAmIConnected(false);

        BluetoothConnectionService.this.start();
    }

}