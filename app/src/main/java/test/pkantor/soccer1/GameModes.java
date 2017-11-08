package test.pkantor.soccer1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class GameModes extends AppCompatActivity{

    private Player player1 = new Player();
    private Player player2 = new Player();

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

    public void clickPlayLocal(View v)
    {
        final Intent intent = new Intent(this, Game.class);
        intent.putExtra("mode", 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.activity_dialog_setnames, null);
        final EditText p1Name = (EditText) mView.findViewById(R.id.etFirstPlayerName);
        final EditText p2Name = (EditText) mView.findViewById(R.id.etSecondPlayerName);
        Button saveNames = (Button) mView.findViewById(R.id.bSaveNames);

        saveNames.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if ((!p1Name.getText().toString().isEmpty()) && (!p2Name.getText().toString().isEmpty()))
                {
                    intent.putExtra("p1Name", p1Name.getText().toString());
                    intent.putExtra("p2Name", p2Name.getText().toString());
                    startActivity(intent);
                }
                else
                    Toast.makeText(GameModes.this, "blbelel", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setView(mView);
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}
