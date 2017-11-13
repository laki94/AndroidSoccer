package test.pkantor.soccer1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;

public class Options extends AppCompatActivity implements Parcelable {

    CheckBox cbAcceptMove;

    int acceptMove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        cbAcceptMove = (CheckBox) findViewById(R.id.cbShowAcceptMove);
    }

    public static final Parcelable.Creator<Options> CREATOR = new Parcelable.Creator<Options>() {
        public Options createFromParcel(Parcel in) {
            return new Options(in);
        }

        public Options[] newArray(int size) {
            return new Options[size];
        }
    };

    public Options ()
    {

    }

    public Options(Parcel in)
    {
        acceptMove = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(1);
    }

    public boolean getAcceptMove()
    {
        if (acceptMove == 1)
            return true;
        else
            return false;
    }

    @Override
    public void onBackPressed()
    {
        Options options = new Options();

        Intent intent = new Intent(this, Menu.class);
        intent.putExtra("parcel_options", options);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}