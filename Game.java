package ihm;

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
    
    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        player = new Player(100, 500);
        platforms = new ArrayList<>();
        platforms.add(new Platform(200, 400, 200, 20));
        platforms.add(new Platform(0, HEIGHT - 20, WIDTH, 20)); // Sol
        
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
        
        primaryStage.setTitle("Simple JavaFX Platformer");
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
    }
    
    private void draw(GraphicsContext gc) {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        gc.setFill(Color.RED);
        gc.fillRect(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        
        gc.setFill(Color.BLUE);
        for (Platform platform : platforms) {
            gc.fillRect(platform.getX(), platform.getY(), platform.getWidth(), platform.getHeight());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

class Player {
    private double x, y, width = 30, height = 30;
    public double velocityY = 0;
    public boolean onGround = false;
    
    public Player(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void move(double dx, double dy) {
        x += dx;
        y += dy;
    }
    
    public boolean intersects(Platform platform) {
        return x < platform.getX() + platform.getWidth() &&
               x + width > platform.getX() &&
               y + height > platform.getY() &&
               y + height < platform.getY() + platform.getHeight();
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}

class Platform {
    private double x, y, width, height;
    
    public Platform(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}
