package test.pkantor.soccer1;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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

    float fx = 0;
    float fy = 0;
    float sx = 0;
    float sy = 0;
    
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
        //Button btn = (Button) findViewById(R.id.bWyczysc);
        //btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                clickWyczysc();
//            }
//        });

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
                //rysujPilke(listViews.get(listViews.size()/2)); TODO
                lay.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        //lay.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int height = displayMetrics.heightPixels;// - 60;
        int width = displayMetrics.widthPixels;
        countY = round(height/14);
        countX = round(width/11);
        srodekX = countX/2 - 1;
        for (int i = 1; i < 155; i++) { //countX*countY
            final ImageView iv = new ImageView(this);
            iv.setImageResource(R.drawable.pole);
            iv.setClickable(true);
            iv.setId(i - 1);
            iv.setAdjustViewBounds(true);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(countX, countX);
            params.leftMargin = left;
            params.topMargin = top;

            //params.
//            params.setGravity(Gravity.CENTER_HORIZONTAL);
//            params.columnSpec = GridLayout.spec(c);
//            params.rowSpec = GridLayout.spec(r);
//            iv.setLayoutParams(params);
            lay.addView(iv, params);
            listViews.add(iv);
            if ((i % 11 == 0) && (i != 1)) {
                top += countX;
                left = 0;
            } else left += countX;
////     TAKIE COS BYLO NA NECIE https://stackoverflow.com/questions/35318585/how-to-set-on-touch-listener-for-multiple-image-views
////            imageView.setOnTouchListener(this);
////            @Override
////            public boolean onTouch(View v, MotionEvent event) {
////                ImageView view = (ImageView) v;
////                switch (view.getId()){
////                    case R.id.car1: // example id
////                        switch (event.getAction()) {
////                            case MotionEvent.ACTION_DOWN:
////                                break;
////                            case MotionEvent.ACTION_MOVE:
////                                break;
////                            case MotionEvent.ACTION_UP:
////                                break;
////                            case MotionEvent.ACTION_CANCEL:
////                                break;
////                        }
////                        break;
////                    case R.id.car2: // example id
////                        switch (event.getAction()) {
////                            case MotionEvent.ACTION_DOWN:
////                                break;
////                            case MotionEvent.ACTION_MOVE:
////                                break;
////                            case MotionEvent.ACTION_UP:
////                                break;
////                            case MotionEvent.ACTION_CANCEL:
////                                break;
////                        }
////                        break;
////                }
////                return true;
////            }
//
            iv.setOnTouchListener(new View.OnTouchListener() { //1111111111111111111111111111111
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch(motionEvent.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            Log.d("DOWN","" + iv.getId());
                            if (game)
                                _destination = (ImageView) findViewById(iv.getId());
                            else
                            {
                                _source = (ImageView) findViewById(iv.getId());
                            }
                            game = !game;
//                            if (source != null)
//                                Log.d("SOURCE","" + source.getId());
//                            if (destination != null)
//                                Log.d("DEST","" + destination.getId());
                            return true;
                        case MotionEvent.ACTION_UP:
                            Log.d("UP","" + iv.getId());
                            if ((!game) && ((_destination == _lastDestination) || (_source == _lastDestination) || (_lastDestination == null)))
                            {
                                rysujLinie(_source, _destination);
                                _lastDestination = _destination;
                            }
                            else if ((_destination != _lastDestination) && (_source != _lastDestination))
                                Toast.makeText(getApplicationContext(), "Nie wcisnieto ostatniej pozycji", Toast.LENGTH_SHORT).show();

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

//        for (int i=40; i<listLinie.size(); i++)
//        {
//            iv = listLinie.get(i);
//            iv.setVisibility(View.GONE);
//            //frameLayout.removeView(image);
//        }
    }

    public void rysujLinie(ImageView source, ImageView destination) // TODO poprawic wyswietlanie linii
    {
        ImageView linia = new ImageView(this);

        linia.setImageResource(R.drawable.linia);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.FLlay);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(countX + 10, countX);
        //FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        fx = source.getX();
        fy = source.getY();
        sx = destination.getX();
        sy = destination.getY();

        if ((abs(fx-sx) <= countX) && (abs(fy-sy) <=countY) && ((fx - sx != 0) || (fy - sy != 0)))
        {
            if (fy == sy)
            {
                linia.setRotation(90);
                fx += countX/2;
                sx += countX/2;

            }
            else if (fx == sx)
            {
                linia.setRotation(0);
//                fx += countX/2;
                fy += countY/2 - 10;
//               // sx += countX/2;
                sy += countY/2 - 10 ;
            }
            else
            {
                if (((fx > sx) && (fy < sy)) || ((fx < sx) && (fy > sy)) )
                    linia.setRotation(45);
                else if (((fx < sx) && (fy < sy)) || ((fx > sx) && (fy > sy)))
                    linia.setRotation(135);
                else
                    Toast.makeText(getApplicationContext(),"Nieznany ruch", Toast.LENGTH_SHORT).show();

                int skos = (int)sqrt(pow(countX, 2) + pow(countX, 2));
                params = new FrameLayout.LayoutParams(10, skos);
                fx += countX - 4;
                sx += countX - 4;
                fy += countX/2  - 20;
                sy += countX/2 - 20;

                if (((fx > sx) && (fy > sy)) || ((fx < sx) && (fy > sy)))
                {
//                    fy += 8;
//                    sy += 8;
                }

            }

            params.leftMargin = ((int)fx < (int)sx) ? (int)fx : (int)sx;
            params.topMargin = ((int)fy < (int)sy) ? (int)fy : (int)sy;
            listLinie.add(linia);
            lay.addView(linia, params);
            //rysujPilke(destination);
            lay.invalidate();
        }
        else
           Toast.makeText(getApplicationContext(),"Zbyt dlugi ruch", Toast.LENGTH_SHORT).show();

        Log.d("COORDS","fx: " + fx + " fy: " + fy + " sx: " + sx + " sy: " + sy);
    }

    public void rysujPilke()
    {
        int SRODEK = 71;

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.FLlay);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(countX/2, countX/2);

        int centerX = listViews.get(SRODEK).getWidth() / 3;
        int centerY = listViews.get(SRODEK).getHeight() / 3;
        params.leftMargin = listViews.get(SRODEK).getLeft() + centerX;
        params.topMargin = listViews.get(SRODEK). getTop() + centerY;
        ImageView pilka = new ImageView(this);
        pilka.setAdjustViewBounds(true);
        pilka.setImageResource(R.drawable.ball);
        frameLayout.addView(pilka, params);


    }
    public void rysujBoisko()
    {
        int[] fieldID = {12, 13, 14, 15, 4, 5, 6, 17, 18, 19, 20, 31, 42, 53, 64, 75, 86, 97, 108, 119, 130, 129, 128, 127, 138, 137, 136, 125, 124, 123, 122, 111, 100, 89, 78, 67, 56, 45, 34, 23, 12};
       // FrameLayout fr = (FrameLayout) findViewById(R.id.FLlay);
        for(int i =0;i<fieldID.length - 1;i++)
        {
            Log.e("Przed", "pobranie");
            rysujLinie(listViews.get(fieldID[i]), listViews.get(fieldID[i+1]));
           // listViews.get(fieldID[i]).performClick();
           // listViews.get(fieldID[i]).setX(1000);
            Log.e("Po", "wcisnieto");

//            iv.performClick();
        }

    }

}
