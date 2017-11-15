package test.pkantor.soccer1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothConnection extends AppCompatActivity {


    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    ListView listView;

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


}
