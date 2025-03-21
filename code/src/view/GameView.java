package src.view;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.List;

public class GameView {
    private final DoubleProperty cameraX = new SimpleDoubleProperty(0);
    private final DoubleProperty cameraY = new SimpleDoubleProperty(0);
    private final GraphicsContext gc;

    // ------------------------------------------------------------
    // Animation de l'engrenage
    // ------------------------------------------------------------
    private Image gearSpriteSheet;
    private int gearFrameIndex = 0;
    private int gearFrameCount = 0;
    private int gearFrameWidth = 0;
    private int gearFrameHeight = 0;
    private long lastGearFrameTime = 0;
    private final long gearFrameDuration = 50_000_000; // 50 ms

    // ------------------------------------------------------------
    // Joueur Idle
    // ------------------------------------------------------------
    private Image playerIdleSheet;
    private int idleFrameIndex = 0;
    private int idleFrameCount = 0;
    private int idleFrameWidth = 0;
    private int idleFrameHeight = 0;
    private long lastIdleFrameTime = 0;
    private final long idleFrameDuration = 100_000_000; // 100 ms

    // ------------------------------------------------------------
    // Joueur Walking
    // ------------------------------------------------------------
    private Image playerWalkSheet;
    private int walkFrameIndex = 0;
    private int walkFrameCount = 0;
    private int walkFrameWidth = 0;
    private int walkFrameHeight = 0;
    private long lastWalkFrameTime = 0;
    private final long walkFrameDuration = 100_000_000; // 100 ms

    // ------------------------------------------------------------
    // Offset d'affichage du joueur
    // ------------------------------------------------------------
    private final double playerOffsetY = 50; // Ajuste si besoin

    public GameView(GraphicsContext gc) {
        this.gc = gc;
        try {
            // Engrenage
            gearSpriteSheet = new Image("file:../textures/engrenage_animation-Sheet.png");
            if (!gearSpriteSheet.isError()) {
                gearFrameHeight = (int) gearSpriteSheet.getHeight();
                gearFrameWidth  = gearFrameHeight;
                if (gearFrameWidth != 0) {
                    gearFrameCount = (int) (gearSpriteSheet.getWidth() / gearFrameWidth);
                }
                System.out.println("Engrenage chargé : " + gearFrameCount + " frames");
            }

            // Joueur Idle
            playerIdleSheet = new Image("file:../textures/static wrench-Sheet.png");
            if (!playerIdleSheet.isError()) {
                idleFrameHeight = (int) playerIdleSheet.getHeight();
                idleFrameWidth  = idleFrameHeight;
                if (idleFrameWidth != 0) {
                    idleFrameCount = (int) (playerIdleSheet.getWidth() / idleFrameWidth);
                }
                System.out.println("Feuille Idle chargée : " + idleFrameCount + " frames");
            }

            // Joueur Walk
            playerWalkSheet = new Image("file:../textures/sprite sheet wrench walking.png");
            if (!playerWalkSheet.isError()) {
                walkFrameHeight = (int) playerWalkSheet.getHeight();
                walkFrameWidth  = walkFrameHeight;
                if (walkFrameWidth != 0) {
                    walkFrameCount = (int) (playerWalkSheet.getWidth() / walkFrameWidth);
                }
                System.out.println("Feuille Walk chargée : " + walkFrameCount + " frames");
            }

        } catch (Exception e) {
            System.err.println("Exception loading images: " + e.getMessage());
        }
    }

    /**
     * Dessine le jeu avec un background qui ne bouge pas et remplit la fenêtre.
     * => Pas de scrolling horizontal/vertical du fond, plus de zones noires.
     */
    public void draw(Image backgroundImgFromLevel,
                     double playerX, double playerY,
                     double playerWidth, double playerHeight,
                     boolean isWalking,
                     boolean facingRight,
                     List<Image> platformImages,
                     List<Double[]> platformPositions,
                     List<Double[]> enemyPositions) {

        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();

        // Fond noir pour effacer le canvas
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        // ------------------------------------------------------------
        // BACKGROUND (statique, plein écran)
        // ------------------------------------------------------------
        if (backgroundImgFromLevel != null) {
            // On l'étire à la taille du canvas (pas de scrolling)
            gc.drawImage(backgroundImgFromLevel, 0, 0, canvasWidth, canvasHeight);
        }

        // ------------------------------------------------------------
        // ENGRENADE (toujours en haut à gauche)
        // ------------------------------------------------------------
        if (gearSpriteSheet != null && gearFrameWidth != 0 && gearFrameHeight != 0) {
            long currentTime = System.nanoTime();
            if (currentTime - lastGearFrameTime >= gearFrameDuration) {
                gearFrameIndex = (gearFrameIndex + 1) % gearFrameCount;
                lastGearFrameTime = currentTime;
            }
            int frameX = gearFrameIndex * gearFrameWidth;
            gc.drawImage(gearSpriteSheet,
                         frameX, 0, gearFrameWidth, gearFrameHeight,
                         0, 0,
                         gearFrameWidth, gearFrameHeight);
        }

        // ------------------------------------------------------------
        // JOUEUR
        // ------------------------------------------------------------
        // On garde un scaleFactor pour que le joueur soit plus grand en pixels
        double scaleFactor = 2.0;

        double drawX = playerX - cameraX.get();
        double drawY = playerY - cameraY.get() - playerOffsetY;

        // Walk / Idle
        if (isWalking && playerWalkSheet != null && walkFrameWidth != 0 && walkFrameHeight != 0) {
            long currentTime = System.nanoTime();
            if (currentTime - lastWalkFrameTime >= walkFrameDuration) {
                walkFrameIndex = (walkFrameIndex + 1) % walkFrameCount;
                lastWalkFrameTime = currentTime;
            }
            int frameX = walkFrameIndex * walkFrameWidth;

            if (facingRight) {
                gc.drawImage(playerWalkSheet,
                             frameX, 0, walkFrameWidth, walkFrameHeight,
                             drawX, drawY,
                             playerWidth * scaleFactor, playerHeight * scaleFactor);
            } else {
                gc.save();
                gc.translate(drawX + playerWidth * scaleFactor, drawY);
                gc.scale(-1, 1);
                gc.drawImage(playerWalkSheet,
                             frameX, 0, walkFrameWidth, walkFrameHeight,
                             0, 0,
                             playerWidth * scaleFactor, playerHeight * scaleFactor);
                gc.restore();
            }

        } else if (!isWalking && playerIdleSheet != null && idleFrameWidth != 0 && idleFrameHeight != 0) {
            long currentTime = System.nanoTime();
            if (currentTime - lastIdleFrameTime >= idleFrameDuration) {
                idleFrameIndex = (idleFrameIndex + 1) % idleFrameCount;
                lastIdleFrameTime = currentTime;
            }
            int frameX = idleFrameIndex * idleFrameWidth;

            if (facingRight) {
                gc.drawImage(playerIdleSheet,
                             frameX, 0, idleFrameWidth, idleFrameHeight,
                             drawX, drawY,
                             playerWidth * scaleFactor, playerHeight * scaleFactor);
            } else {
                gc.save();
                gc.translate(drawX + playerWidth * scaleFactor, drawY);
                gc.scale(-1, 1);
                gc.drawImage(playerIdleSheet,
                             frameX, 0, idleFrameWidth, idleFrameHeight,
                             0, 0,
                             playerWidth * scaleFactor, playerHeight * scaleFactor);
                gc.restore();
            }
        }

        // ------------------------------------------------------------
        // PLATEFORMES
        // ------------------------------------------------------------
        for (int i = 0; i < platformImages.size(); i++) {
            Image platformImage = platformImages.get(i);
            Double[] pos = platformPositions.get(i);
            double px = pos[0] - cameraX.get();
            double py = pos[1] - cameraY.get();
            double pw = pos[2];
            double ph = pos[3];

            if (platformImage != null) {
                gc.drawImage(platformImage, px, py, pw, ph);
            } else {
                gc.setFill(Color.BLUE);
                gc.fillRect(px, py, pw, ph);
            }
        }

        // ------------------------------------------------------------
        // ENNEMIS
        // ------------------------------------------------------------
        gc.setFill(Color.GREEN);
        for (Double[] pos : enemyPositions) {
            double ex = pos[0] - cameraX.get();
            double ey = pos[1] - cameraY.get();
            double ew = pos[2];
            double eh = pos[3];
            gc.fillRect(ex, ey, ew, eh);
        }
    }

    // Méthodes utilitaires
    public double getCanvasWidth() {
        return gc.getCanvas().getWidth();
    }
    public double getCanvasHeight() {
        return gc.getCanvas().getHeight();
    }

    public DoubleProperty cameraXProperty() {
        return cameraX;
    }
    public DoubleProperty cameraYProperty() {
        return cameraY;
    }
}
