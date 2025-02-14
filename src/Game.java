package src;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import src.model.Player;
import src.model.Platform;
import src.model.Enemy;
import src.view.GameView;
import src.controller.GameController;

import java.util.ArrayList;
import java.util.List;

public class Game extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Player player = new Player(100, 500);
        List<Platform> platforms = new ArrayList<>();
        platforms.add(new Platform(200, 400, 200, 20));
        platforms.add(new Platform(600, 350, 200, 20));
        platforms.add(new Platform(1000, 400, 200, 20));
        platforms.add(new Platform(0, HEIGHT - 20, WIDTH * 2, 20)); // Sol étendu

        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy(650, 330, 50, 50, 2, 600, 800));
        enemies.add(new Enemy(1200, 330, 50, 50, 2, 1100, 1300));

        GameView view = new GameView(gc);
        GameController controller = new GameController(player, platforms, enemies, view);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        controller.handleInput(scene);

        primaryStage.setTitle("2D Platformer Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        controller.startGameLoop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}