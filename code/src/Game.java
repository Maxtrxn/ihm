package src;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import src.model.Player;
import src.view.GameView;
import src.controller.GameController;
import src.levels.Level;
import src.levels.Level1;
import src.levels.Level2;

public class Game extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private Stage primaryStage;
    private Player player;
    private GameController controller;
    private GameView view;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.player = new Player(100, 500);
        this.view = new GameView(new Canvas(WIDTH, HEIGHT).getGraphicsContext2D());

        loadLevel(new Level1(player));

        primaryStage.setTitle("Steampunk Adventure");
        primaryStage.show();
    }

    public void loadLevel(Level level) {
        Pane root = new Pane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        view = new GameView(gc);

        controller = new GameController(player, level.getPlatforms(), level.getEnemies(), view, this, level);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        controller.handleInput(scene);

        primaryStage.setScene(scene);

        controller.startGameLoop();
    }

    public void nextLevel() {
        controller.resetPlayerState();
        player.setX(100);
        player.setY(500);
        loadLevel(new Level2(player));
    }

    public static void main(String[] args) {
        launch(args);
    }
}