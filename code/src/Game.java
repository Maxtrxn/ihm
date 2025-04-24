package src;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.List;
import java.util.Arrays;
import java.util.function.Function;

import src.controller.GameController;
import src.levels.Level;
import src.levels.Level1;
import src.levels.SpaceshipLevel;
import src.levels.Level2;
import src.levels.Level3;
import src.model.Player;
import src.view.GameView;

public class Game extends Application {
    // Taille de base (800×600), utilisée comme taille initiale.
    private static final int WIDTH  = 800;
    private static final int HEIGHT = 600;

    private Stage primaryStage;
    private Player player;
    private GameController controller;

    // Liste de "fabriques" de niveaux, dans l'ordre de jeu
    private List<Function<Player, Level>> levelSuppliers;
    private int currentLevelIndex = 0;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.player = new Player(100, 500);

        // Initialise la liste de niveaux
        levelSuppliers = Arrays.asList(
            Level1::new,
            SpaceshipLevel::new,
            Level2::new,
            Level3::new
        );

        primaryStage.setTitle("Steampunk Adventure");
        // On maximise la fenêtre sans passer en plein écran
        primaryStage.setMaximized(true);

        // Charge le premier niveau
        loadCurrentLevel();

        primaryStage.show();
    }

    /** Charge l’instance du niveau courant. */
    private void loadCurrentLevel() {
        Level lvl = levelSuppliers.get(currentLevelIndex).apply(player);
        loadLevel(lvl);
    }

    /**
     * Charge un niveau et met en place la scène avec un Canvas redimensionnable.
     * Arrête la boucle de jeu précédente si elle existe.
     */
    public void loadLevel(Level level) {
        if (controller != null) {
            controller.stopGameLoop();
        }

        Pane root = new Pane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());
        root.getChildren().add(canvas);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        GameView view = new GameView(gc);
        // ———> on vide le cache du background pour que l’ancien fond n’apparaisse plus
        view.resetBackgroundCache();

        controller = new GameController(
            player,
            level.getPlatforms(),
            level.getEnemies(),
            level.getDecorations(),
            view,
            this,
            level
        );

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        controller.handleInput(scene);

        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        controller.startGameLoop();
    }

    /**
     * Passe au niveau suivant dans la liste levelSuppliers.
     */
    public void nextLevel() {
        // Réinitialise l'état et la position du joueur.
        controller.resetPlayerState();
        player.setX(100);
        player.setY(500);

        if (currentLevelIndex < levelSuppliers.size() - 1) {
            currentLevelIndex++;
            loadCurrentLevel();
        } else {
            System.out.println("Vous avez terminé le jeu !");
            controller.stopGameLoop();
            // TODO : écran de victoire
        }
    }

    /**
     * Charge explicitement le niveau vaisseau (au cas où vous l’appeliez directement).
     */
    public void loadSpaceshipLevel() {
        controller.resetPlayerState();
        player.setX(100);
        player.setY(500);
        loadLevel(new SpaceshipLevel(player));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
