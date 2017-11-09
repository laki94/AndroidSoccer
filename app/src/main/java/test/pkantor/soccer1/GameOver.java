package test.pkantor.soccer1;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

import test.pkantor.soccer1.R;

public class GameOver extends AppCompatActivity {

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, Menu.class); // TODO dodac do intentu wysylanie nazw graczy
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
        Bundle extras = getIntent().getExtras();
        int result = 0;
        String winner = "";
        if (extras != null)
        {
            result = extras.getInt("goal");
            winner = extras.getString("winner");
        }

        switch (result)
        {
            case 1:
                tv.setText("Bramka samobójcza, wygrywa gracz " + winner);
                break;
            case 2:
                tv.setText("Wygrywa " + winner);
                break;
            case 3:
                tv.setText("Bramka samobójcza, wygrywa gracz " + winner);
                break;
            case 4:
                tv.setText("Wygrywa " + winner);
                break;
            default:
                tv.setText("Otrzymano nieznana wartość: " + result);
                break;
        }
    }


}
