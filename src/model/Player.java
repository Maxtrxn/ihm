package src.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Player {
    private DoubleProperty x = new SimpleDoubleProperty();
    private DoubleProperty y = new SimpleDoubleProperty();
    private double width = 50;
    private double height = 50;
    public double velocityY = 0;
    public boolean onGround = false;
    private int jumps = 0;
    private static final int MAX_JUMPS = 2;
    private double speed = 3.0;
    private boolean jetpackActive = false;

    public Player(double x, double y) {
        this.x.set(x);
        this.y.set(y);
    }

    public double getX() {
        return x.get();
    }

    public void setX(double x) {
        this.x.set(x);
    }

    public DoubleProperty xProperty() {
        return x;
    }

    public double getY() {
        return y.get();
    }

    public void setY(double y) {
        this.y.set(y);
    }

    public DoubleProperty yProperty() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void move(double dx, double dy) {
        setX(getX() + dx);
        setY(getY() + dy);
    }

    public boolean intersects(Platform platform) {
        return getX() < platform.getX() + platform.getWidth() &&
               getX() + getWidth() > platform.getX() &&
               getY() < platform.getY() + platform.getHeight() &&
               getY() + getHeight() > platform.getY();
    }

    public boolean intersects(Enemy enemy) {
        return getX() < enemy.getX() + enemy.getWidth() &&
               getX() + getWidth() > enemy.getX() &&
               getY() < enemy.getY() + enemy.getHeight() &&
               getY() + getHeight() > enemy.getY();
    }

    public boolean landsOn(Enemy enemy) {
        return getX() < enemy.getX() + enemy.getWidth() &&
               getX() + getWidth() > enemy.getX() &&
               getY() + getHeight() >= enemy.getY() &&
               getY() + getHeight() <= enemy.getY() + enemy.getHeight() / 2 &&
               velocityY > 0;
    }

    public int getJumps() {
        return jumps;
    }

    public void incrementJumps() {
        jumps++;
    }

    public void resetJumps() {
        jumps = 0;
    }

    public boolean canJump() {
        return jumps < MAX_JUMPS;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public boolean isJetpackActive() {
        return jetpackActive;
    }

    public void setJetpackActive(boolean jetpackActive) {
        this.jetpackActive = jetpackActive;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }
}