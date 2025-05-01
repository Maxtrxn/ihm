// src/Game.java
package src.model.game;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import src.common.ResourceManager;
import src.controller.game.GameController;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GameModel{
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


    public GameModel(GameController controller){
        this.controller = controller;
        this.player = new Player(100, 500);


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

    public GameModel(GameController controller, String levelName){
        this.controller = controller;
        this.player = new Player(100, 500);


        // Liste des noms de niveaux JSON
        List<String> levelNames = List.of(levelName);
        levelSuppliers = new ArrayList<>();

        // Instancie Level(player, name) pour chaque JSON
        for (String name : levelNames) {
            levelSuppliers.add(p -> new Level(p, name));
        }
        loadCurrentLevel();
    }


    private void loadCurrentLevel() {
        Level lvl = levelSuppliers.get(currentLevelIndex).apply(player);

        if (currentLevelIndex > 0) {
            controller.stopGameLoop();
        }

        controller.setPlayer(player);
        controller.setLevel(lvl);


        controller.handleInput();
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
            System.out.println(ResourceManager.getString("GameModel_win_message"));
            controller.stopGameLoop();
        }
    }
}
