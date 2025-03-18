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

    // Caméra
    private double cameraX = 0;
    private double cameraY = 0;
    // Ajuste si tu veux la vue plus ou moins haute
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
                    }, 500);
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
        double dx = 0;
        if (left)  dx -= player.getSpeed();
        if (right) dx += player.getSpeed();

        if (jumping && player.canJump() && !jetpack) {
            player.velocityY = -10;
            player.incrementJumps();
            jumping = false;
        }

        if (jetpack && player.isJetpackActive()) {
            player.velocityY = -5;
        } else {
            player.velocityY += GRAVITY;
        }

        double dy = player.velocityY;
        player.move(dx, dy);

        for (Platform p : platforms) {
            if (p instanceof FragilePlatform) {
                ((FragilePlatform) p).resetStep(player);
            }
        }

        Iterator<Platform> platformIterator = platforms.iterator();
        while (platformIterator.hasNext()) {
            Platform platform = platformIterator.next();
            if (player.intersects(platform) && player.velocityY > 0) {
                player.setY(platform.getY() - player.getHeight());
                player.velocityY = 0;
                player.onGround = true;
                player.resetJumps();

                if (platform instanceof FragilePlatform) {
                    FragilePlatform fragile = (FragilePlatform) platform;
                    if (!fragile.isBroken()) {
                        fragile.step(player);
                    }
                    if (fragile.isBroken()) {
                        platformIterator.remove();
                    }
                }
            }
        }

        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            if (player.landsOn(enemy)) {
                enemyIterator.remove();
                player.velocityY = -10;
            } else if (player.intersects(enemy)) {
                resetPlayerPosition();
            }
            enemy.update();
        }

        if (player.getY() > GameView.HEIGHT) {
            resetPlayerPosition();
        }

        if (player.getX() > 1600) {
            game.nextLevel();
        }

        updateCamera();
    }

    private void draw() {
        List<Image> platformImages = new ArrayList<>();
        List<Double[]> platformPositions = new ArrayList<>();
        for (Platform p : platforms) {
            platformImages.add(p.getTexture());
            platformPositions.add(new Double[]{
                p.getX(),
                p.getY(),
                p.getWidth(),
                p.getHeight()
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
        double desiredCameraX = player.getX() - (GameView.WIDTH * 0.4);
        cameraX += 0.1 * (desiredCameraX - cameraX);
        if (cameraX < 0) cameraX = 0;

        double levelWidth = 3000;
        double maxCameraX = levelWidth - GameView.WIDTH;
        if (cameraX > maxCameraX) {
            cameraX = maxCameraX;
        }

        double desiredCameraY = player.getY() - (GameView.HEIGHT / 2.0) + cameraYOffset;
        cameraY += 0.1 * (desiredCameraY - cameraY);
        if (cameraY < 0) cameraY = 0;

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
