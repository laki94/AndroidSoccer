package test.pkantor.soccer1;

import android.app.AlertDialog;
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
    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.activity_dialog_exit, null);
        builder.setView(mView);
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        Button bYes = (Button) mView.findViewById(R.id.bYes);
        Button bNo = (Button) mView.findViewById(R.id.bNo);

        bYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
                System.exit(0);
            }
        });
        bNo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu);
        bPlay = (Button) findViewById(R.id.bPlay);
        bQuit = (Button) findViewById(R.id.bQuit);
       // bOptions = (Button) findViewById(R.id.bOptions);
    }
    public void clickPlay(View v) {
//        Bundle extras = getIntent().getExtras();
//        Options options = null;
//        if (extras != null)
//            options = extras.getParcelable("parcel_options");
//
        Intent i = getIntent();
        Options options = i.getParcelableExtra("parcel_options");

        Intent intent = new Intent(this, GameModes.class);
        if (options != null)
            intent.putExtra("parcel_options", options);
        startActivity(intent);
    }
//    public void clickOptions(View v) {
//        Intent intent = new Intent(this, Options.class);
//        startActivity(intent);
//    }
    public void clickQuit(View v) {
        finish();
        System.exit(0);
    }
}
