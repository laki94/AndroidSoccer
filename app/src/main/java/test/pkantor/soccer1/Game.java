package test.pkantor.soccer1;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.Image;
import android.opengl.Visibility;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManagerNonConfig;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;
import static test.pkantor.soccer1.R.layout.activity_game;


public class Game extends AppCompatActivity {

    public static final int POLE_WYSOKOSC = 65;
    public static final int POLE_SZEROKOSC = 65;


    FrameLayout lay;

    float x,y,dx,dy;
    FrameLayout.LayoutParams flParams;

    int i = 0;
    int left = 0;
    int top = 0;
    int srodekX;
    int srodekY;
    int countX;
    int countY;
    ImageView _source;
    ImageView _destination;
    ImageView _lastDestination;
    ImageView pilka;
    ScaleGestureDetector scaleGestureDetector;
    List<ImageView> listViews;
    List<ImageView> listLinie;
    private float scale = 1f;
    boolean game = false;

//    float fx = 0;
//    float fy = 0;
//    float sx = 0;
//    float sy = 0;
//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_game);
        Button boisko = (Button) findViewById(R.id.boisko);
        boisko.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                rysujBoisko();
            }
        });

        int c = 0;
        int r = 0;
        listViews = new ArrayList<>();
        listLinie = new ArrayList<>();
        lay = (FrameLayout) findViewById(R.id.FLlay);
        lay.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rysujBoisko();
                rysujPilke();
                lay.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int height = displayMetrics.heightPixels;// - 60;
        int width = displayMetrics.widthPixels;
        countY = round(height/13);
        countX = round(width/11);
        srodekX = countX/2 - 1;
        for (int i = 1; i < 144; i++) { //countX*countY
            final Field pol = new Field(this);
            pol.setId(i - 1);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(countX, countX);
            params.leftMargin = left;
            params.topMargin = top;

            //params.
//            params.setGravity(Gravity.CENTER_HORIZONTAL);
//            params.columnSpec = GridLayout.spec(c);
//            params.rowSpec = GridLayout.spec(r);
//            iv.setLayoutParams(params);
            lay.addView(pol, params);
            listViews.add(pol);
            if ((i % 11 == 0) && (i != 1)) {
                top += countX;
                left = 0;
            } else left += countX;

            pol.setOnTouchListener(new View.OnTouchListener() { //1111111111111111111111111111111
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch(motionEvent.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            Log.d("DOWN","" + pol.getId());
                           // if (game)
                                _destination = (ImageView) findViewById(pol.getId());
                            return true;
                        case MotionEvent.ACTION_UP:
                            Log.d("UP","" + pol.getId());
                            if (_source != _destination)
                            //if ((!game) && ((_destination == _lastDestination) || (_source == _lastDestination) || (_lastDestination == null)))
                            {
                                if (rysujLinie(_source, _destination))
                                {
                                    pilka.setX(_destination.getLeft() + _destination.getWidth()/4);
                                    pilka.setY(_destination.getTop() + _destination.getHeight()/4);
                                    pilka.bringToFront();
                                    pilka.invalidate();
                                    _source = _destination;
                                }
                            }
                            else
                                Toast.makeText(getApplicationContext(), "Wcisnij pole swojego następnego ruchu.", Toast.LENGTH_SHORT).show();

                            return false;
                        default:
                            return false;
                    }
                }
//
//////            iv.setOnClickListener(new View.OnClickListener() {
//////                @Override
//////                public void onClick(View view) {
//////                    Log.d("Clickediv","" + iv.getId());
//////                }
//////            });
//////            iv.setOnLongClickListener(new View.OnLongClickListener() {
//////                @Override
//////                public boolean onLongClick(View view)
//////                {
//////                    Log.d("Hold","" + iv.getId());
//////                    return true;
//////                }
            });
        }
        _source = listViews.get(71);

    }

    public void clickWyczysc()
    {
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.FLlay);
        ImageView iv = new ImageView(this);
        // for (ImageView image: listLinie)
        if (listLinie.size()>40)
        {
            iv = listLinie.get(listLinie.size() - 1);
            iv.setVisibility(View.GONE);
            listLinie.remove(listLinie.size() - 1);
            iv = listLinie.get(listLinie.size() - 1);
            _lastDestination = iv;
        }
        else
            Toast.makeText(this, "Nie mozna wrocic bardziej", Toast.LENGTH_SHORT).show();
    }

    public boolean rysujLinie(ImageView source, ImageView destination) // TODO poprawic wyswietlanie linii
    {
        Line line = new Line(this);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.FLlay);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(countX + 15, _source.getWidth()); // szer ; dl
        if (line.createLine(source, destination))
        {
           // params.leftMargin = ((int) line.getFx() < (int) line.getSx()) ? (int) line.getFx() : (int) line.getSx();
           // params.topMargin = ((int) line.getFy() < (int) line.getSy()) ? (int) line.getFy(): (int) line.getSy();
            listLinie.add(line);
            lay.addView(line, line.getParams());
            lay.invalidate();

            Log.d("COORDS", "fx: " + line.getFx() + " fy: " + line.getFy() + " sx: " + line.getSx() + " sy: " + line.getSy());
            return true;
        }
        else
            return false;
    }

    public void rysujPilke()
    {
        int SRODEK = 71;
        Ball ball = new Ball(this);
        ball.createBall(listViews.get(SRODEK));

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.FLlay);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(countX/2, countX/2);

        params.leftMargin = listViews.get(SRODEK).getLeft() + ball.getCenterX();
        params.topMargin = listViews.get(SRODEK). getTop() + ball.getCenterY();

        pilka = ball;
        frameLayout.addView(pilka, params);


    }
    public void rysujBoisko()
    {
        int[] fieldID = {12, 13, 14, 15, 4, 5, 6, 17, 18, 19, 20, 31, 42, 53, 64, 75, 86, 97, 108, 119, 130, 129, 128, 127, 138, 137, 136, 125, 124, 123, 122, 111, 100, 89, 78, 67, 56, 45, 34, 23, 12};
        int[] notClickableField = {0, 1, 2, 3, 7, 8, 9, 10, 11, 21, 22, 32, 33, 43, 44, 54, 55, 65, 66, 76, 77, 87, 88, 98, 99, 109, 110, 120, 121, 131, 132, 133, 134, 135, 139, 140, 141, 142, 143};

        for(int i =0;i<fieldID.length - 1;i++)
            rysujLinie(listViews.get(fieldID[i]), listViews.get(fieldID[i+1]));

        for (int i=0;i < notClickableField.length - 1; i++)
            listViews.get(notClickableField[i]).setEnabled(false);
    }

}
