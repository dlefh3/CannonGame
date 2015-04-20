package com.fic.android.cannongame;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

// Thread subclass to control the game loop
public class CannonThread extends Thread {
    private SurfaceHolder surfaceHolder; // for manipulating canvas
    private CannonView cannonView; // for manipulating canvas
    private boolean threadIsRunning = true; // running by default

    // initializes the surface holder
    public CannonThread(SurfaceHolder holder, CannonView cannonView) {
        this.surfaceHolder = holder;
        this.cannonView = cannonView;
        setName("CannonThread");
    }

    // changes running state
    public void setRunning(boolean running) {
        threadIsRunning = running;
    }

    // controls the game loop
    @Override
    public void run() {
        Canvas canvas = null; // used for drawing
        long previousFrameTime = System.currentTimeMillis();

        while (threadIsRunning) {
            try {
                // get Canvas for exclusive drawing from this thread
                canvas = surfaceHolder.lockCanvas(null);

                // lock the surfaceHolder for drawing
                synchronized (surfaceHolder) {
                    long currentTime = System.currentTimeMillis();
                    double elapsedTimeMS = currentTime - previousFrameTime;
                    cannonView.totalElapsedTime += elapsedTimeMS / 1000.0;
                    cannonView.updatePositions(elapsedTimeMS); // update game state
                    cannonView.drawGameElements(canvas); // draw using the canvas
                    previousFrameTime = currentTime; // update previous time
                }
            } finally {
                // display canvas's contents on the CannonView
                // and enable other threads to use the Canvas
                if (canvas != null)
                    surfaceHolder.unlockCanvasAndPost(canvas);
            }
        } // end while
    } // end method run
} // end nested class CannonThread