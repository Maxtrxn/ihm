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
import src.levels.Level2;
import src.levels.Level3;
import src.levels.SpaceshipLevel;
import src.model.Player;
import src.model.Platform;
import src.model.Enemy;
import src.model.Projectile;
import src.model.Decoration;
import src.view.GameView;

public class GameController {
    private static final double GRAVITY = 1800.0;

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

    /**
     * Une itération de la boucle (~60FPS), délègue le passage de niveau à Game.nextLevel().
     * @param deltaSec  Temps écoulé (s) depuis la dernière frame
     */
    private void update(double deltaSec) {
        boolean isSpaceship = level instanceof SpaceshipLevel;

        // Calcul du déplacement horizontal en px pour ce frame
        double dx    = 0.0;
        double speed = player.getSpeed() * 1.5; // px/sec
        if (left)  { dx -= speed * deltaSec; player.setFacingRight(false); }
        if (right) { dx += speed * deltaSec; player.setFacingRight(true); }

        // Mode vaisseau ou plateforme
        if (isSpaceship) {
            updateSpaceship(dx, deltaSec);
            updateProjectiles(deltaSec);
        } else {
            updatePlatform(dx, deltaSec);
        }

        // Passage de niveau si on dépasse X = 1600
        if (player.getX() > level.getLevelWidth()) {
            javafx.application.Platform.runLater(() -> game.nextLevel());
        }

        // Caméra + rendu
        updateCamera(isSpaceship);
        render(isSpaceship);
    }



    /** @param dx      déplacement horizontal pour ce frame
     *  @param deltaSec  temps écoulé (s)
     */
    private void updateSpaceship(double dx, double deltaSec) {
        double dy = 0.0;
        double shipSpeed = player.getSpeed() * 1.5; // px/sec
        if (up)   dy -= shipSpeed * deltaSec;
        if (down) dy += shipSpeed * deltaSec;
        player.move(dx, dy);
    }



    /** @param deltaSec  temps écoulé (s) */
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


    /** Tire un nouveau projectile depuis la position du joueur. */
    private void fireProjectile() {
        double offsetX = player.isFacingRight()
                       ? player.getWidth()
                       : -10;
        double px = player.getX() + offsetX;
        double py = player.getY() + player.getHeight() / 2.0;
        projectiles.add(new Projectile(px, py, player.isFacingRight()));
    }




    /**
     * @param dx       déplacement horizontal pour ce frame (px)
     * @param deltaSec temps écoulé (s) depuis la dernière frame
     */
    private void updatePlatform(double dx, double deltaSec) {
        // Mémorise la position Y AVANT le déplacement
        double oldY = player.getY();

        // déclenchement du saut : on passe à -600 px/s (~-10 px/frame @60FPS)
        if (jumping && player.canJump() && !jetpack) {
            player.setVelocityY(-603.0);
            player.setOnGround(false);
            player.incrementJumps();
            jumping = false;
        }

        // jetpack : vitesse de montée constante (px/s)
        if (jetpack && player.isJetpackActive()) {
            player.setVelocityY(-300.0);
        } else {
            // intégration de la gravité en px/s²
            player.setVelocityY(player.velocityY + GRAVITY * deltaSec);
        }

        // déplacement vertical selon deltaSec
        double dy = player.velocityY * deltaSec;
        player.move(dx, dy);

        // collisions plateformes en tenant compte de oldY
        handlePlatformCollisions(oldY);

        // gestion des ennemis
        handleEnemies(deltaSec);

        // reset si chute
        if (player.getY() > levelHeight) {
            resetPlayerPosition();
        }
    }





    /**
     * Gère les collisions plateforme en s’assurant que le joueur
     * venait bien d’au-dessus de la plateforme.
     *
     * @param oldY position Y du joueur AVANT le move(...)
     */
    private void handlePlatformCollisions(double oldY) {
        // 1) Reset fragile platforms
        for (Platform p : platforms) {
            if (p instanceof src.model.platforms.FragilePlatform) {
                ((src.model.platforms.FragilePlatform) p).resetStep(player);
            }
        }

        // 2) Test d’atterrissage
        for (Platform p : platforms) {
            double platformTop     = p.getY();
            double playerBottomNow = player.getY() + player.getHeight();
            double playerBottomOld = oldY            + player.getHeight();

            if (player.intersects(p)
                && player.velocityY > 0.0
                && playerBottomOld <= platformTop
            ) {
                // Pose le joueur exactement sur la plateforme
                player.setY(platformTop - player.getHeight());
                player.velocityY = 0.0;
                player.setOnGround(true);
                player.resetJumps();

                // Si plateforme fragile, incrémente son état
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


    /**
     * @param deltaSec  temps écoulé (s) depuis la dernière frame
     */
    private void handleEnemies(double deltaSec) {
        List<Enemy> toRemove = new ArrayList<>();
        for (Enemy enemy : enemies) {
            // déplacement de l’ennemi à la bonne vitesse (px/s)
            enemy.update(deltaSec);

            if (player.landsOn(enemy)) {
                toRemove.add(enemy);
                player.setVelocityY(-600.0); // rebond identique au saut
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
        cameraX = Math.max(0, Math.min(cameraX, level.getLevelWidth() - cw));

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
