package test.pkantor.soccer1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RecoverySystem;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import static java.lang.Math.round;

public class Options extends AppCompatActivity {

    CheckBox cbAcceptMove;
    CheckBox cbShowPlayerNames;
    CheckBox cbEnableVibrations;
    EditText etPlayerName;
    TextView tvEnterPlayerName;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Resources res;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        res = getResources();

        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preferences_soccer), Context.MODE_PRIVATE);

        cbAcceptMove = (CheckBox) findViewById(R.id.cbShowAcceptMove);
        cbShowPlayerNames = (CheckBox) findViewById(R.id.cbShowPlayerNames);
        cbEnableVibrations = (CheckBox) findViewById(R.id.cbEnableVibrations);
        etPlayerName = (EditText) findViewById(R.id.etPlayerName);
        tvEnterPlayerName = (TextView) findViewById(R.id.tvEnterPlayerName);

        etPlayerName.setTextSize(TypedValue.COMPLEX_UNIT_SP, (sharedPreferences.getInt(getString(R.string.SPcountX), 12) / res.getDisplayMetrics().scaledDensity) / 2);
        tvEnterPlayerName.setTextSize(TypedValue.COMPLEX_UNIT_SP, (sharedPreferences.getInt(getString(R.string.SPcountX), 12) / res.getDisplayMetrics().scaledDensity) / 2);

        cbAcceptMove.setChecked(sharedPreferences.getBoolean(getString(R.string.SPshowAcceptMove), true));
        cbShowPlayerNames.setChecked(sharedPreferences.getBoolean(getString(R.string.SPshowPlayerNames), true));
        cbEnableVibrations.setChecked(sharedPreferences.getBoolean(getString(R.string.SPenableVibrations), true));

        etPlayerName.setText(sharedPreferences.getString(getString(R.string.SPplayerName), getString(R.string.DefaultPlayer1)));
        etPlayerName.setFilters(new InputFilter[]
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

    }

    public Options ()
    {

    }

    public boolean getShowAcceptMove()
    {
        return cbAcceptMove.isChecked();
    }

    public boolean getShowPlayerNames()
    {
        return cbShowPlayerNames.isChecked();
    }

    public boolean getEnableVibrations()
    {
        return cbEnableVibrations.isChecked();
    }

    public String getPlayerName()
    {
        return etPlayerName.getText().toString();
    }
    @Override
    public void onBackPressed()
    {
        editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.SPshowAcceptMove), getShowAcceptMove());
        editor.putBoolean(getString(R.string.SPshowPlayerNames), getShowPlayerNames());
        editor.putBoolean(getString(R.string.SPenableVibrations), getEnableVibrations());
        editor.putString(getString(R.string.SPplayerName), getPlayerName());

        editor.apply();

        finish();
    }
}