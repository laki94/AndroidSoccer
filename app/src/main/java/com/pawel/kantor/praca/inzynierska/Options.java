package com.pawel.kantor.praca.inzynierska;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.TypedValue;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

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

        etPlayerName.setTextSize(TypedValue.COMPLEX_UNIT_SP, (sharedPreferences.getInt(getString(R.string.SP_count_x), 12) / res.getDisplayMetrics().scaledDensity) / 2);
        tvEnterPlayerName.setTextSize(TypedValue.COMPLEX_UNIT_SP, (sharedPreferences.getInt(getString(R.string.SP_count_x), 12) / res.getDisplayMetrics().scaledDensity) / 2);

        cbAcceptMove.setChecked(sharedPreferences.getBoolean(getString(R.string.SP_show_accept_move), true));
        cbShowPlayerNames.setChecked(sharedPreferences.getBoolean(getString(R.string.SP_show_player_names), true));
        cbEnableVibrations.setChecked(sharedPreferences.getBoolean(getString(R.string.SP_enable_vibrations), true));

        etPlayerName.setText(sharedPreferences.getString(getString(R.string.SP_player_name), getString(R.string.default_fplayer)));
        etPlayerName.setFilters(new InputFilter[]
                {
                        new InputFilter() {
                            public CharSequence filter(CharSequence src, int start, int end, Spanned dst, int dstart, int dend)
                            {
                                if (src.toString().matches(getString(R.string.allowed_words)))
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
        editor.putBoolean(getString(R.string.SP_show_accept_move), getShowAcceptMove());
        editor.putBoolean(getString(R.string.SP_show_player_names), getShowPlayerNames());
        editor.putBoolean(getString(R.string.SP_enable_vibrations), getEnableVibrations());
        editor.putString(getString(R.string.SP_player_name), getPlayerName());

        editor.apply();

        finish();
    }
}