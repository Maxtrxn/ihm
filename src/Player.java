package ihm.src;

public class Player {
    private double x, y, width = 30, height = 30;
    public double velocityY = 0;
    public boolean onGround = false;
    
    public Player(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void move(double dx, double dy) {
        x += dx;
        y += dy;
    }
    
    public boolean intersects(Platform platform) {
        return x < platform.getX() + platform.getWidth() &&
               x + width > platform.getX() &&
               y + height > platform.getY() &&
               y + height < platform.getY() + platform.getHeight();
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}