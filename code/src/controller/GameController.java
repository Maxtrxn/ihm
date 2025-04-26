// src/controller/GameController.java
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
import src.levels.SpaceshipLevel;
import src.model.Player;
import src.model.Platform;
import src.model.Enemy;
import src.model.Boss;
import src.model.Projectile;
import src.model.Decoration;
import src.view.GameView;

public class GameController {
    private static final double GRAVITY = 1800.0;
    private static final double SHIP_SCROLL_SPEED = 200.0; // px/s

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

    // Dimensions “logiques” du niveau
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
            private long lastTime = 0;
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                double deltaSec = (now - lastTime) / 1_000_000_000.0;
                update(deltaSec);
                lastTime = now;
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

    /** Itération de la boucle (~60FPS). */
    private void update(double deltaSec) {
        boolean isShip = (level instanceof SpaceshipLevel);

        // dx selon input
        double dx    = 0.0;
        double speed = player.getSpeed() * 1.5; // px/s
        if (left)  { dx -= speed * deltaSec; player.setFacingRight(false); }
        if (right) { dx += speed * deltaSec; player.setFacingRight(true); }

        if (isShip) {
            // Mouvement du vaisseau en mode spaceship
            updateSpaceship(dx, deltaSec);
            updateProjectiles(deltaSec);

            // Défilement auto de la caméra
            cameraX += SHIP_SCROLL_SPEED * deltaSec;
            cameraX = Math.min(cameraX, levelWidth - view.getCanvasWidth());

            // Contrainte du joueur dans la zone visible
            double minX    = cameraX;
            double maxXpos = cameraX + view.getCanvasWidth() - player.getWidth();
            if (player.getX() < minX)    player.setX(minX);
            if (player.getX() > maxXpos) player.setX(maxXpos);

        } else {
            // Mode plateforme
            updatePlatform(dx, deltaSec);
        }

        // Passage de niveau automatique
        if (player.getX() > 1600.0) {
            javafx.application.Platform.runLater(game::nextLevel);
        }

        // Mise à jour caméra & rendu
        updateCamera(isShip);
        render(isShip);
    }

    /** Update en mode spaceship (déplacement libre). */
    private void updateSpaceship(double dx, double deltaSec) {
        double dy = 0.0;
        if (up)   dy -= player.getSpeed() * 1.5 * deltaSec;
        if (down) dy += player.getSpeed() * 1.5 * deltaSec;
        player.move(dx, dy);
    }

    /** Met à jour projectiles (mouvement + collisions). */
    private void updateProjectiles(double deltaSec) {
        Iterator<Projectile> pit = projectiles.iterator();
        while (pit.hasNext()) {
            Projectile p = pit.next();
            p.update(deltaSec);
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

    /** Tire un projectile (mode spaceship). */
    private void fireProjectile() {
        double offsetX = player.isFacingRight() ? player.getWidth() : -10;
        double px = player.getX() + offsetX;
        double py = player.getY() + player.getHeight() / 2.0;
        projectiles.add(new Projectile(px, py, player.isFacingRight()));
    }

    /** Update en mode plateforme (gravité + collisions). */
    private void updatePlatform(double dx, double deltaSec) {
        double oldY = player.getY();

        if (jumping && player.canJump() && !jetpack) {
            player.setVelocityY(-603.0);
            player.setOnGround(false);
            player.incrementJumps();
            jumping = false;
        }

        if (jetpack && player.isJetpackActive()) {
            player.setVelocityY(-300.0);
        } else {
            player.setVelocityY(player.velocityY + GRAVITY * deltaSec);
        }

        double dy = player.velocityY * deltaSec;
        player.move(dx, dy);

        handlePlatformCollisions(oldY);
        handleEnemies(deltaSec);

        if (player.getY() > levelHeight) {
            resetPlayerPosition();
        }
    }

    /**
     * Gère les collisions plateformes en s’assurant qu’on vient d’en haut,
     * puis supprime les plateformes fragiles cassées.
     */
    private void handlePlatformCollisions(double oldY) {
        // 1) Reset fragile platforms
        for (Platform p : platforms) {
            if (p instanceof src.model.platforms.FragilePlatform) {
                ((src.model.platforms.FragilePlatform) p).resetStep(player);
            }
        }

        // 2) Gestion de l’atterrissage
        for (Platform p : platforms) {
            double top    = p.getY();
            double botNow = player.getY() + player.getHeight();
            double botOld = oldY            + player.getHeight();

            if (player.intersects(p)
                && player.velocityY > 0
                && botOld <= top) {
                player.setY(top - player.getHeight());
                player.velocityY = 0;
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

        // 3) Suppression des plateformes fragiles désormais cassées
        platforms.removeIf(p ->
            p instanceof src.model.platforms.FragilePlatform
            && ((src.model.platforms.FragilePlatform) p).isBroken()
        );
    }

    /** Gère déplacement & collisions ennemis. */
    private void handleEnemies(double deltaSec) {
        List<Enemy> toRemove = new ArrayList<>();
        for (Enemy e : enemies) {
            e.update(deltaSec);
            if (player.landsOn(e)) {
                if (e instanceof Boss) {
                    Boss boss = (Boss) e;
                    boss.hit();
                    if (boss.isDead()) {
                        toRemove.add(boss);
                    }
                    player.setVelocityY(-600.0);
                } else {
                    toRemove.add(e);
                    player.setVelocityY(-600.0);
                }
            } else if (player.intersects(e)) {
                resetPlayerPosition();
            }
        }
        enemies.removeAll(toRemove);
    }

    /** Met à jour la caméra selon le mode. */
    private void updateCamera(boolean isShip) {
        double cw = view.getCanvasWidth();
        double ch = view.getCanvasHeight();

        if (!isShip) {
            double targetX = player.getX() - cw / 2.0;
            cameraX += 0.1 * (targetX - cameraX);
            cameraX = Math.max(0, Math.min(cameraX, levelWidth - cw));
        }

        if (isShip) {
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

    /** Dessine tout : décor, plateformes, ennemis, projectiles, joueur/vaisseau. */
    private void render(boolean isShip) {
        List<Image> decoImgs   = new ArrayList<>();
        List<Double[]> posDeco = new ArrayList<>();
        for (Decoration d : decorations) {
            decoImgs.add(d.getTexture());
            posDeco.add(new Double[]{d.getX(), d.getY(), d.getWidth(), d.getHeight()});
        }
        List<Image> platImgs   = new ArrayList<>();
        List<Double[]> posPl   = new ArrayList<>();
        for (Platform p : platforms) {
            platImgs.add(p.getTexture());
            posPl.add(new Double[]{p.getX(), p.getY(), p.getWidth(), p.getHeight()});
        }
        List<Double[]> posProj = new ArrayList<>();
        for (Projectile p : projectiles) {
            posProj.add(new Double[]{p.getX(), p.getY(), p.getWidth(), p.getHeight()});
        }

        boolean isJumping = !player.onGround;
        view.draw(
            level.getBackgroundImage(),
            player.getX(), player.getY(),
            player.getWidth(), player.getHeight(),
            player.isWalking(), player.isFacingRight(),
            isJumping,
            isShip,
            decoImgs, posDeco,
            platImgs, posPl,
            enemies,
            posProj
        );
    }

    /** Remet le joueur en position et état initiaux. */
    private void resetPlayerPosition() {
        player.setX(initialPlayerX);
        player.setY(initialPlayerY);
        player.velocityY    = 0;
        player.setOnGround(true);
        player.resetJumps();
        player.setJetpackActive(false);
        if (jetpackTimer != null) {
            jetpackTimer.cancel();
            jetpackTimer = null;
        }
    }

    /** Réinitialise uniquement les flags (sans arrêter la boucle). */
    public void resetPlayerState() {
        left = right = up = down = jumping = jetpack = false;
        player.setJetpackActive(false);
        player.resetJumps();
        player.setVelocityY(0);
        player.setOnGround(true);
        if (jetpackTimer != null) {
            jetpackTimer.cancel();
            jetpackTimer = null;
        }
    }

    /** TimerTask interne pour activer le jetpack après 500 ms. */
    private class JetpackTask extends TimerTask {
        @Override
        public void run() {
            jetpack = true;
            player.setJetpackActive(true);
        }
    }
}
