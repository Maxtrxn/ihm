package src.controller;

import java.util.ArrayList;
import java.util.Iterator;
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
import src.levels.Level3;
import src.levels.SpaceshipLevel;
import src.model.Player;
import src.model.Platform;
import src.model.Enemy;
import src.model.Projectile;
import src.model.Decoration;
import src.view.GameView;

public class GameController {
    private static final double GRAVITY = 0.5;

    // Flags de déplacement
    private boolean left, right, up, down, jumping, jetpack;

    private final Player player;
    private final List<Platform> platforms;
    private final List<Enemy> enemies;
    private final List<Decoration> decorations;
    private final List<Projectile> projectiles = new ArrayList<>();
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

    // Boucle JavaFX
    private AnimationTimer gameLoop;

    public GameController(Player player,
                          List<Platform> platforms,
                          List<Enemy> enemies,
                          List<Decoration> decorations,
                          GameView view,
                          Game game,
                          Level level) {
        this.player          = player;
        this.platforms       = platforms;
        this.enemies         = enemies;
        this.decorations     = decorations;
        this.view            = view;
        this.game            = game;
        this.level           = level;
        this.initialPlayerX  = player.getX();
        this.initialPlayerY  = player.getY();
    }

    /** Lie les touches aux flags, et gère tirs vs saut. */
    public void handleInput(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.LEFT)  left  = true;
            if (e.getCode() == KeyCode.RIGHT) right = true;
            if (e.getCode() == KeyCode.UP)    up    = true;
            if (e.getCode() == KeyCode.DOWN)  down  = true;

            if (e.getCode() == KeyCode.SPACE) {
                if (level instanceof SpaceshipLevel) {
                    fireProjectile();
                } else {
                    jumping = true;
                    if (jetpackTimer == null) {
                        jetpackTimer = new Timer(true);
                        jetpackTimer.schedule(new JetpackTask(), 500L);
                    }
                }
            }
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.LEFT)  left  = false;
            if (e.getCode() == KeyCode.RIGHT) right = false;
            if (e.getCode() == KeyCode.UP)    up    = false;
            if (e.getCode() == KeyCode.DOWN)  down  = false;

            if (e.getCode() == KeyCode.SPACE && !(level instanceof SpaceshipLevel)) {
                jumping = false;
                jetpack = false;
                player.setJetpackActive(false);
                if (jetpackTimer != null) {
                    jetpackTimer.cancel();
                    jetpackTimer = null;
                }
            }
        });
    }

    /** Démarre la boucle via AnimationTimer. */
    public void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };
        gameLoop.start();
    }

    /** Stoppe proprement la boucle en cours. */
    public void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    /** Une itération de la boucle (~60FPS). */
    private void update() {
        boolean isSpaceship = (level instanceof SpaceshipLevel);

        // dx commun
        double dx    = 0.0;
        double speed = player.getSpeed() * 1.5;
        if (left)  { dx -= speed; player.setFacingRight(false); }
        if (right) { dx += speed; player.setFacingRight(true); }

        if (isSpaceship) {
            updateSpaceship(dx, speed);
            updateProjectiles();
        } else {
            updatePlatform(dx);
        }

        // Passage de niveau si on dépasse X=1600
        if (player.getX() > 1600.0) {
            javafx.application.Platform.runLater(() -> {
                if (level instanceof Level1) {
                    game.loadSpaceshipLevel();
                } else if (level instanceof SpaceshipLevel) {
                    // Après le vaisseau → Level 3
                    game.loadLevel(new Level3(player));
                } else {
                    game.nextLevel();
                }
            });
        }

        // Caméra + dessin
        updateCamera(isSpaceship);
        render(isSpaceship);
    }

    /** Déplace le joueur en mode vaisseau. */
    private void updateSpaceship(double dx, double speed) {
        double dy = 0.0;
        if (up)   dy -= speed;
        if (down) dy += speed;
        player.move(dx, dy);
    }

    /** Met à jour les projectiles (mouvement, collisions, retrait). */
    private void updateProjectiles() {
        Iterator<Projectile> pit = projectiles.iterator();
        while (pit.hasNext()) {
            Projectile p = pit.next();
            p.update();
            if (p.isOutOfBounds(levelWidth)) {
                pit.remove();
                continue;
            }
            Iterator<Enemy> eit = enemies.iterator();
            while (eit.hasNext()) {
                Enemy enemy = eit.next();
                if (p.intersects(enemy)) {
                    eit.remove();
                    pit.remove();
                    break;
                }
            }
        }
    }

    /** Tire un nouveau projectile depuis la position du joueur. */
    private void fireProjectile() {
        double offsetX = player.isFacingRight()
                       ? player.getWidth()
                       : -10;
        double px = player.getX() + offsetX;
        double py = player.getY() + player.getHeight() / 2.0;
        projectiles.add(new Projectile(px, py, player.isFacingRight()));
    }

    /** Logique du mode plateforme (gravité + collisions). */
    private void updatePlatform(double dx) {
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

        if (player.getY() > levelHeight) {
            resetPlayerPosition();
        }
    }

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
                    src.model.platforms.FragilePlatform fp =
                        (src.model.platforms.FragilePlatform) p;
                    if (!fp.isBroken()) {
                        fp.step(player);
                    }
                }
            }
        }
    }

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

    private void updateCamera(boolean isSpaceship) {
        double cw = view.getCanvasWidth();
        double ch = view.getCanvasHeight();

        // X
        double targetX = player.getX() - cw / 2.0;
        cameraX += 0.1 * (targetX - cameraX);
        cameraX = Math.max(0, Math.min(cameraX, levelWidth - cw));

        // Y
        if (isSpaceship) {
            cameraY = 0;
        } else if (ch > levelHeight) {
            cameraY = levelHeight - ch;
        } else {
            double targetY = player.getY() - ch / 2.0;
            cameraY += 0.1 * (targetY - cameraY);
            cameraY = Math.max(0, Math.min(cameraY, levelHeight - ch));
        }

        view.cameraXProperty().set(cameraX);
        view.cameraYProperty().set(cameraY);
    }

    /** Dessine décors, plateformes, ennemis, projectiles, joueur/vaisseau. */
    private void render(boolean isSpaceship) {
        // Décorations
        List<Image> decoImgs = new ArrayList<>();
        List<Double[]> posDeco = new ArrayList<>();
        for (Decoration d : decorations) {
            decoImgs.add(d.getTexture());
            posDeco.add(new Double[]{ d.getX(), d.getY(), d.getWidth(), d.getHeight() });
        }

        // Plateformes
        List<Image> imgs     = new ArrayList<>();
        List<Double[]> posPl = new ArrayList<>();
        for (Platform p : platforms) {
            imgs.add(p.getTexture());
            posPl.add(new Double[]{ p.getX(), p.getY(), p.getWidth(), p.getHeight() });
        }

        // Ennemis
        List<Double[]> posEn = new ArrayList<>();
        for (Enemy e : enemies) {
            posEn.add(new Double[]{ e.getX(), e.getY(), e.getWidth(), e.getHeight() });
        }

        // Projectiles
        List<Double[]> posProj = new ArrayList<>();
        for (Projectile p : projectiles) {
            posProj.add(new Double[]{ p.getX(), p.getY(), p.getWidth(), p.getHeight() });
        }

        boolean isJumping = !player.onGround;
        view.draw(
            level.getBackgroundImage(),
            player.getX(), player.getY(),
            player.getWidth(), player.getHeight(),
            player.isWalking(), player.isFacingRight(),
            isJumping,
            isSpaceship,
            decoImgs, posDeco,
            imgs, posPl,
            posEn,
            posProj
        );
    }

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

    /** Tâche interne pour activer le jetpack après 500 ms (mode plateforme). */
    private class JetpackTask extends TimerTask {
        @Override
        public void run() {
            jetpack = true;
            player.setJetpackActive(true);
        }
    }
}
