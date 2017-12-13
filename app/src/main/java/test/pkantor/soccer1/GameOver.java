package test.pkantor.soccer1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import test.pkantor.soccer1.Bluetooth.BluetoothConnectionService;
import test.pkantor.soccer1.Bluetooth.BluetoothMainActivity;

public class GameOver extends AppCompatActivity {


    Bundle extras;
    ProgressDialog mProgressDialog;
    boolean imReady = false;
    boolean secondPlayerReady = false;
    Intent intent;
    GlobalSocket gSocket;
    StringBuffer mOutStringBuffer;

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, Menu.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        TextView tv = (TextView) findViewById(R.id.textView3);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER);
        extras = getIntent().getExtras();
        int result = 0;
        String winner = "";

        gSocket = (GlobalSocket) getApplicationContext();

        if (gSocket.getBluetoothConnectionService() != null)
            gSocket.getBluetoothConnectionService().setHandler(mHandler);

        if (extras != null)

//            result = extras.getInt("goal");
            winner = extras.getString("winner");


        switch (result)
        {
            case 0:
                Resources resources = getResources();
                tv.setText(resources.getString(R.string.winner_is, winner));
                break;
//            case 1:
//                tv.setText("Wygrywa " + winner);
//                break;
            default:
                tv.setText("Otrzymano nieznana wartość: " + result);
                break;
        }
    }

    public void clickRematch(View v)
    {
        mOutStringBuffer = new StringBuffer("");
        imReady = true;

        intent = new Intent(this, Game.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        intent.putExtra("p1Name", extras.getString("p1Name"));
        intent.putExtra("p2Name", extras.getString("p2Name"));
        intent.putExtra("goalPoints", extras.getInt("goalPoints"));

        int gameMode = extras.getInt("gameMode");

        intent.putExtra("gameMode", gameMode);

        if (gameMode == Game.BLUETOOTH)
        {
            String message = "rematch";

            sendMessage(message);
            intent.putExtra("amIFirst", extras.getBoolean("amIFirst"));

            mProgressDialog = ProgressDialog.show(GameOver.this, getString(R.string.waiting_for_other_player), getString(R.string.please_wait), true);


            if ((imReady) && (secondPlayerReady))
                startGame(intent);
        }
        else
            startActivity(intent);

    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    switch (msg.arg1) {
                        case BluetoothConnectionService.STATE_CONNECTED:
                            break;
                        case BluetoothConnectionService.STATE_CONNECTING:
                            break;
                        case BluetoothConnectionService.STATE_LISTEN:
                            break;
                        case BluetoothConnectionService.STATE_NONE:
                            break;
                    }
                    break;
                case 3:
                    break;
                case 2:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    checkIfPlayerReady(readMessage);
                    break;
                case 4:

                    break;
                case 5:
                    if (null != getApplicationContext()) {
                        if (msg.getData().getInt("toast") == 0)
                        {
                            Toast.makeText(getApplicationContext(), getString(R.string.cannot_connect_to_device_error),
                                    Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(GameOver.this, Menu.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), getString(R.string.connection_lost),
                                    Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(GameOver.this, Menu.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }

                            if ((mProgressDialog != null) && (mProgressDialog.isShowing()))
                                mProgressDialog.dismiss();
                        }
                    break;
            }
        }
    };

    public boolean checkIfPlayerReady(String readMessage)
    {
        if (readMessage.equals("rematch"))
        {
            secondPlayerReady = true;
            if (imReady)
                startGame(intent);
        }
        return secondPlayerReady;
    }

    public void startGame(Intent intent)
    {
        if ((mProgressDialog != null) && (mProgressDialog.isShowing()))
            mProgressDialog.dismiss();

        startActivity(intent);
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (gSocket.getBluetoothConnectionService().getState() != BluetoothConnectionService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            gSocket.getBluetoothConnectionService().write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

}
