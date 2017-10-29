package test.pkantor.soccer1;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Pawel on 29.10.2017.
 */

public class Field extends android.support.v7.widget.AppCompatImageView{

    private int[][] shots;
    private ImageView imageResource;

    public Field(Context context) {
        super(context);
        this.shots = new int[3][3];
        this.setImageResource(R.drawable.pole);
        this.setClickable(true);
        this.setAdjustViewBounds(true);
    }

    public Field(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Field(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int[][] getShots() {
        return shots;
    }

    public void setShots(int[][] shots) {
        this.shots = shots;
    }
}
