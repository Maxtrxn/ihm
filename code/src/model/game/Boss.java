package src.model.game;

import java.util.Random;

public class Boss extends Enemy {
    // ——— Physique du saut ———
    private double velocityY = 0.0;
    private static final double GRAVITY    = 1800.0;
    private static final double JUMP_SPEED = 650.0;
    private final double groundY;
    private boolean onGround;

    // ——— Timer de saut aléatoire ———
    private double timeSinceLastJump = 0.0;
    private static final double JUMP_INTERVAL_MIN = 2.0;
    private static final double JUMP_INTERVAL_MAX = 5.0;
    private double nextJumpTime;
    private final Random rnd = new Random();

    // ——— Gestion des hits ———
    private int hits = 0;
    private static final int MAX_HITS = 3;
    private double hitTimer = 0.0;

    public Boss(double x, double y, double width, double height,
                double speed, double leftBound, double rightBound) {
        super(x, y, width, height, speed, leftBound, rightBound);
        this.groundY  = y;
        this.onGround = true;
        scheduleNextJump();
    }

    private void scheduleNextJump() {
        this.nextJumpTime      = JUMP_INTERVAL_MIN + rnd.nextDouble() * (JUMP_INTERVAL_MAX - JUMP_INTERVAL_MIN);
        this.timeSinceLastJump = 0;
    }

    @Override
    public void update(double deltaSec) {
        // 1) Mouvement horizontal
        super.update(deltaSec);

        // 2) Décision de saut
        timeSinceLastJump += deltaSec;
        if (onGround && timeSinceLastJump >= nextJumpTime) {
            velocityY = -JUMP_SPEED;
            onGround  = false;
            scheduleNextJump();
        }

        // 3) Gravité + intégration
        velocityY += GRAVITY * deltaSec;
        double dy = velocityY * deltaSec;
        setY(getY() + dy);

        // 4) Atterrissage
        if (getY() >= groundY) {
            setY(groundY);
            velocityY = 0;
            onGround  = true;
        }

        // 5) Flash rouge après hit
        if (hitTimer > 0) {
            hitTimer = Math.max(0, hitTimer - deltaSec);
        }
    }

    /** Appelé quand le joueur saute dessus. */
    public void hit() {
        hits++;
        hitTimer = 0.2;
    }

    /** True si on est en plein flash rouge. */
    public boolean isHit() {
        return hitTimer > 0;
    }

    /** True si le boss est éliminé. */
    public boolean isDead() {
        return hits >= MAX_HITS;
    }
}
