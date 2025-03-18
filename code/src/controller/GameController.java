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

    // -----------------------------------------
    // Position de la caméra
    // -----------------------------------------
    private double cameraX = 0;
    private double cameraY = 0;

    // -----------------------------------------
    // Offset vertical pour “monter” la caméra 
    // et voir plus de sol
    // (valeur positive => la caméra se déplace vers le haut)
    // -----------------------------------------
    private final double cameraYOffset = -227; 

    public GameController(Player player, List<Platform> platforms, List<Enemy> enemies,
                          GameView view, Game game, Level level) {
        this.player = player;
        this.platforms = platforms;
        this.enemies = enemies;
        this.view = view;
        this.game = game;
        this.level = level;
        this.initialPlayerX = player.getX();
        this.initialPlayerY = player.getY();
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
        // 1) Calculer dx (gauche/droite)
        double dx = 0;
        if (left) {
            dx -= player.getSpeed();
        }
        if (right) {
            dx += player.getSpeed();
        }

        // 2) Gérer le saut (et jetpack)
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

        // 3) Calculer dy
        double dy = player.velocityY;

        // 4) Déplacer le joueur (une seule fois)
        player.move(dx, dy);

        // 5) Réinitialiser "playerWasOn" (pour les plateformes fragiles)
        for (Platform p : platforms) {
            if (p instanceof FragilePlatform) {
                ((FragilePlatform) p).resetStep(player);
            }
        }

        // 6) Gérer collisions plateformes
        Iterator<Platform> platformIterator = platforms.iterator();
        while (platformIterator.hasNext()) {
            Platform platform = platformIterator.next();

            if (player.intersects(platform) && player.velocityY > 0) {
                // On cale le joueur sur la plateforme
                player.setY(platform.getY() - player.getHeight());
                player.velocityY = 0;
                player.onGround = true;
                player.resetJumps();

                // Si c'est une plateforme fragile
                if (platform instanceof FragilePlatform) {
                    FragilePlatform fragilePlatform = (FragilePlatform) platform;
                    if (!fragilePlatform.isBroken()) {
                        fragilePlatform.step(player);
                    }
                    if (fragilePlatform.isBroken()) {
                        platformIterator.remove();
                    }
                }
            }
        }

        // 7) Gérer collisions ennemis
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            if (player.landsOn(enemy)) {
                // Détruire l'ennemi
                enemyIterator.remove();
                // Rebond
                player.velocityY = -10;
            } else if (player.intersects(enemy)) {
                // Collision latérale => reset
                resetPlayerPosition();
            }
            enemy.update();
        }

        // 8) Si le joueur tombe hors de l'écran
        if (player.getY() > GameView.HEIGHT) {
            resetPlayerPosition();
        }

        // 9) Fin de niveau (exemple)
        if (player.getX() > 1600) {
            game.nextLevel();
        }

        // 10) Mettre à jour la caméra
        updateCamera();
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
            player.isWalking(),
            platformImages,
            platformPositions,
            enemyPositions
        );
    }

    private void updateCamera() {
        // Calcul de la caméraX (horizontal)
        double desiredCameraX = player.getX() - (GameView.WIDTH * 0.4);
        cameraX += 0.1 * (desiredCameraX - cameraX);
        if (cameraX < 0) cameraX = 0;
        // (Adapte la borne max si besoin, selon la largeur du niveau)
        double levelWidth = 3000; 
        double maxCameraX = levelWidth - GameView.WIDTH;
        if (cameraX > maxCameraX) {
            cameraX = maxCameraX;
        }

        // Calcul de la caméraY (vertical) avec offset
        double desiredCameraY = player.getY() - (GameView.HEIGHT / 2.0) + cameraYOffset;
        cameraY += 0.1 * (desiredCameraY - cameraY);

        // Empêcher la caméra de trop monter/descendre si besoin
        if (cameraY < 0) cameraY = 0;
        // (Si ton niveau a une hauteur plus grande que 600, tu peux borner en bas aussi.)

        // On met à jour la vue
        view.cameraXProperty().set(cameraX);
        view.cameraYProperty().set(cameraY);
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
