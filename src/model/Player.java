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
}