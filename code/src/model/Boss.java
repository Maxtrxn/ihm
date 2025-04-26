package src.model;

public class Boss extends Enemy {
    private int hits = 0;
    private static final int MAX_HITS = 3;
    private double hitTimer = 0.0; // en secondes

    public Boss(double x, double y, double width, double height,
                double speed, double leftBound, double rightBound) {
        super(x, y, width, height, speed, leftBound, rightBound);
    }

    @Override
    public void update(double deltaSec) {
        super.update(deltaSec);
        if (hitTimer > 0) {
            hitTimer = Math.max(0, hitTimer - deltaSec);
        }
    }

    /** Appelée quand le joueur saute dessus */
    public void hit() {
        hits++;
        hitTimer = 0.2; // flash rouge pendant 0,2 s
    }

    /** True si le boss doit disparaître */
    public boolean isDead() {
        return hits >= MAX_HITS;
    }

    /** True si on est en plein flash rouge */
    public boolean isHit() {
        return hitTimer > 0;
    }
}
