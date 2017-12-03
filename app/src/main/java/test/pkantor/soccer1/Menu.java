package test.pkantor.soccer1;

import android.app.AlertDialog;
import android.app.VoiceInteractor;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import static java.lang.Math.round;

public class Menu extends AppCompatActivity {
    Button bPlay;
    Button bQuit;
    Button bOptions;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.activity_dialog_exit, null);
        builder.setView(mView);
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        Button bYes = (Button) mView.findViewById(R.id.bYes);
        Button bNo = (Button) mView.findViewById(R.id.bNo);

        bYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
                System.exit(0);
            }
        });
        bNo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu);
        bPlay = (Button) findViewById(R.id.bPlay);
        bQuit = (Button) findViewById(R.id.bQuit);
        bOptions = (Button) findViewById(R.id.bOptions);

        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preferences_soccer), Context.MODE_PRIVATE);
        calculateScreenParams();

        GlobalSocket globalSocket = (GlobalSocket) getApplicationContext();
        if (globalSocket.getBluetoothConnectionService() != null)
        {
            globalSocket.getBluetoothConnectionService().stop();
            globalSocket.setBluetoothConnectionService(null);
            globalSocket.setBluetoothHandler(null);

        }
    }
    public void clickPlay(View v) {
        Intent intent = new Intent(this, GameModes.class);
        startActivity(intent);
    }
    public void clickOptions(View v) {
        Intent intent = new Intent(this, Options.class);
        startActivity(intent);
    }
    public void clickQuit(View v) {
        finish();
        System.exit(0);
    }

    public void calculateScreenParams()
    {
        editor = sharedPreferences.edit();
        int countX;
        int countY;
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();

        countY = round(displayMetrics.heightPixels / 13);
        countX = round(displayMetrics.widthPixels / 11);

        editor.putInt(getString(R.string.SPdevHeight), displayMetrics.heightPixels);
        editor.putInt(getString(R.string.SPdevWidth), displayMetrics.widthPixels);
        editor.putInt(getString(R.string.SPcountX), countX);
        editor.putInt(getString(R.string.SPcountY), countY);
        editor.apply();
    }
}
