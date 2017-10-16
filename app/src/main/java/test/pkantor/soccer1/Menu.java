package test.pkantor.soccer1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class Menu extends AppCompatActivity {
    Button bPlay;
    Button bQuit;
    Button bOptions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        bPlay = (Button) findViewById(R.id.bPlay);
        bQuit = (Button) findViewById(R.id.bQuit);
        bOptions = (Button) findViewById(R.id.bOptions);
    }
    public void clickPlay(View v) {
        Intent intent = new Intent(this, Game.class);
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
}
