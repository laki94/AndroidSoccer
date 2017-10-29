package test.pkantor.soccer1;

import android.content.Context;
import android.media.Image;
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

    private float fx;
    private float sx;
    private float fy;
    private float sy;
    private FrameLayout.LayoutParams params;

    public FrameLayout.LayoutParams getParams() {
        return params;
    }

    public void setParams(FrameLayout.LayoutParams params) {
        this.params = params;
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
//        this.params = new FrameLayout.LayoutParams();
    }

    public Line(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Line(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean createLine(ImageView src, ImageView dst)
    {
        fx = src.getX();
        fy = src.getY();
        sx = dst.getX();
        sy = dst.getY();

        params = new FrameLayout.LayoutParams(100,100);

        if ((abs(fx - sx) > src.getWidth()) || (abs(fy - sy) > src.getHeight()))
        {
            Toast.makeText(getContext(), "Zbyt dlugi ruch", Toast.LENGTH_LONG).show();
            return false;
        }
        else if ((fx == sx) && (fy != sy))
            return createVerticalLine(src);
        else if ((fy == sy) && (fx != sx))
            return createHorizontalLine(src);
        else
            return createDiagonalLine(src);
    }

    public boolean createVerticalLine(ImageView iv)
    {
        try {
           // this.params.width = iv.getWidth();
//            this.params.height = 10;
            this.setRotation(0);
           // fx -= 6;
            fy += iv.getHeight() / 2;
            sy += iv.getHeight() / 2;
            //fy += countY / 2 - 18;
           // sx -= 6;
            //sy += countY / 2 - 18;

            params.leftMargin = ((int) getFx() < (int) getSx()) ? (int) getFx() : (int) getSx();
            params.topMargin = ((int) getFy() < (int) getSy()) ? (int) getFy(): (int) getSy();
            return true;
        } catch (Exception e) {
            Log.e("Exc:", e.getMessage());
            return false;
        }

    }

    public boolean createHorizontalLine(ImageView iv)
    {
        try
        {
           // this.params.width = iv.getWidth();
//            this.params.height = 10;
            this.setRotation(90);
            fx += iv.getWidth() / 2;
            sx += iv.getWidth() / 2;

            params.leftMargin = ((int) getFx() < (int) getSx()) ? (int) getFx() : (int) getSx();
            params.topMargin = ((int) getFy() < (int) getSy()) ? (int) getFy(): (int) getSy();
            return true;
        } catch (Exception e)
        {
            Log.e("Exc:", e.getMessage());
            return false;
        }
    }

    public boolean createDiagonalLine(ImageView iv)
    {
        if (((fx > sx) && (fy < sy)) || ((fx < sx) && (fy > sy)))
            this.setRotation(45);
        else if (((fx < sx) && (fy < sy)) || ((fx > sx) && (fy > sy)))
            this.setRotation(135);
        else
        {
            Toast.makeText(getContext(), "Nieznany ruch", Toast.LENGTH_SHORT).show();
            return false;
        }

        int skos = (int) sqrt(pow(iv.getWidth(), 2) + pow(iv.getHeight(), 2));
        fx += iv.getWidth() / 2;
        sx += iv.getWidth() / 2;
        fy += iv.getHeight() / 2 - 19;
        sy += iv.getHeight() / 2 - 19;

        //this.params.width = 8;
        this.params.height = skos;

        params.leftMargin = ((int) getFx() < (int) getSx()) ? (int) getFx() : (int) getSx();
        params.topMargin = ((int) getFy() < (int) getSy()) ? (int) getFy(): (int) getSy();

        return true;
    }
}
