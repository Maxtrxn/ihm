package src.view;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.List;

public class GameView {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    private DoubleProperty cameraX = new SimpleDoubleProperty(0);
    private DoubleProperty cameraY = new SimpleDoubleProperty(0);
    private GraphicsContext gc;
    private Image spriteSheet;
    private Image playerImage;
    private int frameIndex = 0;
    private int frameCount;
    private int frameWidth;
    private int frameHeight;
    private long lastFrameTime = 0;
    private long frameDuration = 50_000_000; // Durée de chaque frame en nanosecondes (100ms)

    public GameView(GraphicsContext gc) {
        this.gc = gc;
        try {
            // Utilisez un chemin absolu ou relatif correct pour charger la feuille de sprites
            this.spriteSheet = new Image("file:../textures/engrenage_animation-Sheet.png");
            this.playerImage = new Image("file:../textures/wrench.png");
            if (spriteSheet.isError()) {
                System.out.println("Error loading sprite sheet.");
            } else {
                this.frameHeight = (int) spriteSheet.getHeight();
                System.out.println("Frame height: " + frameHeight);
                // Supposons que la feuille de sprites comporte des frames carrées
                this.frameWidth = this.frameHeight;
                if (frameWidth != 0) {
                    this.frameCount = (int) (spriteSheet.getWidth() / frameWidth);
                    System.out.println("Sprite sheet loaded successfully. Frame count: " + frameCount);
                    System.out.println("Frame width: " + frameWidth + ", Frame height: " + frameHeight);
                } else {
                    System.out.println("Error: frameWidth is zero.");
                }
            }
            if (playerImage.isError()) {
                System.out.println("Error loading player image.");
            }
        } catch (Exception e) {
            System.out.println("Exception loading images: " + e.getMessage());
        }
    }

    /**
     * Ajout du paramètre boolean isWalking pour correspondre à l'appel dans GameController.
     */
    public void draw(Image backgroundImage,
                     double playerX, double playerY,
                     double playerWidth, double playerHeight,
                     boolean isWalking,
                     List<Image> platformImages,
                     List<Double[]> platformPositions,
                     List<Double[]> enemyPositions) {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        
        // Dessiner l'image d'arrière-plan
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT);
        } else {
            System.out.println("Background image is null.");
        }

        // Dessiner l'animation de la roue (spriteSheet)
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
            System.out.println("frameWidth: " + frameWidth + ", Frame height: " + frameHeight);
        }

        // Dessiner le joueur avec l'image wrench.png agrandie
        if (playerImage != null) {
            double scaleFactor = 2; // Facteur d'agrandissement
            gc.drawImage(playerImage,
                         playerX - cameraX.get(), playerY - cameraY.get(),
                         playerWidth * scaleFactor, playerHeight * scaleFactor);
        } else {
            System.out.println("Player image is null.");
        }

        // Dessiner les plateformes
        for (int i = 0; i < platformImages.size(); i++) {
            Image platformImage = platformImages.get(i);
            Double[] position = platformPositions.get(i);
            if (platformImage != null) {
                gc.drawImage(platformImage,
                             position[0] - cameraX.get(), position[1] - cameraY.get(),
                             position[2], position[3]);
            } else {
                System.err.println("Platform texture is null.");
                gc.setFill(Color.BLUE);
                gc.fillRect(position[0] - cameraX.get(), position[1] - cameraY.get(),
                            position[2], position[3]);
            }
        }

        // Dessiner les ennemis
        gc.setFill(Color.GREEN);
        for (Double[] position : enemyPositions) {
            gc.fillRect(position[0] - cameraX.get(), position[1] - cameraY.get(),
                        position[2], position[3]);
        }

        // (Optionnel) Si tu veux utiliser isWalking pour afficher une animation
        // différente, tu peux ajouter ta logique ici :
        //
        // if (isWalking) {
        //     // Exemple : dessiner un effet de mouvement
        // }
    }

    public DoubleProperty cameraXProperty() {
        return cameraX;
    }

    public DoubleProperty cameraYProperty() {
        return cameraY;
    }
}
