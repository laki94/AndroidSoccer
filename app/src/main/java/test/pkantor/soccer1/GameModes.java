package test.pkantor.soccer1;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

import test.pkantor.soccer1.Bluetooth.BluetoothMainActivity;

public class GameModes extends AppCompatActivity{

    private Player player1 = new Player();
    private Player player2 = new Player();
    private AlertDialog dialog = null;

    Resources res;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_modes);

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

        intent.putExtra("mode", 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.activity_dialog_setnames, null);
        final EditText p1Name = (EditText) mView.findViewById(R.id.etFirstPlayerName);
        final EditText p2Name = (EditText) mView.findViewById(R.id.etSecondPlayerName);
        Button saveNames = (Button) mView.findViewById(R.id.bSaveNames);

        final NumberPicker np = (NumberPicker) mView.findViewById(R.id.np);
        np.setMinValue(1);
        np.setMaxValue(9);
        np.setWrapSelectorWheel(true);

        p1Name.setFilters(new InputFilter[]
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

        p2Name.setFilters(new InputFilter[]
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


        saveNames.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
                if (p1Name.getText().length() != 0)
                    intent.putExtra("p1Name", p1Name.getText().toString());
                else
                    intent.putExtra("p1Name", res.getString(R.string.pl_DefaultPlayer1));

                if (p2Name.getText().length() != 0)
                    intent.putExtra("p2Name", p2Name.getText().toString());
                else
                    intent.putExtra("p2Name", res.getString(R.string.pl_DefaultPlayer2));

                intent.putExtra("goalPoints", np.getValue());
                startActivity(intent);
            }
//                    Toast.makeText(GameModes.this, "blbelel", Toast.LENGTH_SHORT).show()
        });

        builder.setView(mView);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public void clickPlayNet(View v)
    {

    }
}
