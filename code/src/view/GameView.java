package src.view;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import src.model.Player;
import src.model.Platform;
import src.model.Enemy;

import java.util.List;

public class GameView {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    private DoubleProperty cameraX = new SimpleDoubleProperty(0);
    private DoubleProperty cameraY = new SimpleDoubleProperty(0);
    private GraphicsContext gc;
    private Image spriteSheet;
    private Image backgroundImage;
    private int frameIndex = 0;
    private int frameCount;
    private int frameWidth;
    private int frameHeight;
    private long lastFrameTime = 0;
    private long frameDuration = 50_000_000; // Durée de chaque frame en nanosecondes (100ms)
    private Image platformImage = new Image("file:../textures/plateforme_acier_1.png");

    public GameView(GraphicsContext gc) {
        this.gc = gc;
        try {
            // Utilisez un chemin absolu pour charger la feuille de sprites
            this.spriteSheet = new Image("file:../textures/engrenage_animation-Sheet.png");
            //this.backgroundImage = new Image("file:src/resources/test.png");
            if (spriteSheet.isError()) {
                System.out.println("Error loading sprite sheet.");
            } else {
                this.frameHeight = (int) spriteSheet.getHeight();
                System.out.println("Frame height: " + frameHeight);
                this.frameWidth = this.frameHeight; // Puisque chaque frame est carrée
                if (frameWidth != 0) {
                    this.frameCount = (int) (spriteSheet.getWidth() / frameWidth);
                    System.out.println("Sprite sheet loaded successfully. Frame count: " + frameCount);
                    System.out.println("Frame width: " + frameWidth + ", Frame height: " + frameHeight);
                } else {
                    System.out.println("Error: frameWidth is zero.");
                }
            }
            if (backgroundImage.isError()) {
                System.out.println("Error loading background image.");
            }
        } catch (Exception e) {
            System.out.println("Exception loading images: " + e.getMessage());
        }
    }

    public void draw(Player player, List<Platform> platforms, List<Enemy> enemies) {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        
        // Dessiner l'image d'arrière-plan
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT);
        } else {
            System.out.println("Background image is null.");
        }

        // Dessiner l'animation de la roue
        if (spriteSheet != null && frameWidth != 0 && frameHeight != 0) {
            long currentTime = System.nanoTime();
            if (currentTime - lastFrameTime >= frameDuration) {
                frameIndex = (frameIndex + 1) % frameCount;
                lastFrameTime = currentTime;
            }
            int frameX = frameIndex * frameWidth;
            gc.drawImage(spriteSheet, frameX, 0, frameWidth, frameHeight, 0, 0, frameWidth, frameHeight);
        } else {
            System.out.println("Sprite sheet is null or frame dimensions are zero.");
            System.out.println("frameWidth: " + frameWidth + ", frameHeight: " + frameHeight);
        }

        gc.setFill(Color.RED);
        gc.fillRect(player.getX() - cameraX.get(), player.getY() - cameraY.get(), player.getWidth(), player.getHeight());
        for (Platform platform : platforms) {
            if (platform.getTexture() != null) {
                gc.drawImage(platform.getTexture(), platform.getX() - cameraX.get(), platform.getY() - cameraY.get(), platform.getWidth(), platform.getHeight());
            } else {
                System.err.println("Platform texture is null.");
                gc.setFill(Color.BLUE);
                gc.fillRect(platform.getX() - cameraX.get(), platform.getY() - cameraY.get(), platform.getWidth(), platform.getHeight());
            }
        }

        gc.setFill(Color.GREEN);
        for (Enemy enemy : enemies) {
            gc.fillRect(enemy.getX() - cameraX.get(), enemy.getY() - cameraY.get(), enemy.getWidth(), enemy.getHeight());
        }
    }

    public DoubleProperty cameraXProperty() {
        return cameraX;
    }

    public DoubleProperty cameraYProperty() {
        return cameraY;
    }
}