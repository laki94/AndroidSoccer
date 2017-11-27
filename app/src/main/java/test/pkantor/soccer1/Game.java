package test.pkantor.soccer1;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.round;
import static test.pkantor.soccer1.R.layout.activity_game;

public class Game extends AppCompatActivity {

    FrameLayout lay;

    private Toast toast = null;

    int left = 0;
    int top = 0;
    int countX;
    int countY;
    int goalPointsToWin;
    boolean playerAccepted = true;
    boolean canDelete = false;
    boolean doShowAcceptButton = true;
    boolean doShowPlayerNames = true;
    boolean newGame = false;

    ImageView _source;
    ImageView _destination;
    ImageView _lastDestination;
    ImageView pilka;

    List<ImageView> listViews;
    List<ImageView> listLinie;
    List<Field> listFields;

    Player player1;
    Player player2;

    Button acceptMove;
    CheckBox cbAcceptMove = null;
    CheckBox cbShowNames = null;
    android.support.v7.widget.Toolbar tbHeader;
    TextView tvScore;

    Resources res;

    @Override
    public void onBackPressed()
    {
        DrawerLayout dr = (DrawerLayout) findViewById(R.id.layDrawer);
        NavigationView nv = (NavigationView) dr.findViewById(R.id.navView);
        if (dr.isDrawerOpen(nv))
            dr.closeDrawer(nv);
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View mView = getLayoutInflater().inflate(R.layout.activity_dialog_exit, null);
            builder.setView(mView);
            final AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);

            Button bYes = (Button) mView.findViewById(R.id.bYes);
            Button bNo = (Button) mView.findViewById(R.id.bNo);

            bYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    finish();
                    System.exit(0);
                }
            });
            bNo.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    public void prepareDrawer()
    {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navView);
        navigationView.bringToFront();
        android.view.Menu menu = navigationView.getMenu();

        final MenuItem iAcceptMove = menu.findItem(R.id.nav_AcceptMove);
        final MenuItem iShowNames = menu.findItem(R.id.nav_ShowPlayers);

        iAcceptMove.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                CheckBox cb = (CheckBox) item.getActionView();
                cb.setChecked(!cb.isChecked());
                return false;
            }
        });

        iShowNames.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                CheckBox cb = (CheckBox) item.getActionView();
                cb.setChecked(!cb.isChecked());
                return false;
            }
        });

        cbAcceptMove = (CheckBox) iAcceptMove.getActionView();
        cbShowNames = (CheckBox) iShowNames.getActionView();

        cbAcceptMove.setChecked(true);
        cbShowNames.setChecked(true);

        cbAcceptMove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                doShowAcceptButton = isChecked;
                if (!isChecked)
                    acceptMove.setVisibility(View.INVISIBLE);
                else
                    acceptMove.setVisibility(View.VISIBLE);
            }
        });

        cbShowNames.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                doShowPlayerNames = isChecked;
                TextView tvPlayer1 = (TextView) findViewById(R.id.tvPlayer1);
                TextView tvPlayer2 = (TextView) findViewById(R.id.tvPlayer2);

                if (!isChecked)
                {
                    tvPlayer1.setVisibility(View.INVISIBLE);
                    tvPlayer2.setVisibility(View.INVISIBLE);
                }

                else
                {
                    tvPlayer1.setVisibility(View.VISIBLE);
                    tvPlayer2.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(activity_game);

        TextView g1 = (TextView) findViewById(R.id.tvPlayer1);
        TextView g2 = (TextView) findViewById(R.id.tvPlayer2);
        tbHeader = (android.support.v7.widget.Toolbar) findViewById(R.id.tbGame);
        tvScore = (TextView) tbHeader.findViewById(R.id.tv_tbTitle);

        res = getResources();

        prepareDrawer();

//        cbAcceptMove = (CheckBox) header.findViewById(R.id.nav_AcceptMove);

//
//        cbAcceptMove.setOnClickListener(new View.OnClickListener()
//        {
//            public void onClick(View v)
//            {
//                cbAcceptMove.setChecked(!cbAcceptMove.isChecked());
//            }
//        });
        //CheckBox cb = (CheckBox) navigationView.getMenu().findItem(R.id.nav_item1).getActionView().findViewById(R.id.checkBox111);
        //cb.setChecked(false);
        //cb.setChecked(true);



        acceptMove = (Button) findViewById(R.id.acceptMove);
        player1 = new Player();
        player2 = new Player();

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

        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        countY = round(height/13);
        countX = round(width/11);

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

            pol.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch(motionEvent.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            Log.d("DOWN","" + pol.getId());

                            _lastDestination = (ImageView) findViewById(pol.getId());

                            if (!tryToCreateLine(_source, _lastDestination))
                            {
                                if (!checkIfShotPossible(_source, _lastDestination))
                                {
                                    if (toast != null)
                                        toast.cancel();
                                    if (_lastDestination == _source)
                                        toast = Toast.makeText(getApplicationContext(), res.getString(R.string.pl_SelectNextFieldError), Toast.LENGTH_LONG);
                                    else
                                        toast = Toast.makeText(getApplicationContext(), res.getString(R.string.pl_TooLongPassError), Toast.LENGTH_LONG);
                                    toast.show();
                                    _lastDestination = _destination;
                                }
                            }
                            else
                            {
                                if (checkIfShotPossible(_source, _lastDestination))
                                {
                                    _destination = _lastDestination;
                                    if (drawLine(_source, _destination))
                                    {
                                        canDelete = true;
                                        playerAccepted = false;
                                        pilka.setX(_destination.getLeft() + _destination.getWidth()/4);
                                        pilka.setY(_destination.getTop() + _destination.getHeight()/4);
                                        pilka.bringToFront();
                                        pilka.invalidate();

                                        if (!doShowAcceptButton)
                                            acceptMove.performClick();

//                                        if (newGame)
//                                            startNewGame();
                                    }
                                }

                                else
                                {
                                    if (toast != null)
                                        toast.cancel();
                                    toast = Toast.makeText(getApplicationContext(), res.getString(R.string.pl_InvalidDirectionError), Toast.LENGTH_LONG);
                                    toast.show();
                                }
                            }



                            return true;
                        case MotionEvent.ACTION_UP:
                            Log.d("UP","" + pol.getId());
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
            goalPointsToWin = extras.getInt("goalPoints");
        }
        else
        {
//            if (player1.getName().equals(""))
                player1.setName(res.getString(R.string.pl_DefaultPlayer1));
//            if (player2.getName().equals(""))
                player2.setName(res.getString(R.string.pl_DefaultPlayer2));

                goalPointsToWin = 1;
        }

        g1.bringToFront();
        g2.bringToFront();
        g1.setWidth(countX * 4);
        g2.setWidth(countX * 4);

        g1.setTextSize(TypedValue.COMPLEX_UNIT_SP, (countX / res.getDisplayMetrics().scaledDensity) / 2);
        g2.setTextSize(TypedValue.COMPLEX_UNIT_SP, (countX / res.getDisplayMetrics().scaledDensity) / 2);

        g1.setText(player1.getName());
        g2.setText(player2.getName());
        g1.setX((countX * 7) - countX / 3);
        g1.setY((countX *  12) - countX / 3);
        g2.setX((countX * 7) - countX / 3);
        g2.setY((countX * 1) - countX / 3);

        setToolbarScore();
        setSupportActionBar(tbHeader);
        //tbSubtitle.setText(tb.getSubtitle());
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //tb.setSubtitle("0 0");


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        if (menuItem.getItemId() == android.R.id.home)
        {
            NavigationView nv = (NavigationView) findViewById(R.id.navView);
            DrawerLayout dr = (DrawerLayout) findViewById(R.id.layDrawer);
            if (dr.isDrawerOpen(nv))
                dr.closeDrawer(nv);
            else
                dr.openDrawer(nv);
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void setToolbarScore()
    {
        String textScore = res.getString(R.string.score, player1.getName(), player1.getPoints(), player2.getPoints(), player2.getName());
        tvScore.setTextSize(TypedValue.COMPLEX_UNIT_SP, ((countX / res.getDisplayMetrics().scaledDensity) / 2) - (textScore.length() / 10)) ;
        tvScore.setText(textScore);
    }

    public void startNewGame(Player winner, Player loser)
    {
        for (Field field: listFields)
            field.initializeShots();

        blockMoveOutsideField();

        while (listLinie.size() > 40)
        {
            listLinie.get(listLinie.size() - 1).setVisibility(View.INVISIBLE);
            listLinie.remove(listLinie.size() - 1);
        }
        _source = listViews.get(71);
        pilka.setX(_source.getLeft() + _source.getWidth() / 4);
        pilka.setY(_source.getTop() + _source.getHeight() / 4);

        winner.setMove(false);
        loser.setMove(true);

        setToolbarScore();

    }

    public boolean tryToCreateLine(ImageView src, ImageView dst)
    {
        Line line = new Line(this);
        return line.canCreateLine(src, dst);
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

    public void deleteLastLine()
    {
        if (canDelete)
            if (listLinie.size() > 40)
            {
                if (!playerAccepted)
                {
                    listLinie.get(listLinie.size() - 1).setVisibility(View.GONE);
                    listLinie.remove(listLinie.size() - 1);

                    playerAccepted = true;
                }
            }
    }

    public synchronized boolean clickAcceptMove(View v)
    {
        if (playerAccepted)
        {
            if (toast != null)
                toast.cancel();
           toast = Toast.makeText(this,res.getString(R.string.pl_MakeMoveFirstError), Toast.LENGTH_SHORT);
           toast.show();
        }

        else
        {
            if ((_source == null) || (_destination == null))
                return false;

            int[][] srcShots = listFields.get(_source.getId()).getShots();
            int[][] dstShots = listFields.get(_destination.getId()).getShots();
            int[][] srcLastShot = listFields.get(_source.getId()).getLastShot();
            int[][] dstLastShot = listFields.get(_destination.getId()).getLastShot();

            _source = _destination;

            if (toast != null)
                toast.cancel();

            playerAccepted = true;

            newGame = checkIfGameOver(countPossibleMoves(), _destination.getId());

            if (!newGame)
            {
                for (int i = 0; i<srcShots.length;i++)
                    for(int j = 0; j<srcShots.length;j++)
                    {
                        if (srcLastShot[i][j] == 1)
                            srcShots[i][j] = 1;
                        if (dstLastShot[i][j] == 1)
                            dstShots[i][j] = 1;
                    }

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
        }
        return playerAccepted;
    }

    public void startGame()
    {
        drawPlayField();
        drawBall();
        player1.setMove(true);
        whoMoves();
    }
    public void whoMoves()
    {
        TextView tv = (TextView) findViewById(R.id.tvWhoMoves);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, (countX / res.getDisplayMetrics().scaledDensity) / 2);
        if (player1.isMove())
            tv.setText("Ruch gracza, " + player1.getName());
        else
            tv.setText("Ruch gracza, " + player2.getName());
    }

    public boolean checkIfShotPossible(ImageView source, ImageView destination)
    {

        int[][] srcShots = listFields.get(source.getId()).getShots();
        int[][] dstShots = listFields.get(destination.getId()).getShots();

        int[][] srcLastShot = new int[3][3];
        int[][] dstLastShot = new int[3][3];
        for (int i=0;i<srcShots.length;i++)
            for(int j=0;j<srcShots.length;j++)
                srcLastShot[i][j] = dstLastShot[i][j] = 0;

        int possibleMoves = 0;
        boolean additionalMove = false;

        for (int i=0;i<dstShots.length;i++)
        {
            for(int j=0;j<dstShots.length;j++)
                if (dstShots[i][j] == 1)
                    additionalMove = true;
                else
                    possibleMoves++;
        }

        if (possibleMoves < 2) return false;


        switch(destination.getId() - source.getId())
        {
            case -1:
                if ((srcShots[1][0] == 1) || (dstShots[1][2] == 1))
                    return false;
                else
                {
                    srcLastShot[1][0] = 1;
                    dstLastShot[1][2] = 1;
                }
                break;
            case 1:
                if ((srcShots[1][2] == 1) || (dstShots[1][0] == 1))
                    return false;
                else
                {
                    srcLastShot[1][2] = 1;
                    dstLastShot[1][0] = 1;
                }
                break;
            case -11:
                if ((srcShots[0][1] == 1) || (dstShots[2][1] == 1))
                    return false;
                else
                {
                    srcLastShot[0][1] = 1;
                    dstLastShot[2][1] = 1;
                }
                break;
            case 11:
                if ((srcShots[2][1] == 1) || (dstShots[0][1] == 1))
                    return false;
                else
                {
                    srcLastShot[2][1] = 1;
                    dstLastShot[0][1] = 1;
                }
                break;
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
                if ((srcShots[2][0] == 1) || (dstShots[0][2] == 1))
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

        listFields.get(source.getId()).setLastShot(srcLastShot);
        listFields.get(destination.getId()).setLastShot(dstLastShot);

        if (player1.isMove())
            player1.setAdditionalMove(additionalMove);
        else
            player2.setAdditionalMove(additionalMove);

        Log.d("ShotsSrc", String.valueOf(srcShots[0][0]) + " " + String.valueOf(srcShots[0][1]) + " " +String.valueOf(srcShots[0][2]) + " " +String.valueOf(srcShots[1][0]) + " " +String.valueOf(srcShots[1][1]) + " " +String.valueOf(srcShots[1][2]) + " " +String.valueOf(srcShots[2][0]) + " " +String.valueOf(srcShots[2][1]) + " " +String.valueOf(srcShots[2][2]));
        Log.d("ShotsDst", String.valueOf(dstShots[0][0]) + " " + String.valueOf(dstShots[0][1]) + " " +String.valueOf(dstShots[0][2]) + " " +String.valueOf(dstShots[1][0]) + " " +String.valueOf(dstShots[1][1]) + " " +String.valueOf(dstShots[1][2]) + " " +String.valueOf(dstShots[2][0]) + " " +String.valueOf(dstShots[2][1]) + " " +String.valueOf(dstShots[2][2]));

        return true;
    }

    public boolean checkIfGameOver(int possibleMoves, int fieldId)
    {
        int[] p2Goal  = {4, 5, 6};
        int[] p1Goal = {138, 137, 136};

        boolean isGoal = false;
        boolean isOwnGoal = false;

            for (int i: p2Goal)
                if (i == fieldId)
                {
                    if (player2.isMove())
                    {
                        isOwnGoal = true;
//                        player1.addPoint();
                    }

                    else
                    {
                        isGoal = true;
//                        player2.addPoint();
                    }
                    player1.addPoint();
                    break;
                }

            if (!isGoal)
                for (int i: p1Goal)
                    if (i == fieldId)
                    {
                        if (player1.isMove())
                        {
                            isOwnGoal = true;
//                            player2.addPoint();
                        }
                        else
                        {
                            isGoal = true;
//                            player1.addPoint();
                        }
                        player2.addPoint();
                        break;
                    }



        Intent intent = new Intent(this, GameOver.class);

        if ((isGoal) || (isOwnGoal))
            if (player1.getPoints() == goalPointsToWin)
            {
                //intent.putExtra("goal", 0);
                intent.putExtra("winner", player1.getName());
                startActivity(intent);
            }
            else if (player2.getPoints() == goalPointsToWin)
            {
               // intent.putExtra("goal", 1);
                intent.putExtra("winner", player2.getName());
                startActivity(intent);
            }

        if (player1.isMove())
        {
            if (isOwnGoal)
            {
                startNewGame(player2, player1);
                return true;
            }

            else if (isGoal)
            {
                startNewGame(player1, player2);
                return true;
            }

        }
        else if (player2.isMove())
            if (isOwnGoal)
            {
                startNewGame(player1, player2);
                return true;
            }
            else if (isGoal)
            {
                startNewGame(player2, player1);
                return true;
            }

        if (possibleMoves <= 2)
        {
            if (player1.isMove())
            {
                startNewGame(player2, player1);
                return true;
            }
            else
            {
                startNewGame(player1, player2);
                return true;
            }
        }

        return false;
    }

    public boolean drawLine(ImageView source, ImageView destination) // TODO wyswietlanie linii na innej rozdzialce,
    {
        Line line = new Line(this);

        if ((source == destination) || (source == null) || (destination == null))
            return false;

        if (line.createLine(source, destination) && (checkIfShotPossible(source, destination)))
        {
            if (canDelete)
            {
                deleteLastLine();
                canDelete = false;
            }

            listLinie.add(line);
            lay.addView(line, line.getParams());
            lay.invalidate();

            return true;
        }
        else
            return false;
    }

    public void drawBall()
    {
        int MIDDLE = 71;
        Ball ball = new Ball(this);
        ball.createBall(listViews.get(MIDDLE));

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.FLlay);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(countX/2, countX/2);

        params.leftMargin = listViews.get(MIDDLE).getLeft() + ball.getCenterX();
        params.topMargin = listViews.get(MIDDLE). getTop() + ball.getCenterY();

        pilka = ball;
        frameLayout.addView(pilka, params);


    }

    public void blockMoveOutsideField()
    {
        int[] rightSide = {20, 31, 42, 53, 64, 75, 86, 97, 108, 119, 130};
        int[] leftSide = {122, 111, 100, 89, 78, 67, 56, 45, 34, 23, 12};
        int[] upSide = {12, 13, 14, 15, 17, 18, 19, 20, 4, 5, 6};
        int[] downSide = {130, 129, 128, 127, 125, 124, 123, 122, 138, 137, 136};

        int[] notClickableField = {0, 1, 2, 3, 7, 8, 9, 10, 11, 21, 22, 32, 33, 43, 44, 54, 55, 65, 66, 76, 77, 87, 88, 98, 99, 109, 110, 120, 121, 131, 132, 133, 134, 135, 139, 140, 141, 142, 143};

        for (int i=0;i < notClickableField.length - 1; i++)
            listViews.get(notClickableField[i]).setEnabled(false);

        for (int i: rightSide)
        {
            int[][] tmpShots = listFields.get(i).getShots();
            tmpShots[0][2] = tmpShots[1][2] = tmpShots[2][2] = tmpShots[0][1] = tmpShots[2][1] = 1;
            listFields.get(i).setShots(tmpShots);
        }

        for (int i:leftSide)
        {
            int[][] tmpShots = listFields.get(i).getShots();
            tmpShots[0][0] = tmpShots[1][0] = tmpShots[2][0] = tmpShots[0][1] = tmpShots[2][1] = 1;
            listFields.get(i).setShots(tmpShots);
        }

        for (int i: upSide)
        {
            int[][] tmpShots = listFields.get(i).getShots();
            if (i == 15)
                tmpShots[0][0] = tmpShots[0][1] = tmpShots[1][1] = 1;
            else if (i == 17)
                tmpShots[0][1] = tmpShots[0][2] = tmpShots[1][2] = 1;
            else
                tmpShots[0][0] = tmpShots[0][1] = tmpShots[0][2] = tmpShots[1][2] = tmpShots[1][0] = 1;
            listFields.get(i).setShots(tmpShots);
        }

        for (int i: downSide)
        {
            int[][] tmpShots = listFields.get(i).getShots();
            if (i == 125)
                tmpShots[2][0] = tmpShots[2][1] = tmpShots[1][1] = 1;
            else if (i == 127)
                tmpShots[2][1] = tmpShots[2][2] = tmpShots[1][2] = 1;
            else
                tmpShots[2][0] = tmpShots[2][1] = tmpShots[2][2] = tmpShots[1][2] = tmpShots[1][0] = 1;
            listFields.get(i).setShots(tmpShots);
        }
    }
    public void drawPlayField()
    {
        int[] fieldID = {12, 13, 14, 15, 4, 5, 6, 17, 18, 19, 20, 31, 42, 53, 64, 75, 86, 97, 108, 119, 130, 129, 128, 127, 138, 137, 136, 125, 124, 123, 122, 111, 100, 89, 78, 67, 56, 45, 34, 23, 12};

        for(int i = 0; i < fieldID.length - 1; i++)
            drawLine(listViews.get(fieldID[i]), listViews.get(fieldID[i+1]));

        blockMoveOutsideField();
    }

}
