package src.controller;

import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import src.model.Player;
import src.model.Platform;
import src.model.Enemy;
import src.model.platforms.FragilePlatform;
import src.view.GameView;
import src.Game;
import src.levels.Level;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Iterator;
import java.util.ArrayList;

public class GameController {
    private static final double GRAVITY = 0.5;
    private boolean left, right, jumping, jetpack;
    private Player player;
    private List<Platform> platforms;
    private List<Enemy> enemies;
    private GameView view;
    private double initialPlayerX;
    private double initialPlayerY;
    private Timer jetpackTimer;
    private Game game;
    private Level level;

    public GameController(Player player, List<Platform> platforms, List<Enemy> enemies, GameView view, Game game, Level level) {
        this.player = player;
        this.platforms = platforms;
        this.enemies = enemies;
        this.view = view;
        this.game = game;
        this.level = level;
        this.initialPlayerX = player.getX();
        this.initialPlayerY = player.getY();

        // Centrer la caméra sur le joueur
        view.cameraXProperty().bind(player.xProperty().subtract(400));
        view.cameraYProperty().bind(player.yProperty().subtract(300));
    }

    public void handleInput(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                left = true;
            }
            if (event.getCode() == KeyCode.RIGHT) {
                right = true;
            }
            if (event.getCode() == KeyCode.SPACE) {
                jumping = true;
                if (jetpackTimer == null) {
                    jetpackTimer = new Timer();
                    jetpackTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            jetpack = true;
                            player.setJetpackActive(true);
                        }
                    }, 500); // Activer le jetpack après 0,5 seconde
                }
            }
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                left = false;
            }
            if (event.getCode() == KeyCode.RIGHT) {
                right = false;
            }
            if (event.getCode() == KeyCode.SPACE) {
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

    public void startGameLoop() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                draw();
            }
        }.start();
    }

    private void update() {
        // -------------------------
        // 1) Calculer le déplacement horizontal
        // -------------------------
        double dx = 0;
        if (left)  dx -= player.getSpeed();
        if (right) dx += player.getSpeed();

        // -------------------------
        // 2) Gérer le saut + jetpack
        // -------------------------
        if (jumping && player.canJump() && !jetpack) {
            player.velocityY = -10;
            player.incrementJumps();
            jumping = false; 
        }

        if (jetpack && player.isJetpackActive()) {
            player.velocityY = -5; // Vitesse de montée avec le jetpack
        } else {
            player.velocityY += GRAVITY; // Gravité
        }

        // -------------------------
        // 3) Calculer le déplacement vertical
        // -------------------------
        double dy = player.velocityY;

        // -------------------------
        // 4) Appeler move() UNE SEULE FOIS
        // -------------------------
        player.move(dx, dy);

        // -------------------------
        // 5) Gérer collisions plateformes
        // -------------------------
        Iterator<Platform> platformIterator = platforms.iterator();
        while (platformIterator.hasNext()) {
            Platform platform = platformIterator.next();

            // Collision "par le dessus" (chute sur la plateforme)
            if (player.intersects(platform) && player.velocityY > 0) {
                player.setY(platform.getY() - player.getHeight());
                player.velocityY = 0;
                player.onGround = true;
                player.resetJumps();

                // Gérer les plateformes fragiles
                if (platform instanceof FragilePlatform) {
                    FragilePlatform fragilePlatform = (FragilePlatform) platform;
                    if (!fragilePlatform.isBroken() && player.velocityY == 0) {
                        fragilePlatform.step(player);
                    }
                    if (fragilePlatform.isBroken()) {
                        platformIterator.remove();
                    }
                }
            }
        }

        // -------------------------
        // 6) Gérer collisions ennemis
        // -------------------------
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();

            // Sauter sur l'ennemi => le détruire
            if (player.landsOn(enemy)) {
                enemyIterator.remove();
                player.velocityY = -10; // Rebond
            }
            // Collision latérale => reset
            else if (player.intersects(enemy)) {
                resetPlayerPosition();
            }
            enemy.update();
        }

        // -------------------------
        // 7) Vérifier si le joueur tombe hors de l'écran
        // -------------------------
        if (player.getY() > GameView.HEIGHT) {
            resetPlayerPosition();
        }

        // -------------------------
        // 8) Fin de niveau (exemple)
        // -------------------------
        if (player.getX() > 1600) {
            game.nextLevel();
        }
    }

    private void draw() {
        // Préparer les listes pour la vue
        List<Image> platformImages = new ArrayList<>();
        List<Double[]> platformPositions = new ArrayList<>();
        for (Platform platform : platforms) {
            platformImages.add(platform.getTexture());
            platformPositions.add(new Double[]{
                platform.getX(),
                platform.getY(),
                platform.getWidth(),
                platform.getHeight()
            });
        }

        List<Double[]> enemyPositions = new ArrayList<>();
        for (Enemy enemy : enemies) {
            enemyPositions.add(new Double[]{
                enemy.getX(),
                enemy.getY(),
                enemy.getWidth(),
                enemy.getHeight()
            });
        }

        // Appeler la méthode draw() de la vue
        view.draw(
            level.getBackgroundImage(),
            player.getX(), 
            player.getY(),
            player.getWidth(), 
            player.getHeight(),
            player.isWalking(),  // <-- c'est ici qu'on passe l'info d'animation
            platformImages, 
            platformPositions, 
            enemyPositions
        );
    }

    private void resetPlayerPosition() {
        player.setX(initialPlayerX);
        player.setY(initialPlayerY);
        player.velocityY = 0;
        player.onGround = true;
        player.resetJumps();
        player.setJetpackActive(false);
        if (jetpackTimer != null) {
            jetpackTimer.cancel();
            jetpackTimer = null;
        }
    }

    public void resetPlayerState() {
        left = false;
        right = false;
        jumping = false;
        jetpack = false;
        player.setJetpackActive(false);
        player.resetJumps();
        player.setVelocityY(0);
        player.setOnGround(true);
        if (jetpackTimer != null) {
            jetpackTimer.cancel();
            jetpackTimer = null;
        }
    }
}
