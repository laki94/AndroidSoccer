package test.pkantor.soccer1;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.os.Vibrator;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import test.pkantor.soccer1.Bluetooth.BluetoothConnectionService;

import static java.lang.Math.round;
import static test.pkantor.soccer1.R.layout.activity_game;

public class Game extends AppCompatActivity {

    FrameLayout lay;

    public static final int LOCAL = 0;
    public static final int BLUETOOTH = 1;

    private Toast toast = null;

    int left = 0;
    int top = 0;
    int countX;
    int countY;
    int goalPointsToWin;
    int gameMode;
    boolean playerAccepted = true;
    boolean canDelete = false;
    boolean doShowAcceptButton;
    boolean doShowPlayerNames;
    boolean doEnableVibrations;
    boolean newGame = false;
    boolean imFirstPlayer = false;

    ImageView _source;
    ImageView _destination;
    ImageView _lastDestination;
    ImageView pilka;

    List<ImageView> listViews;
    List<ImageView> listLinie;
    List<Field> listFields;

    Player player1;
    Player player2;

    Vibrator vibrator;

    Button acceptMove;
    CheckBox cbAcceptMove = null;
    CheckBox cbShowNames = null;
    CheckBox cbEnableVibrations = null;
    Button bSurrender = null;

    android.support.v7.widget.Toolbar tbHeader;
    TextView tvScore;

    Resources res;
    BluetoothConnectionService mBluetoothConnectionService;
    Handler mHandler;
    StringBuffer mOutStringBuffer;
    GlobalSocket gSocket;
    SharedPreferences sharedPreferences;



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
                    if ((gameMode == BLUETOOTH) && (mBluetoothConnectionService != null))
                        mBluetoothConnectionService.stop();
                    finish();
                    Intent intent = new Intent(getApplicationContext(), Menu.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
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
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navView);
        navigationView.bringToFront();
        android.view.Menu menu = navigationView.getMenu();

        final MenuItem iAcceptMove = menu.findItem(R.id.nav_AcceptMove);
        final MenuItem iShowNames = menu.findItem(R.id.nav_ShowPlayers);
        final MenuItem iEnableVibrations = menu.findItem(R.id.nav_EnableVibrations);
        final MenuItem iSurrender = menu.findItem(R.id.nav_Surrender);

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

        iEnableVibrations.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                CheckBox cb = (CheckBox) item.getActionView();
                cb.setChecked(!cb.isChecked());
                return false;
            }
        });

        iSurrender.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Button button = (Button) item.getActionView();
                button.performClick();
                return false;
            }
        });

        cbAcceptMove = (CheckBox) iAcceptMove.getActionView();
        cbShowNames = (CheckBox) iShowNames.getActionView();
        cbEnableVibrations = (CheckBox) iEnableVibrations.getActionView();
        bSurrender = (Button) iSurrender.getActionView();

        cbAcceptMove.setChecked(sharedPreferences.getBoolean(getString(R.string.SP_show_accept_move), true));
        cbShowNames.setChecked(sharedPreferences.getBoolean(getString(R.string.SP_show_player_names), true));
        cbEnableVibrations.setChecked(sharedPreferences.getBoolean(getString(R.string.SP_enable_vibrations), true));

        setVisibilityAcceptMove(cbAcceptMove.isChecked());
        setVisibilityPlayerNames(cbShowNames.isChecked());
        setEnabledVibrations(cbEnableVibrations.isChecked());

        cbAcceptMove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setVisibilityAcceptMove(isChecked);
                editor.putBoolean(getString(R.string.SP_show_accept_move), isChecked);
                editor.apply();
            }
        });

        cbShowNames.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setVisibilityPlayerNames(isChecked);
                editor.putBoolean(getString(R.string.SP_show_player_names), isChecked);
                editor.apply();
            }
        });

        cbEnableVibrations.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setEnabledVibrations(isChecked);
                editor.putBoolean(getString(R.string.SP_enable_vibrations), isChecked);
                editor.apply();
            }
        });

        bSurrender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("surrender");

                if (imFirstPlayer)
                {
                    player2.addPoint();
                    if (!checkIfPointsGained())
                        startNewGame(player2, player1);
                }

                else
                {
                    player1.addPoint();
                    if (!checkIfPointsGained())
                        startNewGame(player1, player2);
                }

            }
        });


//        doShowAcceptButton = cbAcceptMove.isChecked();
//        doShowPlayerNames = cbShowNames.isChecked();
//        doEnableVibrations = cbEnableVibrations.isChecked();

        if (gameMode == LOCAL)
        {
            iSurrender.setEnabled(false);
            bSurrender.setEnabled(false);
        }
        else
        {
            iSurrender.setEnabled(true);
            bSurrender.setEnabled(true);
        }
    }

    public void setVisibilityAcceptMove(boolean visible)
    {
        doShowAcceptButton = visible;
        if (!visible)
            acceptMove.setVisibility(View.INVISIBLE);
        else
            acceptMove.setVisibility(View.VISIBLE);
    }

    public void setVisibilityPlayerNames(boolean visible)
    {
        TextView tvPlayer1 = (TextView) findViewById(R.id.tvPlayer1);
        TextView tvPlayer2 = (TextView) findViewById(R.id.tvPlayer2);

        doShowPlayerNames = visible;

        if (!visible)
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

    public void setEnabledVibrations(boolean enable)
    {
        doEnableVibrations = enable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(activity_game);

        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preferences_soccer), Context.MODE_PRIVATE);
        TextView g1 = (TextView) findViewById(R.id.tvPlayer1);
        TextView g2 = (TextView) findViewById(R.id.tvPlayer2);
        tbHeader = (android.support.v7.widget.Toolbar) findViewById(R.id.tbGame);
        tvScore = (TextView) tbHeader.findViewById(R.id.tv_tbTitle);

        res = getResources();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mOutStringBuffer = new StringBuffer("");

        gSocket = (GlobalSocket) getApplicationContext();

        acceptMove = (Button) findViewById(R.id.acceptMove);
        player1 = new Player();
        player2 = new Player();

        listViews = new ArrayList<>();
        listLinie = new ArrayList<>();
        listFields = new ArrayList<>();
        lay = (FrameLayout) findViewById(R.id.FLlay);

        setSettings();
        prepareDrawer();

        lay.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                startGame();
                lay.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

//        int height = displayMetrics.heightPixels;
//        int width = displayMetrics.widthPixels;
        countY = sharedPreferences.getInt(getString(R.string.SP_count_y), 50);//round(height / 13);
        countX = sharedPreferences.getInt(getString(R.string.SP_count_x), 50);//round(width / 11);

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
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.d("DOWN", "wcisnieto: " + pol.getId());
                            if ((isMyTurn() || gameMode == LOCAL)) {
                                _lastDestination = (ImageView) findViewById(pol.getId());

                            if (makeMove())
                                if (!doShowAcceptButton)
                                    acceptMove.performClick();

                            } else if ((!isMyTurn()) && (gameMode == BLUETOOTH)) {
                                if (toast != null)
                                    toast.cancel();
                                toast = Toast.makeText(getApplicationContext(), getString(R.string.not_your_turn_error), Toast.LENGTH_LONG);
                                toast.show();
                            }

                            return true;
                        case MotionEvent.ACTION_UP:
                            Log.d("UP", "" + pol.getId());
                            return false;
                        default:
                            return false;

                    }
                }
            });
        }
        _source = listViews.get(71);


//        Bundle extras = getIntent().getExtras();


            g1.bringToFront();
            g2.bringToFront();
            g1.setWidth(countX * 4);
            g2.setWidth(countX * 4);

            g1.setTextSize(TypedValue.COMPLEX_UNIT_SP, (countX / res.getDisplayMetrics().scaledDensity) / 2);
            g2.setTextSize(TypedValue.COMPLEX_UNIT_SP, (countX / res.getDisplayMetrics().scaledDensity) / 2);

            g1.setText(player1.getName());
            g2.setText(player2.getName());
            g1.setX((countX * 7) - countX / 3);
            g1.setY((countX * 12) - countX / 3);
            g2.setX((countX * 7) - countX / 3);
            g2.setY((countX * 1) - countX / 3);

            setToolbarScore();
            setSupportActionBar(tbHeader);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    public void setSettings()
    {
        Intent prevIntent = getIntent();

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, prevIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Bundle extras = prevIntent.getExtras();

        if (extras != null) {
            player1.setName(extras.getString("p1Name"));
            player2.setName(extras.getString("p2Name"));
            goalPointsToWin = extras.getInt("goalPoints");
            gameMode = extras.getInt("gameMode");
            imFirstPlayer = extras.getBoolean("amIFirst");
            if (gameMode == BLUETOOTH) {
                mBluetoothConnectionService = gSocket.getBluetoothConnectionService();

                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case 1:
                                break;
                            case 3:
                                byte[] writeBuf = (byte[]) msg.obj;
                                // construct a string from the buffer
                                String writeMessage = new String(writeBuf);
                                //Toast.makeText(getApplicationContext(), "Wyslano " + writeMessage, Toast.LENGTH_LONG).show();
//                                mBTDevicesAdapter.add("Me:  " + writeMessage);
                                break;
                            case 2:
                                byte[] readBuf = (byte[]) msg.obj;
                                // construct a string from the valid bytes in the buffer
                                String readMessage = new String(readBuf, 0, msg.arg1);

                                if (readMessage.equals("surrender"))
                                {
                                    if (imFirstPlayer)
                                    {
                                        player1.addPoint();
                                        if (!checkIfPointsGained())
                                            startNewGame(player1, player2);
                                    }
                                    else
                                    {
                                        player2.addPoint();
                                        if (!checkIfPointsGained())
                                            startNewGame(player2, player1);
                                    }
                                }
                                else
                                {
                                    StringTokenizer tokenizer = new StringTokenizer(readMessage, ":");
                                    if (tokenizer.countTokens() == 2) {
                                        _lastDestination = listViews.get(Integer.parseInt(tokenizer.nextToken()));

                                        makeMove();
                                        if (tokenizer.nextToken().equals("move"))
                                            acceptMove.performClick();
                                    }
                                }
                                break;
                            case 5:
                                if (gameMode == BLUETOOTH)
                                    if (!gSocket.getAmIConnected())
                                    {
                                        if (toast != null)
                                            toast.cancel();
                                        toast = Toast.makeText(getApplicationContext(), getString(R.string.connection_lost), Toast.LENGTH_LONG);
                                        toast.show();

                                        Intent intent = new Intent(Game.this, Menu.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }

                        }
                    }
                };
                mBluetoothConnectionService.setHandler(mHandler);
            }
        } else {
//            if (player1.getName().equals(""))
            player1.setName(getString(R.string.default_fplayer));
//            if (player2.getName().equals(""))
            player2.setName(getString(R.string.default_splayer));

            goalPointsToWin = 1;
        }
    }


    public boolean makeMove()
    {
        if (!tryToCreateLine(_source, _lastDestination)) {
            if (!checkIfShotPossible(_source, _lastDestination)) {
                if (toast != null)
                    toast.cancel();
                if (_lastDestination == _source)
                    toast = Toast.makeText(getApplicationContext(), getString(R.string.select_next_field_error), Toast.LENGTH_LONG);
                else
                    toast = Toast.makeText(getApplicationContext(), getString(R.string.too_long_pass_error), Toast.LENGTH_LONG);
                toast.show();
                _lastDestination = _destination;
            }
        } else {
            if (checkIfShotPossible(_source, _lastDestination)) {
                _destination = _lastDestination;
                if (drawLine(_source, _destination)) {
                    canDelete = true;
                    playerAccepted = false;
                    pilka.setX(_destination.getLeft() + _destination.getWidth() / 4);
                    pilka.setY(_destination.getTop() + _destination.getHeight() / 4);
                    pilka.bringToFront();
                    pilka.invalidate();
                    return true;
                }
            } else {
                if (toast != null)
                    toast.cancel();
                toast = Toast.makeText(getApplicationContext(), getString(R.string.invalid_direction_error), Toast.LENGTH_LONG);
                toast.show();
            }
        }
        return false;
    }

    public boolean isMyTurn()
    {
        if ((imFirstPlayer) && (player1.isMove()))
            return true;
        else if ((!imFirstPlayer) && (player2.isMove()))
            return true;
        else
            return false;
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
        String textScore = getString(R.string.score, player1.getName(), player1.getPoints(), player2.getPoints(), player2.getName());
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
        whoMoves();

    }

    public boolean tryToCreateLine(ImageView src, ImageView dst)
    {
        Line line = new Line(this);
        return line.canCreateLine(src, dst);
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
            toast = Toast.makeText(this, getString(R.string.make_move_first_error), Toast.LENGTH_SHORT);

            switch (gameMode)
            {
                case LOCAL:
                    toast.show();
                    break;
                case BLUETOOTH:
                    if (isMyTurn())
                        toast.show();
                    break;
            }
        }

        else
        {
            if ((_source == null) || (_destination == null))
                return false;

            if ((gameMode == BLUETOOTH) && (isMyTurn()))
            {
                sendMessage(String.valueOf(_destination.getId()) + ":move");

//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        sendMessage(String.valueOf(_destination.getId()) + ":move");
//                    }
//                }, 500);

            }


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
        {
            if (doEnableVibrations)
                if (((imFirstPlayer) && (gameMode == BLUETOOTH)) && (!player1.isAdditionalMove()))
                    vibrator.vibrate(150);
            String nowMoves = getString(R.string.who_moves, player1.getName());
            tv.setText(nowMoves);
        }
        else {
            if (doEnableVibrations)
                if (((!imFirstPlayer) && (gameMode == BLUETOOTH)) && (!player2.isAdditionalMove()))
                    vibrator.vibrate(150);

            String nowMoves = getString(R.string.who_moves, player2.getName());
            tv.setText(nowMoves);
        }
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
                        isOwnGoal = true;
                    else
                        isGoal = true;
                    player1.addPoint();
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
                        player2.addPoint();
                        break;
                    }


        if (possibleMoves <= 2) {
            if (player1.isMove())
                player2.addPoint();
            else
                player1.addPoint();
        }


        if (!checkIfPointsGained())
        {
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
            {
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
        }
        return false;
    }

    public boolean checkIfPointsGained()
    {
        Intent intent = new Intent(this, GameOver.class);

        if (player1.getPoints() == goalPointsToWin)
        {
            intent.putExtra("winner", player1.getName());
            startActivity(intent);
            return true;
        }
        else if (player2.getPoints() == goalPointsToWin)
        {
            intent.putExtra("winner", player2.getName());
            startActivity(intent);
            return true;
        }
        return false;
    }

    public boolean drawLine(ImageView source, ImageView destination)
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

    public void sendMessage(String message)
    {
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBluetoothConnectionService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

}
