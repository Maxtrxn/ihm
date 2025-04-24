package src;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import src.controller.GameController;
import src.levels.Level;
import src.levels.Level1;
import src.model.Player;
import src.view.GameView;

public class Game extends Application {
    // Taille de base (800×600), utilisée comme taille initiale.
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private Stage primaryStage;
    private Player player;
    private GameController controller;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.player = new Player(100, 500);

        // Charge directement le niveau 1
        loadLevel(new Level1(player));

        primaryStage.setTitle("Steampunk Adventure");
        primaryStage.show();
    }

    /**
     * Charge un niveau et met en place la scène avec un Canvas redimensionnable.
     * Arrête la boucle de jeu précédente si elle existe.
     */
    public void loadLevel(Level level) {
        // Arrête l'ancienne boucle si nécessaire
        if (controller != null) {
            controller.stopGameLoop();
        }

        Pane root = new Pane();

        // Création du Canvas (initialement 800×600).
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        // Lie la largeur/hauteur du Canvas à celles du Pane.
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());
        root.getChildren().add(canvas);

        // Récupération du GraphicsContext pour dessiner.
        GraphicsContext gc = canvas.getGraphicsContext2D();
        GameView view = new GameView(gc);

        // Création du contrôleur de jeu (on lui passe aussi les décorations).
        controller = new GameController(
            player,
            level.getPlatforms(),
            level.getEnemies(),
            level.getDecorations(),
            view,
            this,
            level
        );

        // Création de la scène, avec taille initiale 800×600.
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        controller.handleInput(scene);

        // Affectation de la scène au Stage et autorise le redimensionnement.
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);

        // Démarrage de la boucle de jeu.
        controller.startGameLoop();
    }

    /**
     * Passe au niveau suivant (niveau 2).
     */
    public void nextLevel() {
        // Réinitialise l'état et la position du joueur.
        controller.resetPlayerState();
        player.setX(100);
        player.setY(500);

        // Niveau 2 classique
        loadLevel(new src.levels.Level2(player));
    }

    /**
     * Charge le niveau vaisseau, accessible depuis Level1.
     */
    public void loadSpaceshipLevel() {
        // Réinitialise l'état et la position du joueur.
        controller.resetPlayerState();
        player.setX(100);
        player.setY(500);

        loadLevel(new src.levels.SpaceshipLevel(player));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
