package src.controller;

import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.Scene;
import src.model.Player;
import src.model.Platform;
import src.model.Enemy;
import src.view.GameView;
import src.Game;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Iterator;

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

    public GameController(Player player, List<Platform> platforms, List<Enemy> enemies, GameView view, Game game) {
        this.player = player;
        this.platforms = platforms;
        this.enemies = enemies;
        this.view = view;
        this.game = game;
        this.initialPlayerX = player.getX();
        this.initialPlayerY = player.getY();
        view.cameraXProperty().bind(player.xProperty().subtract(400));
        view.cameraYProperty().bind(player.yProperty().subtract(300));
    }

    public void handleInput(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) left = true;
            if (event.getCode() == KeyCode.RIGHT) right = true;
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
                    }, 500); //Activer le jetpack après 0,5 seconde
                }
            }
        });
        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT) left = false;
            if (event.getCode() == KeyCode.RIGHT) right = false;
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
                view.draw(player, platforms, enemies);
            }
        }.start();
    }

    private void update() {
        if (left) player.move(-player.getSpeed(), 0);
        if (right) player.move(player.getSpeed(), 0);
        if (jumping && player.canJump() && !jetpack) {
            player.velocityY = -10;
            player.incrementJumps();
            jumping = false;
        }

        if (jetpack && player.isJetpackActive()) {
            player.velocityY = -5; //Vitesse de montée avec le jetpack
        } else {
            player.velocityY += GRAVITY;
        }

        player.move(0, player.velocityY);

        for (Platform platform : platforms) {
            if (player.intersects(platform) && player.velocityY > 0) {
                player.setY(platform.getY() - player.getHeight());
                player.velocityY = 0;
                player.onGround = true;
                player.resetJumps();
            }
        }

        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            if (player.landsOn(enemy)) {
                enemyIterator.remove();
                player.velocityY = -10; //Rebondir après avoir atterri sur l'ennemi
            } else if (player.intersects(enemy)) {
                resetPlayerPosition();
            }
            enemy.update();
        }

        if (player.getY() > GameView.HEIGHT) {
            resetPlayerPosition();
        }

        if (player.getX() > 1600) {//Pour l'instant c'est la fin du niveau
            game.nextLevel();
        }
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