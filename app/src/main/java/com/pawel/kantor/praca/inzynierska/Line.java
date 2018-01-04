package com.pawel.kantor.praca.inzynierska;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

/**
 * Created by Pawel on 29.10.2017.
 */

public class Line extends android.support.v7.widget.AppCompatImageView {

    private int width;
    private int height;
    private float fx;
    private float sx;
    private float fy;
    private float sy;
    private FrameLayout.LayoutParams params;


    public FrameLayout.LayoutParams getParams() {
        return params;
    }

    public float getFx() {
        return fx;
    }

    public float getSx() {
        return sx;
    }

    public float getFy() {
        return fy;
    }

    public float getSy() {
        return sy;
    }

    public Line(Context context) {
        super(context);
        this.setImageResource(R.drawable.linia);
    }

    public Line(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Line(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean canCreateLine(ImageView src, ImageView dst)
    {
        if ((src == null) || (dst == null))
            return false;

        fx = src.getX();
        fy = src.getY();
        sx = dst.getX();
        sy = dst.getY();

        if ((abs(fx - sx) > src.getWidth()) || (abs(fy - sy) > src.getHeight()))
        {
            return false;
        }
        else if (src.equals(dst))
            return false;

        return true;
    }

    public boolean createLine(ImageView src, ImageView dst)
    {
        if ((src == null) || (dst == null))
            return false;

        Resources res = getResources();
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(res.getString(R.string.preferences_soccer), Context.MODE_PRIVATE);
        int devWidth = sharedPreferences.getInt("devWidth", 1);
        int devHeight = sharedPreferences.getInt("devHeight", 1);

        fx = src.getX();
        fy = src.getY();
        sx = dst.getX();
        sy = dst.getY();

        width = round(devWidth / 11);
        height = round(devHeight / 130);

        params = new FrameLayout.LayoutParams(height, width);
        this.setScaleType(ScaleType.CENTER_CROP);
        if (canCreateLine(src, dst)) {
            if ((fx == sx) && (fy != sy))
                return createVerticalLine(src);
            else if ((fy == sy) && (fx != sx))
                return createHorizontalLine(src);
            else
                return createDiagonalLine(src);
        }
        else
            return false;
    }

    public boolean createVerticalLine(ImageView iv)
    {
        if (iv == null)
            return false;
        try {
            this.setRotation(0);

            fy += iv.getHeight() / 2;
            sy += iv.getHeight() / 2;
            fx += (iv.getWidth() / 2 - params.width / 2) + 1;
            sx += (iv.getWidth() / 2 - params.width / 2) + 1;

            params.leftMargin = ((int) getFx() < (int) getSx()) ? (int) getFx() : (int) getSx();
            params.topMargin = ((int) getFy() < (int) getSy()) ? (int) getFy(): (int) getSy();
            this.setLayoutParams(params);

            return true;
        } catch (Exception e) {
            Log.e("Line","Creating vertical line error, " + e.getMessage());
            return false;
        }

    }

    public boolean createHorizontalLine(ImageView iv)
    {
        if (iv == null)
            return false;
        try
        {
            this.setRotation(90);
            fx += (iv.getWidth() - params.width / 2);
            sx += (iv.getWidth() - params.width / 2);

            params.leftMargin = ((int) getFx() < (int) getSx()) ? (int) getFx() : (int) getSx();
            params.topMargin = ((int) getFy() < (int) getSy()) ? (int) getFy(): (int) getSy();

            return true;
        } catch (Exception e)
        {
            Log.e("Line", "Creating horizontal line error, " + e.getMessage());
            return false;
        }
    }

    public boolean createDiagonalLine(ImageView iv)
    {
        if (iv == null)
            return false;

        if (((fx > sx) && (fy < sy)) || ((fx < sx) && (fy > sy)))
            this.setRotation(45);
        else if (((fx < sx) && (fy < sy)) || ((fx > sx) && (fy > sy)))
            this.setRotation(135);
        else
        {
            Toast.makeText(getContext(), "Nieznany ruch", Toast.LENGTH_SHORT).show();
            return false;
        }

        int diagonal = (int) sqrt(pow(iv.getWidth(), 2) + pow(iv.getHeight(), 2)) + 4;

        int measureError = (diagonal + 2 - width) / 9;

        fx += iv.getWidth() - measureError;
        sx += iv.getWidth() - measureError;
        fy += (iv.getHeight() / 3) - measureError;
        sy += (iv.getHeight() / 3) - measureError;

        this.params.height = diagonal;

        params.leftMargin = ((int) getFx() < (int) getSx()) ? (int) getFx() : (int) getSx();
        params.topMargin = ((int) getFy() < (int) getSy()) ? (int) getFy(): (int) getSy();

        return true;
    }
}
