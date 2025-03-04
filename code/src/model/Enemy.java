package src.model;

public class Enemy {
    private double x, y, width, height, speed;
    private double leftBound, rightBound;
    private boolean movingRight = true;

    public Enemy(double x, double y, double width, double height, double speed, double leftBound, double rightBound) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void update() {
        if (movingRight) {
            x += speed;
            if (x >= rightBound) movingRight = false;
        } else {
            x -= speed;
            if (x <= leftBound) movingRight = true;
        }
    }
}