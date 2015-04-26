package com.dlefh3.android.cannongame;

import android.graphics.Point;

/**
 * CannonBall class holds the data for a cannon ball
 * in the game
 */
public class CannonBall {

    private static final int MULTIPLICATIVE_INCREASE = 0;
    private static final int ADDITIVE_INCREASE = 1;

    private Point location; // cannonball image's upper-left corner
    private int velocityX; // cannonball's x velocity
    private int velocityY; // cannonball's y velocity
    private boolean onScreen; // whether cannonball on the screen
    private int radius; // cannonball's radius
    private int speed; // cannonball's speed

    public CannonBall()
    {
        location = new Point();
        location.x = 0;
        location.y = 0;
        velocityX = 0;
        velocityY = 0;
        onScreen = false;
        radius = 0;
        speed = 0;
    }
    public int getX() {
        return location.x;
    }
    public int getY() {
        return location.y;
    }
    public void setX(int x)
    {
        this.location.x = x;
    }
    public void setY(int y)
    {
        this.location.y = y;
    }
    public Point getLocation()
    {
        return location;
    }
    public void setLocation(Point p) {
        this.location = p;
    }

    public int getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(int velocityX) {
        this.velocityX = velocityX;
    }

    public int getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(int velocityY) {
        this.velocityY = velocityY;
    }

    public boolean isOnScreen() {
        return onScreen;
    }

    public void setOnScreen(boolean onScreen) {
        this.onScreen = onScreen;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
