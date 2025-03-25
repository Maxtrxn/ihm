package src.controller;

import java.util.Timer;
import java.util.TimerTask;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.image.Image;

import src.Game;
import src.levels.Level;
import src.levels.Level1;
import src.levels.SpaceshipLevel;
import src.model.Player;
import src.model.Platform;
import src.model.Enemy;
import src.view.GameView;

/**
 * Contrôleur gérant deux modes :
 * - Mode "plateforme" (gravité, collisions)
 * - Mode "vaisseau" (pas de gravité, pas de collisions, déplacements libres).
 */
public class GameController {

    private static final double GRAVITY = 0.5;

    // Flags de déplacement
    private boolean left;
    private boolean right;
    private boolean up;
    private boolean down;
    private boolean jumping;
    private boolean jetpack;

    private Player player;
    private List<Platform> platforms;
    private List<Enemy> enemies;
    private GameView view;
    private Game game;
    private Level level;

    private double initialPlayerX;
    private double initialPlayerY;
    private Timer jetpackTimer;

    // Dimensions logiques du niveau
    private double levelWidth  = 3000.0;
    private double levelHeight = 600.0;

    // Caméra
    private double cameraX = 0.0;
    private double cameraY = 0.0;

    public GameController(Player player,
                          List<Platform> platforms,
                          List<Enemy> enemies,
                          GameView view,
                          Game game,
                          Level level) {
        this.player = player;
        this.platforms = platforms;
        this.enemies = enemies;
        this.view = view;
        this.game = game;
        this.level = level;
        this.initialPlayerX = player.getX();
        this.initialPlayerY = player.getY();
    }

    /**
     * Gère les touches clavier.
     */
    public void handleInput(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.LEFT)  this.left  = true;
            if (e.getCode() == KeyCode.RIGHT) this.right = true;
            if (e.getCode() == KeyCode.UP)    this.up    = true;
            if (e.getCode() == KeyCode.DOWN)  this.down  = true;

            if (e.getCode() == KeyCode.SPACE) {
                // Espace = saut en mode plateforme
                this.jumping = true;
                if (this.jetpackTimer == null) {
                    this.jetpackTimer = new Timer(true);
                    this.jetpackTimer.schedule(new JetpackTask(), 500L);
                }
            }
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.LEFT)  this.left  = false;
            if (e.getCode() == KeyCode.RIGHT) this.right = false;
            if (e.getCode() == KeyCode.UP)    this.up    = false;
            if (e.getCode() == KeyCode.DOWN)  this.down  = false;

            if (e.getCode() == KeyCode.SPACE) {
                this.jumping = false;
                this.jetpack = false;
                this.player.setJetpackActive(false);
                if (this.jetpackTimer != null) {
                    this.jetpackTimer.cancel();
                    this.jetpackTimer = null;
                }
            }
        });
    }

    /**
     * Lance la boucle de jeu.
     */
    public void startGameLoop() {
        new GameLoopThread().start();
    }

    /**
     * Boucle principale appelée ~60 fois/s.
     */
    private void update() {
        boolean isSpaceship = (this.level instanceof SpaceshipLevel);

        // Mode vaisseau -> aucune gravité, aucun handlePlatformCollisions
        // Mode plateforme -> gravité, collisions

        double dx = 0.0;
        double speed = player.getSpeed() * 1.5;

        if (this.left)  { dx -= speed; player.setFacingRight(false); }
        if (this.right) { dx += speed; player.setFacingRight(true);  }

        if (isSpaceship) {
            // --------------------------------------------------------
            // MODE VAISSEAU : Pas de gravité, pas de collisions
            // --------------------------------------------------------
            double dy = 0.0;
            if (this.up)   dy -= speed;
            if (this.down) dy += speed;

            // On applique directement le mouvement
            this.player.move(dx, dy);

        } else {
            // --------------------------------------------------------
            // MODE PLATEFORME : gravité, saut, collisions
            // --------------------------------------------------------
            if (this.jumping && this.player.canJump() && !this.jetpack) {
                this.player.velocityY = -10.0;
                this.player.incrementJumps();
                this.jumping = false;
            }
            if (this.jetpack && this.player.isJetpackActive()) {
                this.player.velocityY = -5.0;
            } else {
                this.player.velocityY += GRAVITY;
            }
            double dy = this.player.velocityY * 2.0;
            this.player.move(dx, dy);

            handlePlatformCollisions();
            handleEnemies();

            // Tombe hors du niveau ?
            if (this.player.getY() > levelHeight) {
                resetPlayerPosition();
            }
        }

        // Avancement du niveau
        if (this.player.getX() > 1600.0) {
            javafx.application.Platform.runLater(() -> {
                if (this.level instanceof Level1) {
                    this.game.loadSpaceshipLevel();
                } else {
                    this.game.nextLevel();
                }
            });
        }

        // Mise à jour de la caméra
        updateCamera(isSpaceship);

        // Préparation pour l'affichage
        ArrayList<Image> platformImages = new ArrayList<>();
        ArrayList<Double[]> platformPositions = new ArrayList<>();
        for (Platform p : this.platforms) {
            platformImages.add(p.getTexture());
            platformPositions.add(new Double[]{p.getX(), p.getY(), p.getWidth(), p.getHeight()});
        }
        ArrayList<Double[]> enemyPositions = new ArrayList<>();
        for (Enemy enemy : this.enemies) {
            enemyPositions.add(new Double[]{enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight()});
        }

        boolean isJumping = !this.player.onGround;

        // Dessin
        this.view.draw(
            this.level.getBackgroundImage(),
            this.player.getX(), this.player.getY(),
            this.player.getWidth(), this.player.getHeight(),
            this.player.isWalking(), this.player.isFacingRight(),
            isJumping,
            isSpaceship,
            platformImages, platformPositions, enemyPositions
        );

        // Debug pour voir la coordonnée Y
        if (isSpaceship && (this.up || this.down)) {
            System.out.println("[SPACESHIP DEBUG] x=" + player.getX() + " y=" + player.getY());
        }
    }

    /**
     * Collisions plateformes (uniquement en mode plateforme).
     */
    private void handlePlatformCollisions() {
        // (Si fragilePlatform)
        for (Platform p : this.platforms) {
            if (p instanceof src.model.platforms.FragilePlatform) {
                ((src.model.platforms.FragilePlatform) p).resetStep(this.player);
            }
        }
        // Collision par le haut
        for (Platform p : this.platforms) {
            if (this.player.intersects(p) && this.player.velocityY > 0.0) {
                this.player.setY(p.getY() - this.player.getHeight());
                this.player.velocityY = 0.0;
                this.player.setOnGround(true);
                this.player.resetJumps();
                if (p instanceof src.model.platforms.FragilePlatform) {
                    src.model.platforms.FragilePlatform fp = (src.model.platforms.FragilePlatform) p;
                    if (!fp.isBroken()) {
                        fp.step(this.player);
                    }
                }
            }
        }
    }

    /**
     * Collisions ennemis (uniquement en mode plateforme).
     */
    private void handleEnemies() {
        // Si on "tombe" sur un ennemi, on le détruit, sinon on reset le joueur
        // (vous pouvez adapter pour une vraie gestion des PV)
        List<Enemy> toRemove = new ArrayList<>();
        for (Enemy enemy : this.enemies) {
            enemy.update();
            if (this.player.landsOn(enemy)) {
                toRemove.add(enemy);
                this.player.velocityY = -10.0;
            } else if (this.player.intersects(enemy)) {
                resetPlayerPosition();
            }
        }
        this.enemies.removeAll(toRemove);
    }

    /**
     * Mise à jour de la caméra.
     * - En mode vaisseau : on bloque la caméraY à 0 (shoot'em up horizontal).
     * - En mode plateforme : la caméra suit le joueur.
     */
    private void updateCamera(boolean isSpaceship) {
        double canvasWidth = this.view.getCanvasWidth();
        double canvasHeight = this.view.getCanvasHeight();

        double targetX = this.player.getX() - canvasWidth / 2.0;
        double targetY = isSpaceship
            ? 0
            : (this.player.getY() - canvasHeight / 2.0);

        // Lissage
        this.cameraX += 0.1 * (targetX - this.cameraX);
        this.cameraY += 0.1 * (targetY - this.cameraY);

        // Borne en X
        if (this.cameraX < 0) {
            this.cameraX = 0;
        }
        double maxCamX = levelWidth - canvasWidth;
        if (this.cameraX > maxCamX) {
            this.cameraX = maxCamX;
        }

        // Borne en Y seulement si mode plateforme
        if (!isSpaceship) {
            if (this.cameraY < 0) {
                this.cameraY = 0;
            }
            double maxCamY = levelHeight - canvasHeight;
            if (this.cameraY > maxCamY) {
                this.cameraY = maxCamY;
            }
        } else {
            // Mode vaisseau : on fixe la caméraY à 0
            this.cameraY = 0;
        }

        this.view.cameraXProperty().set(this.cameraX);
        this.view.cameraYProperty().set(this.cameraY);
    }

    /**
     * Réinitialise la position du joueur.
     */
    private void resetPlayerPosition() {
        this.player.setX(this.initialPlayerX);
        this.player.setY(this.initialPlayerY);
        this.player.velocityY = 0.0;
        this.player.setOnGround(true);
        this.player.resetJumps();
        this.player.setJetpackActive(false);
        if (this.jetpackTimer != null) {
            this.jetpackTimer.cancel();
            this.jetpackTimer = null;
        }
    }

    /**
     * Remet à zéro les flags de déplacement.
     */
    public void resetPlayerState() {
        this.left = false;
        this.right = false;
        this.up = false;
        this.down = false;
        this.jumping = false;
        this.jetpack = false;
        this.player.setJetpackActive(false);
        this.player.resetJumps();
        this.player.setVelocityY(0.0);
        this.player.setOnGround(true);
        if (this.jetpackTimer != null) {
            this.jetpackTimer.cancel();
            this.jetpackTimer = null;
        }
    }

    /**
     * Tâche pour activer le jetpack après 500ms (mode plateforme).
     */
    private class JetpackTask extends TimerTask {
        @Override
        public void run() {
            jetpack = true;
            player.setJetpackActive(true);
        }
    }

    /**
     * Thread de boucle de jeu.
     */
    private class GameLoopThread extends Thread {
        public GameLoopThread() {
            setDaemon(true);
        }
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    break;
                }
                update();
            }
        }
    }
}
