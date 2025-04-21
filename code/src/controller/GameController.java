package src.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.image.Image;

import src.Game;
import src.levels.Level;
import src.levels.Level1;
import src.levels.SpaceshipLevel;
import src.model.Player;
import src.model.Platform;
import src.model.Enemy;
import src.view.GameView;

public class GameController {
    private static final double GRAVITY = 0.5;

    // Flags de déplacement
    private boolean left, right, up, down, jumping, jetpack;

    private final Player player;
    private final List<Platform> platforms;
    private final List<Enemy> enemies;
    private final GameView view;
    private final Game game;
    private final Level level;

    private final double initialPlayerX, initialPlayerY;
    private Timer jetpackTimer;

    // Dimensions logiques du niveau
    private final double levelWidth  = 3000.0;
    private final double levelHeight = 600.0;

    // Caméra
    private double cameraX = 0.0, cameraY = 0.0;

    // Boucle de jeu JavaFX
    private AnimationTimer gameLoop;

    public GameController(Player player,
                          List<Platform> platforms,
                          List<Enemy> enemies,
                          GameView view,
                          Game game,
                          Level level) {
        this.player          = player;
        this.platforms       = platforms;
        this.enemies         = enemies;
        this.view            = view;
        this.game            = game;
        this.level           = level;
        this.initialPlayerX  = player.getX();
        this.initialPlayerY  = player.getY();
    }

    /** Lie les événements clavier aux flags de déplacement. */
    public void handleInput(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.LEFT)  this.left  = true;
            if (e.getCode() == KeyCode.RIGHT) this.right = true;
            if (e.getCode() == KeyCode.UP)    this.up    = true;
            if (e.getCode() == KeyCode.DOWN)  this.down  = true;

            if (e.getCode() == KeyCode.SPACE) {
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

    /** Démarre la boucle de jeu (AnimationTimer). */
    public void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };
        gameLoop.start();
    }

    /** Arrête la boucle de jeu en cours. */
    public void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    /** Mise à jour (~60 FPS) : route vers le mode plateforme ou vaisseau. */
    private void update() {
        boolean isSpaceship = (level instanceof SpaceshipLevel);

        // Calcul du dx commun
        double dx    = 0.0;
        double speed = player.getSpeed() * 1.5;
        if (left)  { dx -= speed; player.setFacingRight(false); }
        if (right) { dx += speed; player.setFacingRight(true); }

        if (isSpaceship) {
            updateSpaceship(dx, speed);
        } else {
            updatePlatform(dx);
        }

        // Passage de niveau si on dépasse X = 1600
        if (player.getX() > 1600.0) {
            javafx.application.Platform.runLater(() -> {
                if (level instanceof Level1) {
                    game.loadSpaceshipLevel();
                } else {
                    game.nextLevel();
                }
            });
        }

        // Caméra et rendu
        updateCamera(isSpaceship);
        render(isSpaceship);
    }

    /** Déplacement en mode vaisseau (shoot'em up). */
    private void updateSpaceship(double dx, double speed) {
        double dy = 0.0;
        if (up)   dy -= speed;
        if (down) dy += speed;
        player.move(dx, dy);
    }

    /** Déplacement en mode plateforme (gravité + collisions). */
    private void updatePlatform(double dx) {
        // Saut / jetpack
        if (jumping && player.canJump() && !jetpack) {
            player.velocityY = -10.0;
            player.incrementJumps();
            jumping = false;
        }
        if (jetpack && player.isJetpackActive()) {
            player.velocityY = -5.0;
        } else {
            player.velocityY += GRAVITY;
        }

        double dy = player.velocityY * 2.0;
        player.move(dx, dy);

        handlePlatformCollisions();
        handleEnemies();

        // Reset si on tombe sous le niveau
        if (player.getY() > levelHeight) {
            resetPlayerPosition();
        }
    }

    /** Collisions plateformes (mode plateforme). */
    private void handlePlatformCollisions() {
        for (Platform p : platforms) {
            if (p instanceof src.model.platforms.FragilePlatform) {
                ((src.model.platforms.FragilePlatform) p).resetStep(player);
            }
        }
        for (Platform p : platforms) {
            if (player.intersects(p) && player.velocityY > 0.0) {
                player.setY(p.getY() - player.getHeight());
                player.velocityY = 0.0;
                player.setOnGround(true);
                player.resetJumps();
                if (p instanceof src.model.platforms.FragilePlatform) {
                    src.model.platforms.FragilePlatform fp = (src.model.platforms.FragilePlatform) p;
                    if (!fp.isBroken()) {
                        fp.step(player);
                    }
                }
            }
        }
    }

    /** Gestion des ennemis (mode plateforme). */
    private void handleEnemies() {
        List<Enemy> toRemove = new ArrayList<>();
        for (Enemy enemy : enemies) {
            enemy.update();
            if (player.landsOn(enemy)) {
                toRemove.add(enemy);
                player.velocityY = -10.0;
            } else if (player.intersects(enemy)) {
                resetPlayerPosition();
            }
        }
        enemies.removeAll(toRemove);
    }

    /**
     * Met à jour la caméra selon le mode :
     * - Alignement vertical automatique si la fenêtre est plus haute que le niveau.
     */
    private void updateCamera(boolean isSpaceship) {
        double cw = view.getCanvasWidth();
        double ch = view.getCanvasHeight();

        // --- AXE X ---
        double targetX = player.getX() - cw / 2.0;
        cameraX += 0.1 * (targetX - cameraX);
        cameraX = Math.max(0, Math.min(cameraX, levelWidth - cw));

        // --- AXE Y ---
        if (isSpaceship) {
            cameraY = 0;
        } else if (ch > levelHeight) {
            // Fenêtre trop haute → aligner le bas du monde sur le bas de la fenêtre
            cameraY = levelHeight - ch;  // valeur négative ou 0
        } else {
            double targetY = player.getY() - ch / 2.0;
            cameraY += 0.1 * (targetY - cameraY);
            cameraY = Math.max(0, Math.min(cameraY, levelHeight - ch));
        }

        view.cameraXProperty().set(cameraX);
        view.cameraYProperty().set(cameraY);
    }

    /** Appelle la vue pour dessiner tous les éléments. */
    private void render(boolean isSpaceship) {
        ArrayList<Image> imgs = new ArrayList<>();
        ArrayList<Double[]> poses = new ArrayList<>();
        for (Platform p : platforms) {
            imgs.add(p.getTexture());
            poses.add(new Double[]{p.getX(), p.getY(), p.getWidth(), p.getHeight()});
        }
        ArrayList<Double[]> enemyPos = new ArrayList<>();
        for (Enemy e : enemies) {
            enemyPos.add(new Double[]{e.getX(), e.getY(), e.getWidth(), e.getHeight()});
        }

        boolean isJumping = !player.onGround;
        view.draw(
            level.getBackgroundImage(),
            player.getX(), player.getY(),
            player.getWidth(), player.getHeight(),
            player.isWalking(), player.isFacingRight(),
            isJumping,
            isSpaceship,
            imgs, poses, enemyPos
        );
    }

    /** Remet le joueur à sa position initiale. */
    private void resetPlayerPosition() {
        player.setX(initialPlayerX);
        player.setY(initialPlayerY);
        player.velocityY    = 0.0;
        player.setOnGround(true);
        player.resetJumps();
        player.setJetpackActive(false);
        if (jetpackTimer != null) {
            jetpackTimer.cancel();
            jetpackTimer = null;
        }
    }

    /** Réinitialise tous les flags d'état du joueur. */
    public void resetPlayerState() {
        left = right = up = down = jumping = jetpack = false;
        player.setJetpackActive(false);
        player.resetJumps();
        player.setVelocityY(0.0);
        player.setOnGround(true);
        if (jetpackTimer != null) {
            jetpackTimer.cancel();
            jetpackTimer = null;
        }
    }

    /** Tâche interne pour activer le jetpack après 500 ms (mode plateforme). */
    private class JetpackTask extends TimerTask {
        @Override
        public void run() {
            jetpack = true;
            player.setJetpackActive(true);
        }
    }
}
