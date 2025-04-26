// src/Game.java
package src;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import src.controller.game.GameController;
import src.model.game.Level;
import src.model.game.Player;
import src.view.game.GameView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Game extends Application {
    private static final int WIDTH  = 800;
    private static final int HEIGHT = 600;

    private Stage primaryStage;
    private Scene scene;
    private Pane root;
    private Canvas canvas;
    private Player player;
    private GameController controller;

    private List<Function<Player, Level>> levelSuppliers;
    private int currentLevelIndex = 0;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.player = new Player(100, 500);

        root = new Pane();
        canvas = new Canvas(WIDTH, HEIGHT);
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());
        root.getChildren().add(canvas);
        scene = new Scene(root, WIDTH, HEIGHT);

        primaryStage.setTitle("Steampunk Adventure");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();

        // Liste des noms de niveaux JSON
        List<String> levelNames = List.of("level1", "level2", "level3", "level4", "level5");
        levelSuppliers = new ArrayList<>();

        // Instancie Level(player, name) pour chaque JSON
        for (String name : levelNames) {
            levelSuppliers.add(p -> new Level(p, name));
        }
        // Insère le niveau vaisseau (spaceship.json) après le premier
        levelSuppliers.add(1, p -> new Level(p, "spaceship"));

        loadCurrentLevel();
    }

    private void loadCurrentLevel() {
        Level lvl = levelSuppliers.get(currentLevelIndex).apply(player);

        if (controller != null) {
            controller.stopGameLoop();
        }

        var gc = canvas.getGraphicsContext2D();
        GameView view = new GameView(gc);

        controller = new GameController(
            player,
            lvl.getPlatforms(),
            lvl.getEnemies(),
            lvl.getDecorations(),
            view,
            this,
            lvl
        );

        controller.handleInput(scene);
        controller.startGameLoop();
    }

    public void nextLevel() {
        controller.resetPlayerState();
        player.setX(100);
        player.setY(500);

        if (currentLevelIndex < levelSuppliers.size() - 1) {
            currentLevelIndex++;
            loadCurrentLevel();
        } else {
            System.out.println("🎉 Vous avez terminé le jeu !");
            controller.stopGameLoop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
