package src.controller;

import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.Scene;
import src.model.Player;
import src.model.Platform;
import src.model.Enemy;
import src.view.GameView;

import java.util.List;

public class GameController {
    private static final double GRAVITY = 0.5;
    private boolean left, right, jumping;
    private Player player;
    private List<Platform> platforms;
    private List<Enemy> enemies;
    private GameView view;

    public GameController(Player player, List<Platform> platforms, List<Enemy> enemies, GameView view) {
        this.player = player;
        this.platforms = platforms;
        this.enemies = enemies;
        this.view = view;
        view.cameraXProperty().bind(player.xProperty().subtract(400));
    }

    public void handleInput(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) left = true;
            if (event.getCode() == KeyCode.RIGHT) right = true;
            if (event.getCode() == KeyCode.SPACE) jumping = true;
        });
        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT) left = false;
            if (event.getCode() == KeyCode.RIGHT) right = false;
            if (event.getCode() == KeyCode.SPACE) jumping = false;
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
        if (left) player.move(-3, 0);
        if (right) player.move(3, 0);
        if (jumping && player.onGround) {
            player.velocityY = -10;
            player.onGround = false;
        }

        player.velocityY += GRAVITY;
        player.move(0, player.velocityY);

        for (Platform platform : platforms) {
            if (player.intersects(platform) && player.velocityY > 0) {
                player.setY(platform.getY() - player.getHeight());
                player.velocityY = 0;
                player.onGround = true;
            }
        }

        for (Enemy enemy : enemies) {
            enemy.update();
        }
    }
}