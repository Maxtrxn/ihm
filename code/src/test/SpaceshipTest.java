package test;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;

/**
 * Exemple minimal : un "vaisseau" (cercle) qui se déplace
 * librement avec les flèches. Aucune gravité, aucune collision.
 */
public class SpaceshipTest extends Application {

    private double x = 100;   // Position initiale du "vaisseau"
    private double y = 300;
    private double speed = 3; // Vitesse de déplacement

    private boolean up, down, left, right;

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, 800, 600);

        // Gestion des touches
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.UP)    up    = true;
            if (e.getCode() == KeyCode.DOWN)  down  = true;
            if (e.getCode() == KeyCode.LEFT)  left  = true;
            if (e.getCode() == KeyCode.RIGHT) right = true;
        });
        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.UP)    up    = false;
            if (e.getCode() == KeyCode.DOWN)  down  = false;
            if (e.getCode() == KeyCode.LEFT)  left  = false;
            if (e.getCode() == KeyCode.RIGHT) right = false;
        });

        primaryStage.setTitle("Spaceship Test");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Boucle de jeu : ~60 fps
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                draw(gc);
            }
        };
        timer.start();
    }

    /**
     * Met à jour la position du "vaisseau".
     */
    private void update() {
        if (up)    y -= speed;
        if (down)  y += speed;
        if (left)  x -= speed;
        if (right) x += speed;
    }

    /**
     * Dessine la scène : efface l'écran et dessine un cercle.
     */
    private void draw(GraphicsContext gc) {
        gc.clearRect(0, 0, 800, 600);

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 800, 600);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(18));
        gc.fillText("Use arrow keys to move the spaceship.\nX=" + x + ", Y=" + y, 20, 30);

        // Dessine un cercle comme "vaisseau"
        gc.setFill(Color.YELLOW);
        gc.fillOval(x, y, 40, 40);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
