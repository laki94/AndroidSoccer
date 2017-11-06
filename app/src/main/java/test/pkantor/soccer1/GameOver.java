package test.pkantor.soccer1;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

import test.pkantor.soccer1.R;

public class GameOver extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        TextView tv = (TextView) findViewById(R.id.textView3);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER);
        Bundle extras = getIntent().getExtras();
        int result = 0;
        if (extras != null)
            result = extras.getInt("goal");
        switch (result)
        {
            case 1:
                tv.setText("Bramka samobójcza, wygrywa Gracz 2");
                break;
            case 2:
                tv.setText("Wygrywa Gracz 1");
                break;
            case 3:
                tv.setText("Bramka samobójcza, wygrywa Gracz 2");
                break;
            case 4:
                tv.setText("Wygrywa Gracz 2");
                break;
            default:
                tv.setText("Otrzymano nieznana wartość: " + result);
                break;
        }
    }


}
