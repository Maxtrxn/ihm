package src.view;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.List;

public class GameView {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    private DoubleProperty cameraX = new SimpleDoubleProperty(0);
    private DoubleProperty cameraY = new SimpleDoubleProperty(0);
    private GraphicsContext gc;
    private Image spriteSheet;
    private Image playerWalkSpriteSheet;
    private int frameIndex = 0;
    private int frameCount;
    private int frameWidth;
    private int frameHeight;
    private long lastFrameTime = 0;
    private long frameDuration = 100_000_000; // Durée de chaque frame en nanosecondes (100ms)

    public GameView(GraphicsContext gc) {
        this.gc = gc;
        try {
            this.spriteSheet = new Image("file:../textures/engrenage_animation-Sheet.png");
            this.playerWalkSpriteSheet = new Image("file:../textures/wrench_walk.png");
            if (playerWalkSpriteSheet.isError()) {
                System.out.println("Error loading player walk sprite sheet.");
            } else {
                this.frameHeight = (int) playerWalkSpriteSheet.getHeight();
                this.frameWidth = (int) playerWalkSpriteSheet.getWidth() / 4; // Suppose 4 frames
                this.frameCount = 4;
            }
        } catch (Exception e) {
            System.out.println("Exception loading images: " + e.getMessage());
        }
    }

    public void draw(Image backgroundImage, double playerX, double playerY, double playerWidth, double playerHeight, boolean isWalking, List<Image> platformImages, List<Double[]> platformPositions, List<Double[]> enemyPositions) {
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        // Dessiner l'image d'arrière-plan
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT);
        }

        // Dessiner le joueur
        if (isWalking && playerWalkSpriteSheet != null) {
            long currentTime = System.nanoTime();
            if (currentTime - lastFrameTime >= frameDuration) {
                frameIndex = (frameIndex + 1) % frameCount;
                lastFrameTime = currentTime;
            }
            int frameX = frameIndex * frameWidth;
            gc.drawImage(playerWalkSpriteSheet, frameX, 0, frameWidth, frameHeight, playerX - cameraX.get(), playerY - cameraY.get(), playerWidth, playerHeight);
        } else {
            gc.setFill(javafx.scene.paint.Color.RED);
            gc.fillRect(playerX - cameraX.get(), playerY - cameraY.get(), playerWidth, playerHeight);
        }

        // Dessiner les plateformes
        for (int i = 0; i < platformImages.size(); i++) {
            Image platformImage = platformImages.get(i);
            Double[] position = platformPositions.get(i);
            if (platformImage != null) {
                gc.drawImage(platformImage, position[0] - cameraX.get(), position[1] - cameraY.get(), position[2], position[3]);
            }
        }
    }

    public DoubleProperty cameraXProperty() {
        return cameraX;
    }

    public DoubleProperty cameraYProperty() {
        return cameraY;
    }
}