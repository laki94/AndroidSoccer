package com.pawel.kantor.praca.inzynierska;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;


import com.pawel.kantor.praca.inzynierska.R;
/**
 * Created by Pawel on 29.10.2017.
 * https://commons.wikimedia.org/wiki/File:Soccer_ball.svg
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

        this.setImageResource(R.drawable.ball);
        this.setAdjustViewBounds(true);
        this.bringToFront();
        this.setClickable(false);

    }
}
