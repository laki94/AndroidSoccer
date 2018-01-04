package com.pawel.kantor.praca.inzynierska;

import android.app.Application;
import android.os.Handler;

import com.pawel.kantor.praca.inzynierska.Bluetooth.BluetoothConnectionService;

/**
 * Created by Pawel on 29.11.2017.
 */

public class GlobalSocket extends Application {

    private BluetoothConnectionService mBluetoothConnectionService;
    private Handler mBluetoothHandler;
    private Handler mBluetoothGameHandler;
    private boolean amIConnected = false;

    public BluetoothConnectionService getBluetoothConnectionService() {
        return mBluetoothConnectionService;
    }

    public void setBluetoothConnectionService(BluetoothConnectionService mBluetoothConnectionService) {
        this.mBluetoothConnectionService = mBluetoothConnectionService;
    }

    public void setBluetoothHandler(Handler handler)
    {
        mBluetoothHandler = handler;
    }

    public void setAmIConnected(boolean state)
    {
        amIConnected = state;
    }

    public boolean getAmIConnected()
    {
        return amIConnected;
    }

    public void setBluetoothGameHandler(Handler handler)
    {
        mBluetoothGameHandler = handler;
    }

}
