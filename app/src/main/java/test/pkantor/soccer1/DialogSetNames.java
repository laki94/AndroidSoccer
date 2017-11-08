package test.pkantor.soccer1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by Pawel on 08.11.2017.
 */

public class DialogSetNames extends DialogFragment implements TextView.OnEditorActionListener{

    private EditText p1;
    private EditText p2;

    public interface PlayersListener{
        void onFinishDialog(String p1Name, String p2Name);
    }

    public DialogSetNames() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.activity_dialog_setnames, container);
        p1 = (EditText) v.findViewById(R.id.etFirstPlayerName);
        p2 = (EditText) v.findViewById(R.id.etSecondPlayerName);

        p1.setOnEditorActionListener(this);
        p2.setOnEditorActionListener(this);
        p1.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return v;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
    {
        PlayersListener activity = (PlayersListener) getActivity();
        activity.onFinishDialog(p1.getText().toString(), p2.getText().toString());
        this.dismiss();
        return true;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        return new AlertDialog.Builder(getActivity())
                .setPositiveButton("Zapisz", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
    }
}
