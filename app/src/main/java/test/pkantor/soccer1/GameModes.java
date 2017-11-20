package test.pkantor.soccer1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

public class GameModes extends AppCompatActivity{

    private Player player1 = new Player();
    private Player player2 = new Player();

    private AlertDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_modes);
    }

    public Dialog setNamesDialog()
    {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.activity_dialog_setnames);
        return dialog;
    }

    public void clickPlayBluetooth(View v)
    {
        Intent intent = new Intent(this, BluetoothService.class);
        startActivity(intent);
    }

    public void clickPlayLocal(View v)
    {
       //Bundle extras = new Bundle();
        Intent i = getIntent();
        Options options = i.getParcelableExtra("parcel_options");


        final Intent intent = new Intent(this, Game.class);

        if (options != null)
            intent.putExtra("parcel_options", options);

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

        saveNames.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
//                if ((!p1Name.getText().toString().isEmpty()) && (!p2Name.getText().toString().isEmpty()))
//                {
                    if ((p1Name.getText().length() > 8) || (p2Name.getText().length() > 7))
                        Toast.makeText(getApplicationContext(), "Nazwa gracza nie może przekraczać 7 znaków", Toast.LENGTH_LONG).show();
                    else if ((p1Name.getText().toString().contains(" ")) || p2Name.getText().toString().contains(" "))
                        Toast.makeText(getApplicationContext(), "Nie używaj białych znaków", Toast.LENGTH_LONG).show();
                    else
                    {
                        dialog.dismiss();
                        if (p1Name.getText().length() != 0)
                            intent.putExtra("p1Name", p1Name.getText().toString());
                        else
                            intent.putExtra("p1Name", "Gracz 1");

                        if (p2Name.getText().length() != 0)
                            intent.putExtra("p2Name", p2Name.getText().toString());
                        else
                            intent.putExtra("p2Name", "Gracz 2");

                        intent.putExtra("goalPoints", np.getValue());
                        startActivity(intent);
                    }
//                    Toast.makeText(GameModes.this, "blbelel", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setView(mView);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public void clickPlayNet(View v)
    {
        Intent intent = new Intent(this, BluetoothConnection.class);
        startActivity(intent);
    }
}
