package test.pkantor.soccer1;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Pawel on 29.10.2017.
 */

public class Ball extends android.support.v7.widget.AppCompatImageView {

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    int centerX;
    int centerY;

    public Ball(Context context) {
        super(context);
    }

    public Ball(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Ball(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void createBall (ImageView iv)
    {
        centerX = iv.getWidth() / 4;
        centerY = iv.getHeight() / 4;

        this.setImageResource(R.drawable.sball);
        this.setAdjustViewBounds(true);
        this.bringToFront();
        this.setClickable(false);

    }
}
