package test.pkantor.soccer1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;

public class Options extends AppCompatActivity{

    CheckBox cbAcceptMove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
    }

    @Override
    public void onBackPressed()
    {
        //TODO wysylanie informacji czy wyswietlac przycisk akceptacji czy nie
    }

    public void clickSetNames(View v)
    {
       //Intent intent = new Intent(this, dialog_setnames.class);

        //startActivity(intent);

       // AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        //builder.setMessage("mesedz").setTitle("tytul");
        //AlertDialog dialog = builder.create();
       // dialog.show();



//        Dialog dialog = new Dialog(this);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setCancelable(true);
//        dialog.setContentView(R.layout.activity_dialog_setnames);
//
//        dialog.show();
    }

//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState)
//    {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        LayoutInflater inflater = this.getLayoutInflater();
//
//        builder.setView(inflater.inflate(R.layout.activity_dialog_setnames, null))
//                .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                })
//                .setNegativeButton("Nie", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                });
//        return builder.create();
//    }
}
