package src.model.game;

public class Enemy {
    private double x, y, width, height, speed;
    private double leftBound, rightBound;
    private boolean movingRight = true;

    public Enemy(double x, double y, double width, double height, double speed, double leftBound, double rightBound) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed * 60;
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

    /** @param deltaSec  temps écoulé (s) */
    /**
 * @param deltaSec  temps écoulé (s) depuis la dernière frame
 */
    public void update(double deltaSec) {
        double move = speed * deltaSec;
        if (movingRight) {
            x += move;
            if (x >= rightBound) movingRight = false;
        } else {
            x -= move;
            if (x <= leftBound) movingRight = true;
        }
    }


}