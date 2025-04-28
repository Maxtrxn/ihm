package src.model.game;

public class Boss extends Enemy {
    // ——— Physique du saut ———
    private double velocityY     = 0.0;
    private static final double GRAVITY    = 1800.0;
    private static final double JUMP_SPEED = 800.0;
    private final double groundY;
    private boolean onGround;

    // ——— Cooldown entre deux sauts ———
    private double jumpTimer = 0.0;
    private static final double JUMP_COOLDOWN = 2.5; // en secondes

    // ——— Gestion des hits / disparition ———
    private int hits         = 0;
    private static final int MAX_HITS  = 3;
    private double hitTimer  = 0.0;

    public Boss(double x, double y, double width, double height,
                double speed, double leftBound, double rightBound) {
        super(x, y, width, height, speed, leftBound, rightBound);
        this.groundY   = y;
        this.onGround  = true;
        this.jumpTimer = JUMP_COOLDOWN; // prêt à sauter immédiatement
    }

    /**
     * Met à jour le boss : poursuite horizontale et saut vers le joueur.
     *
     * @param deltaSec Temps écoulé depuis la dernière frame (en secondes)
     * @param player   Référence au joueur, pour cibler ses coordonnées
     */
    public void update(double deltaSec, Player player) {
        // 1) Poursuite horizontale (vers le joueur, dans sa zone)
        double move = getSpeed() * deltaSec;
        if (player.getX() > getX() && getX() + move <= getRightBound()) {
            setX(getX() + move);
        } else if (player.getX() < getX() && getX() - move >= getLeftBound()) {
            setX(getX() - move);
        }

        // 2) Gestion du saut
        jumpTimer += deltaSec;
        if (onGround && jumpTimer >= JUMP_COOLDOWN) {
            velocityY = -JUMP_SPEED;
            onGround  = false;
            jumpTimer = 0.0;
        }

        // 3) Gravité + intégration Y
        velocityY += GRAVITY * deltaSec;
        setY(getY() + velocityY * deltaSec);

        // 4) Atterrissage
        if (getY() >= groundY) {
            setY(groundY);
            velocityY = 0.0;
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

    /** True si le boss doit disparaître. */
    public boolean isDead() {
        return hits >= MAX_HITS;
    }
}
