package ihm.src;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class Game extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final double GRAVITY = 0.5;
    private boolean left, right, jumping;
    private Player player;
    private List<Platform> platforms;
    private Enemy enemy;
    private double cameraX = 0;
    
    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        player = new Player(100, 500);
        platforms = new ArrayList<>();
        platforms.add(new Platform(200, 400, 200, 20));
        platforms.add(new Platform(600, 350, 200, 20));
        platforms.add(new Platform(1000, 400, 200, 20));
        platforms.add(new Platform(0, HEIGHT - 20, WIDTH * 2, 20)); // Sol étendu
        
        enemy = new Enemy(650, 330, 50, 50, 2, 600, 800);
        
        Scene scene = new Scene(root, WIDTH, HEIGHT);
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
        
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                draw(gc);
            }
        }.start();
        
        primaryStage.setTitle("Scrolling JavaFX Platformer");
        primaryStage.setScene(scene);
        primaryStage.show();
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
        
        enemy.update();
        
        cameraX = player.getX() - WIDTH / 2;
    }
    
    private void draw(GraphicsContext gc) {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        gc.setFill(Color.RED);
        gc.fillRect(player.getX() - cameraX, player.getY(), player.getWidth(), player.getHeight());
        
        gc.setFill(Color.BLUE);
        for (Platform platform : platforms) {
            gc.fillRect(platform.getX() - cameraX, platform.getY(), platform.getWidth(), platform.getHeight());
        }
        
        gc.setFill(Color.GREEN);
        gc.fillRect(enemy.getX() - cameraX, enemy.getY(), enemy.getWidth(), enemy.getHeight());
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}