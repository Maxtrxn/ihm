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
import src.levels.SpaceshipLevel;

public class Game extends Application {
    // Taille de base (800×600), utilisée comme taille initiale.
    // Le Canvas pourra s'adapter ensuite.
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

        // Charge directement le niveau 1
        loadLevel(new Level1(player));

        primaryStage.setTitle("Steampunk Adventure");
        // Si tu veux forcer le plein écran dès le début, décommente la ligne suivante :
        // primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    /**
     * Charge un niveau et met en place la scène avec un Canvas redimensionnable.
     */
    public void loadLevel(Level level) {
        Pane root = new Pane();

        // Création du Canvas (initialement 800×600).
        Canvas canvas = new Canvas(WIDTH, HEIGHT);

        // Lie la largeur/hauteur du Canvas à celles du Pane,
        // ce qui permet de s'adapter quand la fenêtre change de taille.
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        root.getChildren().add(canvas);

        // Récupération du GraphicsContext pour dessiner.
        GraphicsContext gc = canvas.getGraphicsContext2D();
        view = new GameView(gc);

        // Création du contrôleur de jeu (boucle, gestion des collisions, etc.).
        controller = new GameController(player, level.getPlatforms(), level.getEnemies(), view, this, level);

        // Création de la scène, avec taille initiale 800×600.
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // Gestion des entrées clavier (déplacement, saut, etc.).
        controller.handleInput(scene);

        // Affectation de la scène au Stage.
        primaryStage.setScene(scene);

        // Autorise le redimensionnement (le Canvas suivra la taille de la fenêtre).
        primaryStage.setResizable(true);

        // Démarrage de la boucle de jeu.
        controller.startGameLoop();
    }

    /**
     * Appelé pour passer au niveau suivant.
     */
    public void nextLevel() {
        // Réinitialise l'état du joueur (ex. : jetpack, vitesse, etc.).
        controller.resetPlayerState();
        player.setX(100);
        player.setY(500);

        // Charge le niveau 2 (à adapter si tu as plus de niveaux).
        loadLevel(new Level2(player));
    }
    
    /**
     * Charge le niveau vaisseau, accessible depuis Level1.
     */
    public void loadSpaceshipLevel() {
        // Réinitialise l'état du joueur si besoin
        controller.resetPlayerState();
        player.setX(100);
        player.setY(500);
        
        loadLevel(new SpaceshipLevel(player));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
