package test.pkantor.soccer1;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Pawel on 29.10.2017.
 */

public class Field extends android.support.v7.widget.AppCompatImageView{

    private int[][] shots;
    private int[][] lastShot;
    private ImageView imageResource;

    public Field(Context context) {
        super(context);
        this.shots = new int[3][3];
        this.lastShot = new int[3][3];
        initializeShots();
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

    public int[][] getLastShot() {
        return lastShot;
    }

    public void setLastShot(int[][] lastShot) {
        this.lastShot = lastShot;
    }

    public int[][] getShots() {
        return shots;
    }

    public void setShots(int[][] shots) {
        this.shots = shots;
    }


    private void initializeShots()
    {
        for (int i=0;i<shots.length;i++)
            for(int j=0;j<shots.length;j++)
                lastShot[i][j] = shots[i][j] = 0;


    }
}
