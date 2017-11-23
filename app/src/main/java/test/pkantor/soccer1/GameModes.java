package test.pkantor.soccer1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import test.pkantor.soccer1.Bluetooth.MainActivity;

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
        Intent intent = new Intent(this, MainActivity.class);
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

        p1Name.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) // TODO ustawic ze mozna pisac tylko litery
            {
                if (p1Name.getText().length() > p1Name.getMaxEms())
                {
                    Toast.makeText(getApplicationContext(), res.getString(R.string.pl_NameOverMaxEms, p1Name.getMaxEms()), Toast.LENGTH_LONG).show();
                    p1Name.setText(s.subSequence(0, p1Name.getMaxEms()));
                }


                if (s.toString().contains(" "))
                {
                    Toast.makeText(getApplicationContext(), res.getString(R.string.pl_WhitespaceError), Toast.LENGTH_LONG).show();
                    p1Name.setText(s.subSequence(0, start));
                }

                p1Name.setSelection(p1Name.getText().length());

            }
        });

        p2Name.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (p2Name.getText().length() > p2Name.getMaxEms())
                {
                    Toast.makeText(getApplicationContext(), res.getString(R.string.pl_NameOverMaxEms, p2Name.getMaxEms()), Toast.LENGTH_LONG).show();
                    p2Name.setText(s.subSequence(0, p2Name.getMaxEms()));
                }

                if (s.toString().contains(" "))
                {
                    Toast.makeText(getApplicationContext(), res.getString(R.string.pl_WhitespaceError), Toast.LENGTH_LONG).show();
                    p2Name.setText(s.subSequence(0, start));
                }


                p2Name.setSelection(p2Name.getText().length());
            }
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
