package src.model.game;

/**
 * Un projectile tiré par le vaisseau.
 */
public class Projectile {
    private double x, y;
    private static final double WIDTH  = 10;
    private static final double HEIGHT = 4;
    private static final double SPEED  = 360.0;
    private final boolean toRight;

    public Projectile(double x, double y, boolean toRight) {
        this.x       = x;
        this.y       = y;
        this.toRight = toRight;
    }

    /** Avance le projectile d’une frame. */
    /** @param deltaSec  temps écoulé (s) */
    public void update(double deltaSec) {
        x += (toRight ? 1 : -1) * SPEED * deltaSec;
    }


    /** Indique si le projectile est hors limites du niveau. */
    public boolean isOutOfBounds(double levelWidth) {
        return x + WIDTH < 0 || x > levelWidth;
    }

    /** Collision AABB basique avec un ennemi. */
    public boolean intersects(Enemy e) {
        return x < e.getX() + e.getWidth()
            && x + WIDTH  > e.getX()
            && y < e.getY() + e.getHeight()
            && y + HEIGHT > e.getY();
    }

    // Getters
    public double getX()      { return x; }
    public double getY()      { return y; }
    public double getWidth()  { return WIDTH; }
    public double getHeight() { return HEIGHT; }
}
