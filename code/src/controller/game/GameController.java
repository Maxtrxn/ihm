// src/controller/game/GameController.java
package src.controller.game;

import java.io.File;
import java.net.URL;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import src.common.ResourceManager;
import src.model.game.Boss;
import src.model.game.Decoration;
import src.model.game.Enemy;
import src.model.game.GameModel;
import src.model.game.Level;
import src.model.game.Platform;
import src.model.game.Player;
import src.model.game.Projectile;
import src.view.game.GameView;

public class GameController {
    private static final double GRAVITY               = 1800.0;
    private static final double SHIP_SCROLL_SPEED     = 200.0; // px/s
    private static final int    MAX_LIVES             = 3;
    private static final double INVINCIBLE_DURATION   = 1.0;   // seconds

    // Input flags
    private boolean left, right, up, down, jumping, jetpack;
    private boolean spaceshipMode;

    private Player                   player;
    private List<Platform>           platforms;
    private List<Enemy>              enemies;
    private List<Decoration>         decorations;
    private final List<Projectile>   projectiles = new ArrayList<>();
    private final GameView           view;
    private final GameModel          model;
    private Level                    level;

    private double initialPlayerX, initialPlayerY;
    private Timer  jetpackTimer;

    // Camera
    private double cameraX = 0, cameraY = 0;

    // Boss fight
    private boolean inBossFight = false;

    // Lives & invincibility
    private int     lives           = MAX_LIVES;
    private boolean invincible      = false;
    private double  invincibleTimer = 0;

    // Main loop
    private AnimationTimer gameLoop;

    private MediaPlayer musicPlayer;
    private AudioClip musicClip;

    public GameController(Stage primaryStage) {
        this.view  = new GameView(this, primaryStage);
        this.model = new GameModel(this);
        updateLivesDisplay();
    }

    public GameController(Stage primaryStage, String levelName) {
        this.view  = new GameView(this, primaryStage);
        this.model = new GameModel(this, levelName);
        updateLivesDisplay();
    }

    /** Sets the player and its respawn point. */
    public void setPlayer(Player player) {
        this.player = player;
        this.initialPlayerX = player.getX();
        this.initialPlayerY = player.getY();
    }

     /** Charge un nouveau niveau, réinitialise la vie, et joue la musique associée. */
    public void setLevel(Level level) {
        // 1) On met à jour le niveau et ses listes
        this.level       = level;
        this.platforms   = level.getPlatforms();
        this.enemies     = level.getEnemies();
        this.decorations = level.getDecorations();
        this.spaceshipMode = level.isSpaceshipMode();

        // 2) Réinitialisation des vies et de l’invincibilité
        lives      = MAX_LIVES;
        invincible = false;
        updateLivesDisplay();

        // 3) Gestion de la musique de fond
        // Arrête l’ancienne piste si besoin
        if (musicClip != null) {
            musicClip.stop();
        }

        String fname = level.getMusicFileName();
        if (fname != null && !fname.isBlank()) {
            URL url = getClass().getClassLoader().getResource("audio/" + fname);
            if (url != null) {
                musicClip = new AudioClip(url.toExternalForm());
                musicClip.setCycleCount(AudioClip.INDEFINITE);
                musicClip.play();
            } else {
                System.err.println("Audio introuvable : audio/" + fname);
            }
        }
    }

    /** Binds keyboard input to movement flags and actions. */
    public void handleInput() {
        Scene scene = view.getScene();
        scene.setOnKeyPressed(e -> {
            boolean isShip = spaceshipMode;

            if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.Q)  left  = true;
            if (e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.D) right = true;
            if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.Z)    up    = true;
            if (e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.S)  down  = true;

            if (e.getCode() == KeyCode.SPACE) {
                if (isShip) {
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
            boolean isShip = spaceshipMode;

            if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.Q)  left  = false;
            if (e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.D) right = false;
            if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.Z)    up    = false;
            if (e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.S)  down  = false;

            if (e.getCode() == KeyCode.SPACE && !isShip) {
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

    /** Starts the main game loop (~60 FPS). */
    public void startGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastTime = 0;
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double deltaSec = (now - lastTime) / 1_000_000_000.0;
                update(deltaSec);
                lastTime = now;
            }
        };
        gameLoop.start();
    }

    /** Stops the game loop. */
    public void stopGameLoop() {
        if (gameLoop != null) gameLoop.stop();
    }

    /**
     * Main update:
     * - decrement invincibility,
     * - enter boss fight,
     * - handle player movement or spaceship,
     * - collisions/enemies with lives,
     * - camera & render.
     */
    private void update(double deltaSec) {
        // Invincibility countdown
        if (invincible) {
            invincibleTimer -= deltaSec;
            if (invincibleTimer <= 0) {
                invincible = false;
            }
        }

        boolean isShip = spaceshipMode;

        // Enter boss fight
        boolean bossAlive = enemies.stream().anyMatch(e -> e instanceof Boss);
        if (!inBossFight && bossAlive &&
            player.getX() + player.getWidth() >= level.getBossZoneStart()) {
            inBossFight = true;
        }

        // Player or spaceship movement
        double dx = 0;
        double speed = player.getSpeed() * 1.5;
        if (left)  { dx -= speed * deltaSec; player.setFacingRight(false); }
        if (right) { dx += speed * deltaSec; player.setFacingRight(true); }

        if (isShip) {
            updateSpaceship(dx, deltaSec);
            updateProjectiles(deltaSec);
            if (!inBossFight) {
                cameraX += SHIP_SCROLL_SPEED * deltaSec;
                cameraX = Math.min(cameraX, level.getLevelWidth() - view.getCanvasWidth());
            }
            constrainPlayerInView();
        } else {
            updatePlatform(dx, deltaSec);
        }

        // Constrain in boss zone
        if (inBossFight) {
            double startX = level.getBossZoneStart();
            double endX   = level.getBossZoneEnd() - player.getWidth();
            if (player.getX() < startX) player.setX(startX);
            if (player.getX() > endX)   player.setX(endX);
        }

        // Next level
        if (player.getX() + player.getWidth() >= level.getLevelWidth()) {
            javafx.application.Platform.runLater(model::nextLevel);
        }

        // Exit boss fight
        if (inBossFight && !bossAlive) {
            inBossFight = false;
        }

        // Handle enemies & collisions
        handleEnemies(deltaSec);

        // Camera & render
        updateCamera(isShip);
        render(isShip);
    }

    /**
     * Met à jour chaque ennemi et gère les collisions :
     * - Le Boss n'est mis à jour (poursuite + saut) que si inBossFight == true.
     * - Les autres ennemis sont toujours mis à jour.
     * - Si le joueur saute sur un ennemi, il le neutralise.
     * - Si le joueur touche latéralement un ennemi et n'est pas invincible, il perd une vie.
     */
    private void handleEnemies(double deltaSec) {
        List<Enemy> toRemove = new ArrayList<>();

        for (Enemy e : enemies) {
            // 1) Mise à jour de l'ennemi
            if (e instanceof Boss) {
                if (inBossFight) {
                    ((Boss) e).update(deltaSec, player);
                }
                // sinon : le boss reste immobile tant que le joueur n'est pas dans sa zone
            } else {
                e.update(deltaSec);
            }

            // 2) Gestion des collisions
            if (player.landsOn(e)) {
                // Le joueur saute sur l'ennemi
                if (e instanceof Boss) {
                    Boss boss = (Boss) e;
                    boss.hit();
                    if (boss.isDead()) {
                        toRemove.add(boss);
                    }
                } else {
                    toRemove.add(e);
                }
                // Rebond du joueur
                player.setVelocityY(-600.0);

            } else if (!invincible && player.intersects(e)) {
                // Collision latérale : perte de vie si pas en invincibilité
                loseLife();
            }
        }

        // 3) Suppression des ennemis neutralisés
        enemies.removeAll(toRemove);
    }

    /** Decrements a life, triggers invincibility or game-over reset. */
    private void loseLife() {
        lives--;
        invincible = true;
        invincibleTimer = INVINCIBLE_DURATION;
        updateLivesDisplay();

        if (lives == 0) {
            // Game over for this level: reset lives and respawn
            lives = MAX_LIVES;
            updateLivesDisplay();
            resetPlayerPosition();
        }
        // else: remain in place, just invincible
    }

    /** Refreshes the UI display of lives (except in spaceship). */
    private void updateLivesDisplay() {
        // TODO: update your Label or heart icons, e.g.:
        // livesLabel.setText("Lives: " + lives);
    }

    /** Ensures the spaceship-view player stays on screen. */
    private void constrainPlayerInView() {
        double minX = cameraX;
        double maxX = cameraX + view.getCanvasWidth() - player.getWidth();
        if (player.getX() < minX) player.setX(minX);
        if (player.getX() > maxX) player.setX(maxX);
    }

    /** Spaceship free movement. */
    private void updateSpaceship(double dx, double deltaSec) {
        double dy = 0;
        if (up)   dy -= player.getSpeed() * 1.5 * deltaSec;
        if (down) dy += player.getSpeed() * 1.5 * deltaSec;
        player.move(dx, dy);
    }

    /**
     * Déplacement en mode plateforme :
     * 1) collision horizontale « mur »
     * 2) gravité / saut / jetpack
     * 3) collision verticale « sol/plafond » + fragilePlatform
     */
    private void updatePlatform(double dx, double deltaSec) {
        // Réinitialise le flag walking si pas de déplacement horizontal
        if (dx == 0) {
            player.stopWalking();
        }

        // 1) collision horizontale
        if (dx != 0) {
            // Déplace en X et conserve walking = true
            player.move(dx, 0);

            // Si intersection, colle le joueur contre le mur
            for (Platform p : platforms) {
                if (player.intersects(p)) {
                    if (dx > 0) {
                        // collision à droite
                        player.setX(p.getX() - player.getWidth());
                    } else {
                        // collision à gauche
                        player.setX(p.getX() + p.getWidth());
                    }
                }
            }
        }

        // 2) gravité / saut / jetpack
        if (jumping && player.canJump() && !jetpack) {
            player.setVelocityY(-603.0);
            player.setOnGround(false);
            player.incrementJumps();
            jumping = false;
        }
        if (jetpack && player.isJetpackActive()) {
            // ascension continue tant que le jetpack est actif
            player.setVelocityY(-300.0);
        } else {
            // sinon on applique la gravité
            player.setVelocityY(player.velocityY + GRAVITY * deltaSec);
        }

        // 3) collision verticale
        double oldY = player.getY();
        // on modifie Y directement pour ne pas écraser walking
        player.setY(oldY + player.velocityY * deltaSec);

        // appel de votre handler d'atterrissage + fragilePlatform
        handlePlatformCollisions(oldY);

        // chute sous le niveau → perte de vie
        if (player.getY() > level.getLevelHeight()) {
            loseLife();
        }
    }



    /** Handles collisions with platforms (including fragile). */
    private void handlePlatformCollisions(double oldY) {
        for (Platform p : platforms) {
            if (p instanceof src.model.game.platforms.FragilePlatform) {
                ((src.model.game.platforms.FragilePlatform)p).resetStep(player);
            }
        }
        for (Platform p : platforms) {
            double top    = p.getY();
            double botNow = player.getY() + player.getHeight();
            double botOld = oldY + player.getHeight();
            if (player.intersects(p) && player.velocityY > 0 && botOld <= top) {
                player.setY(top - player.getHeight());
                player.velocityY = 0;
                player.setOnGround(true);
                player.resetJumps();
                if (p instanceof src.model.game.platforms.FragilePlatform) {
                    var fp = (src.model.game.platforms.FragilePlatform)p;
                    if (!fp.isBroken()) fp.step(player);
                }
            }
        }
        platforms.removeIf(p ->
            p instanceof src.model.game.platforms.FragilePlatform
            && ((src.model.game.platforms.FragilePlatform)p).isBroken()
        );
    }

    /** Updates projectiles: movement and enemy collisions. */
    private void updateProjectiles(double deltaSec) {
        var pit = projectiles.iterator();
        while (pit.hasNext()) {
            Projectile pr = pit.next();
            pr.update(deltaSec);
            if (pr.isOutOfBounds(level.getLevelWidth())) {
                pit.remove();
                continue;
            }
            var eit = enemies.iterator();
            while (eit.hasNext()) {
                Enemy e = eit.next();
                if (pr.intersects(e)) {
                    eit.remove();
                    pit.remove();
                    break;
                }
            }
        }
    }

    /** Fires a projectile in spaceship mode. */
    private void fireProjectile() {
        double offsetX = player.isFacingRight() ? player.getWidth() : -10;
        double px      = player.getX() + offsetX;
        double py      = player.getY() + player.getHeight()/2.0;
        projectiles.add(new Projectile(px, py, player.isFacingRight()));
    }

    /** Updates the camera position for platform/boss/ship modes. */
    private void updateCamera(boolean isShip) {
        double cw     = view.getCanvasWidth();
        double ch     = view.getCanvasHeight();
        double levelW = level.getLevelWidth();
        double levelH = level.getLevelHeight();

        // X-axis
        if (inBossFight) {
            double centerX = (level.getBossZoneStart() + level.getBossZoneEnd())/2.0;
            cameraX = centerX - cw/2.0;
        } else if (!isShip) {
            double targetX = player.getX() - cw/2.0;
            cameraX += 0.1 * (targetX - cameraX);
        }
        cameraX = Math.max(0, Math.min(cameraX, levelW - cw));

        // Y-axis
        if (isShip) {
            cameraY = 0;
        } else if (inBossFight) {
            Boss boss = enemies.stream()
                               .filter(e -> e instanceof Boss)
                               .map(e -> (Boss)e)
                               .findFirst()
                               .orElse(null);
            if (boss != null) {
                double centerY = boss.getY() + boss.getHeight()/2.0;
                cameraY = centerY - ch/2.0;
            } else {
                cameraY = (levelH - ch)/2.0;
            }
        } else {
            double targetY = player.getY() - ch/2.0;
            cameraY += 0.1 * (targetY - cameraY);
        }
        if (levelH <= ch) {
            cameraY = levelH - ch;
        }
        double minY = Math.min(0, levelH - ch);
        double maxY = Math.max(0, levelH - ch);
        cameraY = Math.max(minY, Math.min(cameraY, maxY));

        view.cameraXProperty().set(cameraX);
        view.cameraYProperty().set(cameraY);
    }

/** Renders all game elements via GameView. */
private void render(boolean isShip) {
    // Décorations
    List<Image>    decoImgs   = new ArrayList<>();
    List<Double[]> posDeco    = new ArrayList<>();
    List<String>   decoNames  = new ArrayList<>();
    for (Decoration d : decorations) {
        decoImgs  .add(d.getTexture());
        posDeco   .add(new Double[]{ d.getX(), d.getY(), d.getWidth(), d.getHeight() });
        decoNames .add(d.getName());
    }

    // Plateformes
    List<Image>    platImgs = new ArrayList<>();
    List<Double[]> posPl    = new ArrayList<>();
    for (Platform p : platforms) {
        platImgs.add(p.getTexture());
        posPl   .add(new Double[]{ p.getX(), p.getY(), p.getWidth(), p.getHeight() });
    }

    // Projectiles
    List<Double[]> posProj = new ArrayList<>();
    for (Projectile pr : projectiles) {
        posProj.add(new Double[]{ pr.getX(), pr.getY(), pr.getWidth(), pr.getHeight() });
    }

    boolean isJumping = !player.onGround;

    view.draw(
        // 1) Contexte de jeu
        level.getBackgroundImage(),
        player.getX(), player.getY(),
        player.getWidth(), player.getHeight(),
        player.isWalking(), player.isFacingRight(),
        isJumping,
        spaceshipMode,
        // 2) Décorations
        decoImgs, posDeco, decoNames,
        // 3) Plateformes
        platImgs, posPl,
        // 4) Ennemis
        enemies,
        // 5) Projectiles
        posProj
    );
}


    /** Respawns the player at the start position. */
    private void resetPlayerPosition() {
        player.setX(initialPlayerX);
        player.setY(initialPlayerY);
        player.velocityY = 0;
        player.setOnGround(true);
        player.resetJumps();
        player.setJetpackActive(false);
        if (jetpackTimer != null) {
            jetpackTimer.cancel();
            jetpackTimer = null;
        }
    }

    /** Resets only the player's state flags. */
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

    /** Activates the jetpack after 500ms of holding SPACE. */
    private class JetpackTask extends TimerTask {
        @Override
        public void run() {
            jetpack = true;
            player.setJetpackActive(false);
        }
    }
}
