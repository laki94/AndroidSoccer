package test.pkantor.soccer1;

import android.app.Application;
import android.os.Handler;

import test.pkantor.soccer1.Bluetooth.BluetoothConnectionService;

/**
 * Created by Pawel on 29.11.2017.
 */

public class GlobalSocket extends Application {

    private BluetoothConnectionService mBluetoothConnectionService;
    private Handler mBluetoothHandler;

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

    public Handler getBluetoothHandler()
    {
        return mBluetoothHandler;
    }

}
