package com.pawel.kantor.praca.inzynierska;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.pawel.kantor.praca.inzynierska.Bluetooth.BluetoothMainActivity;

/**
 * Created by Pawel on 08.11.2017.
 */

public class GameModes extends AppCompatActivity{

    private AlertDialog dialog = null;
    SharedPreferences sharedPreferences;

    Resources res;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_modes);

        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preferences_soccer), Context.MODE_PRIVATE);
        res = getResources();
    }

    public void clickPlayBluetooth(View v)
    {
        Intent intent = new Intent(this, BluetoothMainActivity.class);
        startActivity(intent);
    }

    public void clickPlayLocal(View v)
    {
        final Intent intent = new Intent(this, Game.class);

        intent.putExtra("gameMode", Game.LOCAL);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.activity_dialog_setnames, null);
        final EditText p1Name = (EditText) mView.findViewById(R.id.etFirstPlayerName);
        final EditText p2Name = (EditText) mView.findViewById(R.id.etSecondPlayerName);
        Button saveNames = (Button) mView.findViewById(R.id.bSaveNames);

        p1Name.setText(sharedPreferences.getString(getString(R.string.SP_player_name), getString(R.string.default_fplayer)));

        final NumberPicker numberPicker = (NumberPicker) mView.findViewById(R.id.np);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(9);
        numberPicker.setWrapSelectorWheel(true);

        p1Name.setFilters(new InputFilter[]
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

        p2Name.setFilters(new InputFilter[]
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


        saveNames.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
                if (p1Name.getText().length() != 0)
                    intent.putExtra("p1Name", p1Name.getText().toString());
                else
                    intent.putExtra("p1Name", res.getString(R.string.default_fplayer));

                if (p2Name.getText().length() != 0)
                    intent.putExtra("p2Name", p2Name.getText().toString());
                else
                    intent.putExtra("p2Name", res.getString(R.string.default_splayer));

                intent.putExtra("goalPoints", numberPicker.getValue());
                startActivity(intent);
            }
        });

        builder.setView(mView);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

}
