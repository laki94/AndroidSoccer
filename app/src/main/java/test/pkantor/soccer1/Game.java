package test.pkantor.soccer1;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.round;
import static test.pkantor.soccer1.R.layout.abc_list_menu_item_radio;
import static test.pkantor.soccer1.R.layout.activity_game;


public class Game extends AppCompatActivity {

    public static final int POLE_WYSOKOSC = 65;
    public static final int POLE_SZEROKOSC = 65;


    FrameLayout lay;

    float x,y,dx,dy;
    FrameLayout.LayoutParams flParams;

    int progressStatus = 0;
    private Handler handler = new Handler();

    int i = 0;
    int left = 0;
    int top = 0;
    int srodekX;
    int srodekY;
    int countX;
    int countY;
    boolean playerAccepted = true;
    boolean canDelete = false;
    ImageView _source;
    ImageView _destination;
    ImageView _lastDestination;
    ImageView pilka;
    ScaleGestureDetector scaleGestureDetector;
    List<ImageView> listViews;
    List<ImageView> listLinie;
    List<Field> listFields;
    private float scale = 1f;
    boolean game = false;
    Player player1;
    Player player2;
    Button acceptMove;


    @Override
    public void onBackPressed()
    {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_game);

        TextView g1 = (TextView) findViewById(R.id.tvPlayer1);
        TextView g2 = (TextView) findViewById(R.id.tvPlayer2);


        acceptMove = (Button) findViewById(R.id.acceptMove);
        player1 = new Player();
        player2 = new Player();
        int c = 0;
        int r = 0;
        listViews = new ArrayList<>();
        listLinie = new ArrayList<>();
        listFields = new ArrayList<>();
        lay = (FrameLayout) findViewById(R.id.FLlay);
        lay.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                startGame();
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

            //pol.setScaleType(ImageView.ScaleType.CENTER_CROP); // TODO moze sie przydac
            listFields.add(pol);
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
                            if (playerAccepted)
                                _destination = (ImageView) findViewById(pol.getId());
                            else
                            {
                                _destination = (ImageView) findViewById(pol.getId());
                            }

                            return true;
                        case MotionEvent.ACTION_UP:
                            Log.d("UP","" + pol.getId());
                            if (_source != _destination)
                            //if ((!game) && ((_destination == _lastDestination) || (_source == _lastDestination) || (_lastDestination == null)))
                            {
                                if (rysujLinie(_source, _destination))
                                {
                                    canDelete = true;
                                    playerAccepted = false;
                                    pilka.setX(_destination.getLeft() + _destination.getWidth()/4);
                                    pilka.setY(_destination.getTop() + _destination.getHeight()/4);
                                    pilka.bringToFront();
                                    pilka.invalidate();
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

        Bundle extras = getIntent().getExtras();

        if (extras != null)
        {
            player1.setName(extras.getString("p1Name"));
            player2.setName(extras.getString("p2Name"));
        }
        else
        {
            player1.setName("Gracz 1");
            player2.setName("Gracz 2");
        }

        g1.bringToFront();
        g2.bringToFront();
        g1.setWidth(countX * 4);
        g2.setWidth(countX * 4);
        g1.setTextSize(TypedValue.COMPLEX_UNIT_SP, countX/ 4);
        g2.setTextSize(TypedValue.COMPLEX_UNIT_SP, countX / 4);
        g1.setText(player1.getName());
        g2.setText(player2.getName());
        g1.setX(countX * 7); //listFields.get(135).getX());
        g1.setY((countX *  12) - countX/2); //listFields.get(135).getY());
        g2.setX(countX * 7); //listFields.get(3).getX());
        g2.setY((countX * 1) - countX/2) ; //listFields.get(3).getY());
       // g1.setTranslationX(listFields.get(135).getX());
        //g1.setTranslationY(listFields.get(135).getY());
//        g2.setLeft(listFields.get(3).getLeft());
//        g2.setTop(listFields.get(3).getTop());
//        g1.setLeft(listFields.get(135).getLeft());
//        g1.setTop(listFields.get(135).getTop());
    }

    public void startCounting()
    {
//        ObjectAnimator colorFade = ObjectAnimator.ofObject(acceptMove, "backgroundColor", new ArgbEvaluator(), Color.argb(255,255,255,255), 0xff000000);
//        colorFade.setDuration(10000);
//        colorFade.start();

//        final ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(progressStatus < 100)
//                {
//                    progressStatus++;
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            pb.setProgress(progressStatus);
//                        }
//                    });
//                    try
//                    {
//                        Thread.sleep(100);
//                    }catch (InterruptedException e) {e.printStackTrace();}
//                }
//            }
//        }).start();
    }

    public int countPossibleMoves()
    {
        int[][] dstShots = listFields.get(_destination.getId()).getShots();
        int possibleMoves = 0;

        for (int i=0;i<dstShots.length;i++) {
            for (int j = 0; j < dstShots.length; j++)
                if (dstShots[i][j] == 0)
                    possibleMoves++;
        }
        return possibleMoves;
    }

    public void deleteLastLine(ImageView imageView)
    {
        if (canDelete)
            if (listLinie.size() > 40)
            {
                if (!playerAccepted)
                {
                    listLinie.get(listLinie.size() - 1).setVisibility(View.GONE);
                    listLinie.remove(listLinie.size() - 1);
//                    pilka.setX(imageView.getLeft() + imageView.getWidth()/4);
//                    pilka.setY(imageView.getTop() + imageView.getHeight()/4);
//                    pilka.bringToFront();
//                    pilka.invalidate();
                    canDelete = false;
                    playerAccepted = true;
                }
            }
    }

    public boolean clickAcceptMove(View v)
    {
        if (playerAccepted)
            Toast.makeText(this,"Najpierw należy wykonać ruch", Toast.LENGTH_LONG).show();
        else
        {
            checkIfGameOver(countPossibleMoves(), _destination.getId());

            playerAccepted = true;

            //listFields.get(_source.getId()).setShots(listFields.get(_source.getId()).getLastShot());
           // listFields.get(_destination.getId()).setShots(listFields.get(_destination.getId()).getLastShot());

            int[][] srcShots = listFields.get(_source.getId()).getShots();
            int[][] dstShots = listFields.get(_destination.getId()).getShots();
            int[][] srcLastShot = listFields.get(_source.getId()).getLastShot();
            int[][] dstLastShot = listFields.get(_destination.getId()).getLastShot();

            for (int i = 0; i<srcShots.length;i++)
                for(int j = 0; j<srcShots.length;j++)
                {
                    if (srcLastShot[i][j] == 1)
                        srcShots[i][j] = 1;
                    if (dstLastShot[i][j] == 1)
                        dstShots[i][j] = 1;
                }


            _source = _destination;


            if (player1.isAdditionalMove())
            {
                player1.setMove(true);
                player2.setMove(!player1.isMove());
            }
            else if (player2.isAdditionalMove())
            {
                player2.setMove(true);
                player1.setMove(!player2.isMove());
            }
            else
            {
                player1.setMove(player2.isMove());
                player2.setMove(!player1.isMove());
            }
            whoMoves();
        }

        return playerAccepted;
    }

    public void startGame()
    {
        rysujBoisko();
        rysujPilke();
        player1.setMove(true);
        whoMoves();
    }
    public void whoMoves()
    {
        TextView tv = (TextView) findViewById(R.id.textView2);
        if (player1.isMove())
            tv.setText("Ruch gracza, " + player1.getName());
        else
            tv.setText("Ruch gracza, " + player2.getName());
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

    public boolean checkIfShotPossible(ImageView source, ImageView destination) // TODO przerobic cale na switch case
    {
        int[][] srcShots = listFields.get(source.getId()).getShots();
        int[][] dstShots = listFields.get(destination.getId()).getShots();

        int[][] srcLastShot = new int[3][3];
        int[][] dstLastShot = new int[3][3];
        for (int i=0;i<srcShots.length;i++)
            for(int j=0;j<srcShots.length;j++)
                srcLastShot[i][j] = dstLastShot[i][j] = 0;
//        int[][] srcLastShot = listFields.get(source.getId()).getLastShot();
//        int[][] dstLastShot = listFields.get(destination.getId()).getLastShot();

        int possibleMoves = 0;
        boolean additionalMove = false;

        for (int i=0;i<dstShots.length;i++)
        {
            for(int j=0;j<dstShots.length;j++)
                if (dstShots[i][j] == 1)
                    additionalMove = true;
                else
                    possibleMoves++;
//                if (additionalMove) break;
        }

//        for (int i=0;i<srcShots.length;i++)
//            for (int j=0;j<srcShots.length;j++)
//                if (srcShots[i][j] == 0)
//                    possibleMoves ++;

        if (possibleMoves < 2) return false;

        if (Math.abs(destination.getId() - source.getId()) == 1) // --
        {
            if (destination.getId() > source.getId())
            {
                if ((srcShots[1][2] == 1) || (dstShots[1][0] == 1))
                    return false;
                else
                {
                    srcLastShot[1][2] = 1;
                    dstLastShot[1][0] = 1;
                }
            }
            else
            {
                if ((srcShots[1][0] == 1) || (dstShots[1][2] == 1))
                    return false;
                else
                {
                    srcLastShot[1][0] = 1;
                    dstLastShot[1][2] = 1;
                }
            }
        }
        else if (Math.abs(destination.getId() - source.getId()) == 11) // |
        {
            if (destination.getId() > source.getId()) {
                if ((srcShots[2][1] == 1) || (dstShots[0][1] == 1))
                    return false;
                else {
                    srcLastShot[2][1] = 1;
                    dstLastShot[0][1] = 1;
                }
            } else {
                if ((srcShots[0][1] == 1) || (dstShots[2][1] == 1))
                    return false;
                else {
                    srcLastShot[0][1] = 1;
                    dstLastShot[2][1] = 1;
                }
            }
        }
        else // \ /
        {
            switch(destination.getId() - source.getId())
            {
                case -12:
                    if ((srcShots[0][0] == 1) || (dstShots[2][2] == 1))
                        return false;
                    srcLastShot[0][0] = 1;
                    dstLastShot[2][2] = 1;
                    break;
                case -10:
                    if ((srcShots[0][2] == 1) || (dstShots[2][0] == 1))
                        return false;
                    srcLastShot[0][2] = 1;
                    dstLastShot[2][0] = 1;
                    break;
                case 10:
                    if ((srcLastShot[2][0] == 1) || (dstLastShot[0][2] == 1))
                        return false;
                    srcLastShot[2][0] = 1;
                    dstLastShot[0][2] = 1;
                    break;
                case 12:
                    if ((srcShots[2][2] == 1) || (dstShots[0][0] == 1))
                        return false;
                    srcLastShot[2][2] = 1;
                    dstLastShot[0][0] = 1;
                    break;
                default:
                    return false;
            }
        }

        listFields.get(source.getId()).setLastShot(srcLastShot);
        listFields.get(destination.getId()).setLastShot(dstLastShot);

        if (player1.isMove())
            player1.setAdditionalMove(additionalMove);
        else
            player2.setAdditionalMove(additionalMove);

        Log.d("ShotsSrc", String.valueOf(srcShots[0][0]) + " " + String.valueOf(srcShots[0][1]) + " " +String.valueOf(srcShots[0][2]) + " " +String.valueOf(srcShots[1][0]) + " " +String.valueOf(srcShots[1][1]) + " " +String.valueOf(srcShots[1][2]) + " " +String.valueOf(srcShots[2][0]) + " " +String.valueOf(srcShots[2][1]) + " " +String.valueOf(srcShots[2][2]));
        Log.d("ShotsDst", String.valueOf(dstShots[0][0]) + " " + String.valueOf(dstShots[0][1]) + " " +String.valueOf(dstShots[0][2]) + " " +String.valueOf(dstShots[1][0]) + " " +String.valueOf(dstShots[1][1]) + " " +String.valueOf(dstShots[1][2]) + " " +String.valueOf(dstShots[2][0]) + " " +String.valueOf(dstShots[2][1]) + " " +String.valueOf(dstShots[2][2]));


        //checkIfGameOver(possibleMoves, destination.getId());

        return true;
    }

    public void checkIfGameOver(int possibleMoves, int fieldId) // TODO poprawic wyswietlanie bramek bo jakies cuda sie dziejo
    {                                                               // TODO dodac przycisk akceptujacy ruch
        int[] p2Goal  = {4, 5, 6};
        int[] p1Goal = {138, 137, 136};

        boolean isGoal = false;
        boolean isOwnGoal = false;

            for (int i: p2Goal)
                if (i == fieldId)
                {
                    if (player2.isMove())
                        isOwnGoal = true;
                    else
                        isGoal = true;
                    break;
                }

            if (!isGoal)
                for (int i: p1Goal)
                    if (i == fieldId)
                    {
                        if (player1.isMove())
                            isOwnGoal = true;
                        else
                            isGoal = true;
                        break;
                    }



        Intent intent = new Intent(this, GameOver.class);
//        intent.putExtra("movep1", player1.isMove());
//        intent.putExtra("movep2", player2.isMove());
//        intent.putExtra("isGoal", isGoal);
//        intent.putExtra("isOwnGoal", isOwnGoal);

        if (player1.isMove())
        {
            if (isOwnGoal)
            {
               // Toast.makeText(this, "Bramka samobójcza, wygrywa Gracz 2", Toast.LENGTH_LONG).show();
                intent.putExtra("goal", 1);
                intent.putExtra("winner", player2.getName());
                startActivity(intent);
            }

            else if (isGoal)
            {
               // Toast.makeText(this, "Wygrywa Gracz 1", Toast.LENGTH_LONG).show();
                intent.putExtra("goal", 2);
                intent.putExtra("winner", player1.getName());
                startActivity(intent);
            }

        }
        else if (player2.isMove())
            if (isOwnGoal)
            {
               // Toast.makeText(this, "Bramka samobójcza, wygrywa Gracz 1", Toast.LENGTH_LONG).show();
                intent.putExtra("goal", 3);
                intent.putExtra("winner", player1.getName());
                startActivity(intent);
            }

            else if (isGoal)
            {
                //Toast.makeText(this, "Wygrywa Gracz 2", Toast.LENGTH_LONG).show();
                intent.putExtra("goal", 4);
                intent.putExtra("winner", player2.getName());
                startActivity(intent);
            }


        if (possibleMoves <= 2)
        {
            final TextView tv = (TextView) findViewById(R.id.textView2);
            if (player1.isMove())
            {

//                Toast.makeText(this, "Wygrywa Gracz 2", Toast.LENGTH_LONG).show();
               // tv.setText("Wygrywa Gracz 2");
                intent.putExtra("goal", 4);
                intent.putExtra("winner", player2.getName());
                startActivity(intent);
            }else
            {

//                Toast.makeText(this, "Wygrywa Gracz 1", Toast.LENGTH_LONG).show();
               // tv.setText("Wygrywa Gracz 1");
                intent.putExtra("goal", 2);
                intent.putExtra("winner", player1.getName());
                startActivity(intent);
            }



            //this.finish();
            // return false; // TODO activity konczace gre !!!
        }
    }

    public boolean rysujLinie(ImageView source, ImageView destination) // TODO poprawic wyswietlanie linii
    {
        Line line = new Line(this);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.FLlay);
        //FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(countX + 15, _source.getWidth()); // szer ; dl
        if (line.createLine(source, destination) && (checkIfShotPossible(source, destination)))
        {
            if (canDelete)
                deleteLastLine(_destination);
            // params.leftMargin = ((int) line.getFx() < (int) line.getSx()) ? (int) line.getFx() : (int) line.getSx();
           // params.topMargin = ((int) line.getFy() < (int) line.getSy()) ? (int) line.getFy(): (int) line.getSy();
            listLinie.add(line);
            lay.addView(line, line.getParams());
            lay.invalidate();

           // Log.d("COORDS", "fx: " + line.getFx() + " fy: " + line.getFy() + " sx: " + line.getSx() + " sy: " + line.getSy());
//            Log.d("ShotsSrc", String.valueOf(srcShots[0][0]) + " " + String.valueOf(srcShots[0][1]) + " " +String.valueOf(srcShots[0][2]) + " " +String.valueOf(srcShots[1][0]) + " " +String.valueOf(srcShots[1][1]) + " " +String.valueOf(srcShots[1][2]) + " " +String.valueOf(srcShots[2][0]) + " " +String.valueOf(srcShots[2][1]) + " " +String.valueOf(srcShots[2][2]));
//            Log.d("ShotsDst", String.valueOf(dstShots[0][0]) + " " + String.valueOf(dstShots[0][1]) + " " +String.valueOf(dstShots[0][2]) + " " +String.valueOf(dstShots[1][0]) + " " +String.valueOf(dstShots[1][1]) + " " +String.valueOf(dstShots[1][2]) + " " +String.valueOf(dstShots[2][0]) + " " +String.valueOf(dstShots[2][1]) + " " +String.valueOf(dstShots[2][2]));

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

        int[] rightSide = {20, 31, 42, 53, 64, 75, 86, 97, 108, 119, 130};
        int[] leftSide = {122, 111, 100, 89, 78, 67, 56, 45, 34, 23, 12};
        int[] upSide = {12, 13, 14, 15, 17, 18, 19, 20, 4, 5, 6};
        int[] downSide = {130, 129, 128, 127, 125, 124, 123, 122, 138, 137, 136};

        int[] notClickableField = {0, 1, 2, 3, 7, 8, 9, 10, 11, 21, 22, 32, 33, 43, 44, 54, 55, 65, 66, 76, 77, 87, 88, 98, 99, 109, 110, 120, 121, 131, 132, 133, 134, 135, 139, 140, 141, 142, 143};

        for(int i = 0; i < fieldID.length - 1; i++)
            rysujLinie(listViews.get(fieldID[i]), listViews.get(fieldID[i+1]));

        for (int i=0;i < notClickableField.length - 1; i++)
            listViews.get(notClickableField[i]).setEnabled(false);

        for (int i: rightSide)
        {
            int[][] tmpShots = listFields.get(i).getShots();
            tmpShots[0][2] = tmpShots[1][2] = tmpShots[2][2] = 1;
            listFields.get(i).setShots(tmpShots);
        }

        for (int i:leftSide)
        {
            int[][] tmpShots = listFields.get(i).getShots();
            tmpShots[0][0] = tmpShots[1][0] = tmpShots[2][0] = 1;
            listFields.get(i).setShots(tmpShots);
        }

        for (int i: upSide)
        {
            int[][] tmpShots = listFields.get(i).getShots();
            if (i == 15)
                tmpShots[0][0] = tmpShots[0][1] = 1;
            else if (i == 17)
                tmpShots[0][1] = tmpShots[0][2] = 1;
            else
                tmpShots[0][0] = tmpShots[0][1] = tmpShots[0][2] = 1;
            listFields.get(i).setShots(tmpShots);
        }

        for (int i: downSide)
        {
            int[][] tmpShots = listFields.get(i).getShots();
            if (i == 125)
                tmpShots[2][0] = tmpShots[2][1] = 1;
            else if (i == 127)
                tmpShots[2][1] = tmpShots[2][2] = 1;
            else
                tmpShots[2][0] = tmpShots[2][1] = tmpShots[2][2] = 1;
            listFields.get(i).setShots(tmpShots);
        }
    }

}
