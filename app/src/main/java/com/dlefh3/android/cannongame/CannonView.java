package com.dlefh3.android.cannongame;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

public class CannonView extends SurfaceView implements SurfaceHolder.Callback {
    // constants for game play
    public static final int TARGET_PIECES = 7; // sections in the target
    public static final int MISS_PENALTY = 2; // seconds deducted on a miss
    public static final int HIT_REWARD = 3; // seconds added on a hit
    public static final int MAX_ACTIVE_BALLS = 4; // number of acive cannon balls allowed
    public static final long TIME_BETWEEN_SHOTS = 150; // delay between shots in milliseconds
    public static final double GAME_LENGTH = 15;
    private static final String TAG = "CannonView"; // for logging errors
    // constants and variables for managing sounds
    private static final int TARGET_SOUND_ID = 0;
    private static final int CANNON_SOUND_ID = 1;
    private static final int BLOCKER_SOUND_ID = 2;
    protected double totalElapsedTime; // elapsed seconds
    private com.dlefh3.android.cannongame.CannonThread cannonThread; // controls the game loop
    private Activity activity; // to display Game Over dialog in GUI thread
    private boolean dialogIsDisplayed = false;
    // variables for the game loop and tracking statistics
    private boolean gameOver; // is the game over?
    private double timeLeft; // time remaining in seconds
    private int shotsFired; // shots the user has fired
    // variables for the blocker and target
    private Line blocker; // start and end points of the blocker
    private int blockerDistance; // blocker distance from left
    private int blockerBeginning; // blocker top-edge distance from top
    private int blockerEnd; // blocker bottom-edge distance from top
    private int initialBlockerVelocity; // initial blocker speed multiplier
    private float blockerVelocity; // blocker speed multiplier during game
    private Line target; // start and end points of the target
    private int targetDistance; // target distance from left
    private int targetBeginning; // target distance from top
    private double pieceLength; // length of a target piece
    private int targetEnd; // target bottom's distance from top
    private int initialTargetVelocity; // initial target speed multiplier
    private float targetVelocity; // target speed multiplier
    private int lineWidth; // width of the target and blocker
    private boolean[] hitStates; // is each target piece hit?
    private int targetPiecesHit; // number of target pieces hit (out of 7)
    // variables for the cannon and cannonball
    private ArrayList<CannonBall> balls = new ArrayList<CannonBall>();
    private long timeOfLastShot = 0; // last time shot was fired

    //private Point cannonball; // cannonball image's upper-left corner
    //private int cannonballVelocityX; // cannonball's x velocity
    //private int cannonballVelocityY; // cannonball's y velocity
    //private boolean cannonballOnScreen; // whether cannonball on the screen
    private int cannonballRadius; // cannonball's radius
    private int cannonballSpeed; // cannonball's speed
    private int cannonBaseRadius; // cannon base's radius
    private int cannonLength; // cannon barrel's length
    private Point barrelEnd; // the endpoint of the cannon's barrel
    private int screenWidth;
    private int screenHeight;
    private SoundPool soundPool; // plays sound effects
    private SparseIntArray soundMap; // maps IDs to SoundPool

    // Paint variables used when drawing each item on the screen
    private Paint textPaint; // Paint used to draw text
    private Paint cannonballPaint; // Paint used to draw the cannonball
    private Paint cannonPaint; // Paint used to draw the cannon
    private Paint blockerPaint; // Paint used to draw the blocker
    private Paint targetPaint; // Paint used to draw the target
    private Paint backgroundPaint; // Paint used to clear the drawing area

    // Variables to control level difficulty
    private int level = 1; // Level Number
    private double ballSizeMultiplier = 1.0; // Initial ball size
    private double ballSpeedMultiplier = 1.0; //Initial ball speed
    private double ballSpeedIncrease = 0.1; // Multiplicative rate of speed increase
    private double ballSizeIncrease = 0.15; // Multiplicative rate of size increase

    private boolean stateRestored = false;
    private LevelDatabaseHelper dbHelper;
    // Score keeping
    private double score = 0.0; // Holds current score


    // public constructor
    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs); // call superclass constructor
        activity = (Activity) context; // store reference to MainActivity
        dbHelper = new LevelDatabaseHelper(context);
        // register SurfaceHolder.Callback listener
        getHolder().addCallback(this);

        // create cannon balls
        for (int i = 0; i < MAX_ACTIVE_BALLS; i++)
        {
            balls.add(new CannonBall()); // create cannonballs
        }
        // initialize Lines and Point representing game items
        blocker = new Line(); // create the blocker as a Line
        target = new Line(); // create the target as a Line

        // initialize hitStates as a boolean array
        hitStates = new boolean[TARGET_PIECES];

        // initialize SoundPool to play the app's three sound effects
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        // create Map of sounds and pre-load sounds
        soundMap = new SparseIntArray(3); // create new HashMap
        soundMap.put(TARGET_SOUND_ID,
                soundPool.load(context, R.raw.target_hit, 1));
        soundMap.put(CANNON_SOUND_ID,
                soundPool.load(context, R.raw.cannon_fire, 1));
        soundMap.put(BLOCKER_SOUND_ID,
                soundPool.load(context, R.raw.blocker_hit, 1));

        // construct Paints for drawing text, cannonball, cannon,
        // blocker and target; these are configured in method onSizeChanged
        textPaint = new Paint();
        cannonPaint = new Paint();
        cannonballPaint = new Paint();
        blockerPaint = new Paint();
        targetPaint = new Paint();
        backgroundPaint = new Paint();
    } // end CannonView constructor

    // called by surfaceChanged when the size of the SurfaceView changes,
    // such as when it's first added to the View hierarchy
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            int temp = h;
            h = w;
            w = temp;
        }

        screenWidth = w; // store CannonView's width
        screenHeight = h; // store CannonView's height
        cannonBaseRadius = h / 18; // cannon base radius 1/18 screen height
        //cannonLength = w / 6; // cannon length 1/8 screen width
        cannonLength = cannonBaseRadius * 2;
        cannonballRadius = (int)(w / 36 * ballSizeMultiplier);
        cannonballSpeed = (int)(w * 3 / 2 * ballSpeedMultiplier);
        for (CannonBall ball: balls)
        {
            ball.setRadius(cannonballRadius); // cannonball radius 1/36 screen width
            ball.setSpeed(cannonballSpeed); // cannonball speed multiplier
        }


        lineWidth = w / 24; // target and blocker 1/24 screen width

        // configure instance variables related to the blocker
        blockerDistance = w * 5 / 8; // blocker 5/8 screen width from left
        blockerBeginning = h / 8; // distance from top 1/8 screen height
        blockerEnd = h * 3 / 8; // distance from top 3/8 screen height
        initialBlockerVelocity = h / 2; // initial blocker speed multiplier
        blocker.start = new Point(blockerDistance, blockerBeginning);
        blocker.end = new Point(blockerDistance, blockerEnd);

        // configure instance variables related to the target
        targetDistance = w * 7 / 8; // target 7/8 screen width from left
        targetBeginning = h / 8; // distance from top 1/8 screen height
        targetEnd = h * 7 / 8; // distance from top 7/8 screen height
        pieceLength = (targetEnd - targetBeginning) / TARGET_PIECES;
        initialTargetVelocity = -h / 4; // initial target speed multiplier
        target.start = new Point(targetDistance, targetBeginning);
        target.end = new Point(targetDistance, targetEnd);
        Log.i("piece length", String.valueOf(pieceLength));
        // endpoint of the cannon's barrel initially points horizontally
        barrelEnd = new Point(cannonLength, h / 2);

        // configure Paint objects for drawing game elements
        textPaint.setTextSize(w / 20); // text size 1/20 of screen width
        textPaint.setAntiAlias(true); // smoothes the text
        cannonPaint.setStrokeWidth(lineWidth * 1.5f); // set line thickness
        blockerPaint.setStrokeWidth(lineWidth); // set line thickness
        targetPaint.setStrokeWidth(lineWidth); // set line thickness
        backgroundPaint.setColor(Color.WHITE); // set background color

        if (stateRestored)
        {
            restoreGame();
        }
        else
        {
            newGame(); // set up and start a new game
        }
    } // end method onSizeChanged

    // reset the screen elements and restore a game
    public void restoreGame()
    {
        stateRestored = false;
        blockerVelocity = initialBlockerVelocity; // set initial velocity
        targetVelocity = initialTargetVelocity; // set initial velocity

        for(CannonBall ball: balls) {
            ball.setOnScreen(false);// the cannonball is not on the screen
        }

        // set the start and end Points of the blocker and target
        blocker.start.set(blockerDistance, blockerBeginning);
        blocker.end.set(blockerDistance, blockerEnd);
        target.start.set(targetDistance, targetBeginning);
        target.end.set(targetDistance, targetEnd);

        if (gameOver) // starting a new game after the last game ended
        {
            gameOver = false;
            cannonThread = new com.dlefh3.android.cannongame.CannonThread(getHolder(), this); // create thread
            cannonThread.start(); // start the game loop thread
        }
    }
    public void nextLevel()
    {
        level++;
        ballSpeedMultiplier -= ballSpeedIncrease;
        ballSizeMultiplier += ballSizeIncrease;

        cannonballRadius = (int)(screenWidth / 36 * ballSizeMultiplier);
        cannonballSpeed = (int)(screenWidth * 3 / 2 * ballSpeedMultiplier);
        Log.i("radius", String.valueOf(cannonballRadius));
        blockerVelocity = initialBlockerVelocity; // set initial velocity
        targetVelocity = initialTargetVelocity; // set initial velocity

        for(CannonBall ball: balls) {
            ball.setSpeed(cannonballSpeed);
            ball.setRadius(cannonballRadius);
            ball.setOnScreen(false);// the cannonball is not on the screen
        }

        targetPiecesHit = 0;
        for (int i = 0; i < TARGET_PIECES; i++)
            hitStates[i] = false;

        score += timeLeft;
        timeLeft = GAME_LENGTH;
        // starting a new game after the last game ended
        gameOver = false;
        cannonThread = new com.dlefh3.android.cannongame.CannonThread(getHolder(), this); // create thread
        cannonThread.start(); // start the game loop thread



    }
    // reset all the screen elements and start a new game
    public void newGame() {

        score = 0;
        level = 1;

        ballSpeedMultiplier = 1.0;
        ballSizeMultiplier = 1.0;
        cannonballRadius = (int)(screenWidth / 36 * ballSizeMultiplier);
        cannonballSpeed = (int)(screenWidth * 3 / 2 * ballSpeedMultiplier);

        Log.i("radius new game", String.valueOf(cannonballRadius));
        // set every element of hitStates to false--restores target pieces
        for (int i = 0; i < TARGET_PIECES; i++)
            hitStates[i] = false;

        targetPiecesHit = 0; // no target pieces have been hit
        blockerVelocity = initialBlockerVelocity; // set initial velocity
        targetVelocity = initialTargetVelocity; // set initial velocity
        timeLeft = GAME_LENGTH; // start the countdown

        for(CannonBall ball: balls) {
            ball.setSpeed(cannonballSpeed);
            ball.setRadius(cannonballRadius);
            ball.setOnScreen(false);// the cannonball is not on the screen
        }

        shotsFired = 0; // set the initial number of shots fired
        totalElapsedTime = 0.0; // set the time elapsed to zero

        // set the start and end Points of the blocker and target
        blocker.start.set(blockerDistance, blockerBeginning);
        blocker.end.set(blockerDistance, blockerEnd);
        target.start.set(targetDistance, targetBeginning);
        target.end.set(targetDistance, targetEnd);

        if (gameOver) // starting a new game after the last game ended
        {
            gameOver = false;
            cannonThread = new com.dlefh3.android.cannongame.CannonThread(getHolder(), this); // create thread
            cannonThread.start(); // start the game loop thread
        }


    } // end method newGame

    // called repeatedly by the CannonThread to update game elements
    protected void updatePositions(double elapsedTimeMS) {
        double interval = elapsedTimeMS / 1000.0; // convert to seconds
        for (CannonBall ball: balls)
        {
            if (ball.isOnScreen()) // if there is currently a shot fired
            {

                // update cannonball position
                ball.setX(ball.getX() + (int)(interval * ball.getVelocityX())) ;
                ball.setY(ball.getY() + (int) (interval * ball.getVelocityY())) ;


                // check for collision with blocker
                if (ball.getX() + ball.getRadius() > blockerDistance &&
                        ball.getX() - ball.getRadius() < blockerDistance &&
                        ball.getY() + ball.getRadius() > blocker.start.y &&
                        ball.getY() - ball.getRadius() < blocker.end.y)
                {
                    ball.setVelocityX(ball.getVelocityX() * -1); // reverse cannonball's direction
                    timeLeft -= MISS_PENALTY; // penalize the user

                    // play blocker sound
                    soundPool.play(soundMap.get(BLOCKER_SOUND_ID), 1, 1, 1, 0, 1f);
                }
                // check for collisions with left and right walls
                else if (ball.getX() + ball.getRadius() > screenWidth ||
                        ball.getX() - ball.getRadius() < 0) {
                    ball.setOnScreen(false); // remove cannonball from screen
                }
                // check for collisions with top and bottom walls
                else if (ball.getY() + ball.getRadius() > screenHeight ||
                        ball.getY() - ball.getRadius() < 0) {
                    ball.setOnScreen(false);// remove cannonball from screen
                }
                // check for cannonball collision with target
                else if (ball.getX() + ball.getRadius() > targetDistance &&
                        ball.getX() - ball.getRadius() < targetDistance &&
                        ball.getY() + ball.getRadius() > target.start.y &&
                        ball.getY() - ball.getRadius() < target.end.y) {
                    // determine target section number (0 is the top)
                    int section =
                            (int) ((ball.getY() - target.start.y) / pieceLength);

                    // check if the piece hasn't been hit yet
                    if ((section >= 0 && section < TARGET_PIECES) &&
                            !hitStates[section]) {
                        hitStates[section] = true; // section was hit
                        ball.setOnScreen(false); // remove cannonball
                        timeLeft += HIT_REWARD; // add reward to remaining time
                        score += 1;

                        // play target hit sound
                        soundPool.play(soundMap.get(TARGET_SOUND_ID), 1,
                                1, 1, 0, 1f);

                        // if all pieces have been hit
                        if (++targetPiecesHit == TARGET_PIECES) {
                            //gameOver = true;
                            cannonThread.setRunning(false); // terminate thread
                            showNextLevelDialog(); // show winning dialog
                            //nextLevel();
                        }
                    }
                }
            }
        }


        // update the blocker's position
        double blockerUpdate = interval * blockerVelocity;
        blocker.start.y += blockerUpdate;
        blocker.end.y += blockerUpdate;

        // update the target's position
        double targetUpdate = interval * targetVelocity;
        target.start.y += targetUpdate;
        target.end.y += targetUpdate;

        // if the blocker hit the top or bottom, reverse direction
        if (blocker.start.y < 0 || blocker.end.y > screenHeight)
            blockerVelocity *= -1;

        // if the target hit the top or bottom, reverse direction
        if (target.start.y < 0 || target.end.y > screenHeight)
            targetVelocity *= -1;
        if (!gameOver)
            timeLeft -= interval; // subtract from time left

        // if the timer reached zero
        if (timeLeft <= 0.0) {
            timeLeft = 0.0;
            gameOver = true; // the game is over
            cannonThread.setRunning(false); // terminate thread
            showGameOverDialog(R.string.lose); // show the losing dialog
        }
    } // end method updatePositions

    // fires a cannonball
    public void fireCannonball(MotionEvent event) {
        if (System.currentTimeMillis() - timeOfLastShot > TIME_BETWEEN_SHOTS)
        {
            for ( CannonBall ball: balls)
            {
                if (!ball.isOnScreen())
                {
                    timeOfLastShot = System.currentTimeMillis();
                    double angle = alignCannon(event); // get the cannon barrel's angle

                    //ball.setRadius((cannonballRadius)); // cannonball radius 1/36 screen width
                    // ball.setSpeed(cannonballSpeed); // cannonball speed multiplier

                    // move the cannonball to be inside the cannon
                    ball.setX(ball.getRadius()); // align x-coordinate with cannon
                    ball.setY(screenHeight / 2); // centers ball vertically

                    // get the x component of the total velocity
                    ball.setVelocityX((int) (ball.getSpeed() * Math.sin(angle)));

                    // get the y component of the total velocity
                    ball.setVelocityY((int) (-ball.getSpeed() * Math.cos(angle)));
                    ball.setOnScreen(true); // the cannonball is on the screen
                    ++shotsFired; // increment shotsFired


                    // play cannon fired sound
                    soundPool.play(soundMap.get(CANNON_SOUND_ID), 1, 1, 1, 0, 1f);
                    return;
                }

            }
        }


    } // end method fireCannonball

    // aligns the cannon in response to a user touch
    public double alignCannon(MotionEvent event) {

        Point touchPoint;
        // get the location of the touch in this view
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            touchPoint = new Point((int) event.getY(), (int) event.getX());
            // compute the touch's distance from center of the screen
            // on the y-axis
            double centerMinusY = (screenHeight / 2 - touchPoint.y);

            double angle = 0; // initialize angle to 0

            // calculate the angle the barrel makes with the horizontal
            if (centerMinusY != 0) // prevent division by 0
                angle = Math.atan((double) touchPoint.x / centerMinusY);

            // if the touch is on the lower half of the screen
            if (touchPoint.y > screenHeight / 2)
                angle += Math.PI; // adjust the angle

            // calculate the endpoint of the cannon barrel
            barrelEnd.x = (int) (cannonLength * Math.sin(angle));
            barrelEnd.y =
                    (int) (-cannonLength * Math.cos(angle) + screenHeight / 2);

            return angle; // return the computed angle
        }
        else
        {
            touchPoint = new Point((int) event.getX(), (int) event.getY());
            // compute the touch's distance from center of the screen
            // on the y-axis
            double centerMinusY = (screenHeight / 2 - touchPoint.y);

            double angle = 0; // initialize angle to 0

            // calculate the angle the barrel makes with the horizontal
            if (centerMinusY != 0) // prevent division by 0
                angle = Math.atan((double) touchPoint.x / centerMinusY);

            // if the touch is on the lower half of the screen
            if (touchPoint.y > screenHeight / 2)
                angle += Math.PI; // adjust the angle

            // calculate the endpoint of the cannon barrel
            barrelEnd.x = (int) (cannonLength * Math.sin(angle));
            barrelEnd.y =
                    (int) (-cannonLength * Math.cos(angle) + screenHeight / 2);

            return angle; // return the computed angle
        }


    } // end method alignCannon

    // draws the game to the given Canvas
    public void drawGameElements(Canvas canvas) {
        if (canvas == null)
            return;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            drawGameElementsLandscape(canvas);
        }
        else
        {
            // clear the background
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(),
                    backgroundPaint);

            // display time remaining
            canvas.drawText(getResources().getString(
                    R.string.time_remaining_format, timeLeft, score), 30, 50, textPaint);

            // display speed multiplier
            canvas.drawText(getResources().getString(
                    R.string.speed_multiplier_format, ballSpeedMultiplier), 30, 150, textPaint);
            // display size multiplier
            canvas.drawText(getResources().getString(
                    R.string.size_multiplier_format, ballSizeMultiplier), 30, 250, textPaint);
            // if a cannonball is currently on the screen, draw it
            for (CannonBall ball: balls)
            {
                if (ball.isOnScreen())
                    canvas.drawCircle(ball.getX(), ball.getY(), ball.getRadius(),
                            cannonballPaint);
                //Log.i("radius", String.valueOf(ball.getRadius()));
            }

            // draw the cannon barrel
            canvas.drawLine(0, screenHeight / 2, barrelEnd.x, barrelEnd.y,
                    cannonPaint);

            // draw the cannon base
            canvas.drawCircle(0, (int) screenHeight / 2,
                    (int) cannonBaseRadius, cannonPaint);

            // draw the blocker
            canvas.drawLine(blocker.start.x, blocker.start.y, blocker.end.x,
                    blocker.end.y, blockerPaint);

            Point currentPoint = new Point(); // start of current target section

            // initialize currentPoint to the starting point of the target
            currentPoint.x = target.start.x;
            currentPoint.y = target.start.y;

            // draw the target
            for (int i = 0; i < TARGET_PIECES; i++) {
                // if this target piece is not hit, draw it
                if (!hitStates[i]) {
                    // alternate coloring the pieces
                    if (i % 2 != 0)
                        targetPaint.setColor(Color.BLUE);
                    else
                        targetPaint.setColor(Color.YELLOW);

                    canvas.drawLine(currentPoint.x, currentPoint.y, target.end.x,
                            (int) (currentPoint.y + pieceLength), targetPaint);
                }

                // move currentPoint to the start of the next piece
                currentPoint.y += pieceLength;
            }
        }

    } // end method drawGameElements


    // draws the game to the given Canvas in Landscape
    public void drawGameElementsLandscape(Canvas canvas) {
        // clear the background


        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(),
                backgroundPaint);

        // display time remaining
        canvas.drawText(getResources().getString(
                R.string.time_remaining_format, timeLeft, score), 30, 50, textPaint);

        // display speed multiplier
        canvas.drawText(getResources().getString(
                R.string.speed_multiplier_format, ballSpeedMultiplier), 30, 150, textPaint);
        // display size multiplier
        canvas.drawText(getResources().getString(
                R.string.size_multiplier_format, ballSizeMultiplier), 30, 250, textPaint);
        // if a cannonball is currently on the screen, draw it
        for (CannonBall ball: balls)
        {
            if (ball.isOnScreen())
                canvas.drawCircle(ball.getY(), ball.getX(), ball.getRadius(),
                        cannonballPaint);
        }

        // draw the cannon barrel
        canvas.drawLine(screenHeight / 2, 20, barrelEnd.y, barrelEnd.x+20,
                cannonPaint);

        // draw the cannon base
        canvas.drawCircle((int) screenHeight / 2, 0,
                (int) cannonBaseRadius, cannonPaint);

        // draw the blocker
        canvas.drawLine(blocker.start.y, blocker.start.x, blocker.end.y,
                blocker.end.x, blockerPaint);

        Point currentPoint = new Point(); // start of current target section

        // initialize currentPoint to the starting point of the target
        currentPoint.x = target.start.x;
        currentPoint.y = target.start.y;
        Log.i("target", target.end.toString());
        // draw the target
        for (int i = 0; i < TARGET_PIECES; i++) {
            // if this target piece is not hit, draw it
            if (!hitStates[i]) {
                // alternate coloring the pieces
                if (i % 2 != 0)
                    targetPaint.setColor(Color.BLUE);
                else
                    targetPaint.setColor(Color.YELLOW);

                Log.i("currentPoint", currentPoint.toString());

                canvas.drawLine(currentPoint.y, currentPoint.x,
                        (int) (currentPoint.y + pieceLength), target.end.x, targetPaint);
            }

            // move currentPoint to the start of the next piece
            currentPoint.y += pieceLength;
        }
    } // end method drawGameElements

    private void showNextLevelDialog()
    {
        // DialogFragment to display stats and start new level
        final DialogFragment nextFragment =
                new DialogFragment(){
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState)
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Level Complete!");
                        builder.setPositiveButton("Next Level",
                                new DialogInterface.OnClickListener()
                                {
                                    // called when "Next Level" Button is pressed
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialogIsDisplayed = false;
                                        nextLevel();
                                    }
                                } // end anonymous inner class
                        ); // end call to setPositiveButton
                        return builder.create();
                    }
                };
        activity.runOnUiThread(
                new Runnable() {
                    public void run() {
                        dialogIsDisplayed = true;
                        nextFragment.setCancelable(false); // modal dialog
                        nextFragment.show(activity.getFragmentManager(), "next");

                    }
                } // end Runnable
        ); // end call to runOnUiThread
    }
    // display an AlertDialog when the game ends
    private void showGameOverDialog(final int messageId) {
        // DialogFragment to display quiz stats and start new quiz
        final DialogFragment gameResult =
                new DialogFragment() {
                    // create an AlertDialog and return it
                    @Override
                    public Dialog onCreateDialog(Bundle bundle)
                    {
                        if (bundle == null)
                        {
                            dbHelper.addScore(score);
                            // create dialog displaying String resource for messageId
                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(getActivity());
                            builder.setTitle(getResources().getString(messageId));

                            // display number of shots fired and total time elapsed
                            builder.setMessage(getResources().getString(
                                    R.string.results_format, shotsFired, totalElapsedTime, score));
                            builder.setPositiveButton(R.string.reset_game,
                                    new DialogInterface.OnClickListener()
                                    {
                                        // called when "Reset Game" Button is pressed
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            dialogIsDisplayed = false;
                                            if (gameOver)
                                                newGame(); // set up and start a new game
                                            else
                                                nextLevel();
                                        }
                                    } // end anonymous inner class
                            ); // end call to setPositiveButton

                            return builder.create(); // return the AlertDialog
                        } // end method onCreateDialog
                        else
                            return super.onCreateDialog(bundle);
                    }
                }; // end DialogFragment anonymous inner class

        // in GUI thread, use FragmentManager to display the DialogFragment
        activity.runOnUiThread(
                new Runnable() {
                    public void run() {
                        dialogIsDisplayed = true;
                        gameResult.setCancelable(false); // modal dialog
                        gameResult.show(activity.getFragmentManager(), "results");
                    }
                } // end Runnable
        ); // end call to runOnUiThread
    } // end method showGameOverDialog

    // stops the game; called by CannonGameFragment's onPause method
    public void stopGame() {
        if (cannonThread != null)
            cannonThread.setRunning(false); // tell thread to terminate
    }

    // releases resources; called by CannonGameFragment's onDestroy method
    public void releaseResources() {
        soundPool.release(); // release all resources used by the SoundPool
        soundPool = null;
    }

    // called when surface changes size
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format,
                               int width, int height) {
    }

    // called when surface is first created
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!dialogIsDisplayed) {
            cannonThread = new CannonThread(holder, this); // create thread
            cannonThread.setRunning(true); // start game running
            cannonThread.start(); // start the game loop thread
        }
    }

    // called when the surface is destroyed
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // ensure that thread terminates properly
        boolean retry = true;
        cannonThread.setRunning(false); // terminate cannonThread

        while (retry) {
            try {
                cannonThread.join(); // wait for cannonThread to finish
                retry = false;
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted", e);
            }
        }
    } // end method surfaceDestroyed

    // called when the user touches the screen in this Activity
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // get int representing the type of action which caused this event
        int action = e.getAction();

        // the user user touched the screen or dragged along the screen
        if (action == MotionEvent.ACTION_DOWN ||
                action == MotionEvent.ACTION_MOVE) {
            fireCannonball(e); // fire the cannonball toward the touch point
        }

        return true;
    } // end method onTouchEvent

    @Override
    protected Parcelable onSaveInstanceState()
    {
        //super.onSaveInstanceState();
        Bundle save = new Bundle();
        save.putParcelable("instanceState", super.onSaveInstanceState());
        save.putBooleanArray("hitStates", hitStates);
        save.putDouble("timeLeft", timeLeft);
        save.putInt("targetPiecesHit", targetPiecesHit);
        save.putBoolean("gameOver", gameOver);
        save.putInt("shotsFired", shotsFired);
        save.putDouble("totalElapsedTime", totalElapsedTime);
        save.putDouble("ballSpeedMultiplier", ballSpeedMultiplier);
        save.putDouble("ballSizeMultiplier", ballSizeMultiplier);
        save.putInt("level", level);
        save.putDouble("score", score);

        return save;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {

        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            hitStates = bundle.getBooleanArray("hitStates");
            timeLeft = bundle.getDouble("timeLeft");
            targetPiecesHit = bundle.getInt("targetPiecesHit");
            gameOver = bundle.getBoolean("gameOver");
            shotsFired = bundle.getInt("shotsFired");
            totalElapsedTime = bundle.getDouble("totalElapsedTime");
            ballSizeMultiplier = bundle.getDouble("ballSizeMultiplier");
            ballSpeedMultiplier = bundle.getDouble("ballSpeedMultiplier");
            level = bundle.getInt("level");
            score = bundle.getDouble("score");
            stateRestored = true;

            state = bundle.getParcelable("instanceState");
        }

        super.onRestoreInstanceState(state);

    }

    public boolean getGameOver()
    {
        return gameOver;
    }
} // end class CannonView
