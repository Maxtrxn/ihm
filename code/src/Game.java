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
import src.levels.Level4;
import src.model.Player;
import src.view.GameView;

public class Game extends Application {
    // Taille "logique" de la vue (sera redimensionnée à la fenêtre)
    private static final int WIDTH  = 800;
    private static final int HEIGHT = 600;

    private Stage primaryStage;
    private Scene  scene;
    private Pane   root;
    private Canvas canvas;
    private Player player;
    private GameController controller;

    // Liste des niveaux dans l'ordre
    private List<Function<Player, Level>> levelSuppliers;
    private int currentLevelIndex = 0;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.player       = new Player(100, 500);

        // Crée une seule Scene + Canvas qu'on réutilisera
        this.root   = new Pane();
        this.canvas = new Canvas(WIDTH, HEIGHT);
        // le Canvas s'adapte toujours à la taille du Pane
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());
        root.getChildren().add(canvas);
        this.scene = new Scene(root, WIDTH, HEIGHT);

        primaryStage.setTitle("Steampunk Adventure");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();

        // Prépare la liste des niveaux
        levelSuppliers = Arrays.asList(
            Level1::new,
            SpaceshipLevel::new,
            Level2::new,
            Level3::new,
            Level4::new  // Ajout du niveau 4
        );

        // Charge et lance le premier niveau
        loadCurrentLevel();
    }

    /** Instancie et affiche le niveau à index currentLevelIndex */
    private void loadCurrentLevel() {
        // Crée le Level
        Level lvl = levelSuppliers.get(currentLevelIndex).apply(player);

        // Si on avait déjà un contrôleur, arrête proprement sa boucle
        if (controller != null) {
            controller.stopGameLoop();
        }

        // Redessine le Canvas (avec son nouveau GraphicsContext)
        GraphicsContext gc = canvas.getGraphicsContext2D();
        GameView view = new GameView(gc);

        // Crée le nouveau contrôleur pour ce niveau
        controller = new GameController(
            player,
            lvl.getPlatforms(),
            lvl.getEnemies(),
            lvl.getDecorations(),
            view,
            this,
            lvl
        );

        // Lie les entrées clavier
        controller.handleInput(scene);
        // Démarre la boucle
        controller.startGameLoop();
    }

    /** Passe au niveau suivant (ou termine le jeu) */
    public void nextLevel() {
        // reset du joueur
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
