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

    private final DoubleProperty cameraX = new SimpleDoubleProperty(0);
    private final DoubleProperty cameraY = new SimpleDoubleProperty(0);
    private final GraphicsContext gc;

    // ------------------------------------------------------------
    // 1) Animation de l'engrenage
    // ------------------------------------------------------------
    private Image gearSpriteSheet;
    private int gearFrameIndex = 0;
    private int gearFrameCount = 0;
    private int gearFrameWidth = 0;
    private int gearFrameHeight = 0;
    private long lastGearFrameTime = 0;
    private final long gearFrameDuration = 50_000_000; // 50 ms par frame

    // ------------------------------------------------------------
    // 2) Feuille de sprites du joueur en Idle
    // ------------------------------------------------------------
    private Image playerIdleSheet;
    private int idleFrameIndex = 0;
    private int idleFrameCount = 0;
    private int idleFrameWidth = 0;
    private int idleFrameHeight = 0;
    private long lastIdleFrameTime = 0;
    private final long idleFrameDuration = 100_000_000; // 100 ms par frame

    // ------------------------------------------------------------
    // 3) Feuille de sprites du joueur en Walking
    // ------------------------------------------------------------
    private Image playerWalkSheet;
    private int walkFrameIndex = 0;
    private int walkFrameCount = 0;
    private int walkFrameWidth = 0;
    private int walkFrameHeight = 0;
    private long lastWalkFrameTime = 0;
    private final long walkFrameDuration = 100_000_000; // 100 ms par frame

    // ------------------------------------------------------------
    // 4) Offset d'affichage du joueur
    // ------------------------------------------------------------
    private final double playerOffsetY = 50; // Ajuste cette valeur à ta guise

    public GameView(GraphicsContext gc) {
        this.gc = gc;
        try {
            // ----------------------------------------------------------------
            // Engrenage
            // ----------------------------------------------------------------
            gearSpriteSheet = new Image("file:../textures/engrenage_animation-Sheet.png");
            if (!gearSpriteSheet.isError()) {
                gearFrameHeight = (int) gearSpriteSheet.getHeight();
                gearFrameWidth  = gearFrameHeight;
                if (gearFrameWidth != 0) {
                    gearFrameCount = (int) (gearSpriteSheet.getWidth() / gearFrameWidth);
                }
                System.out.println("Engrenage chargé : " + gearFrameCount + " frames");
            }

            // ----------------------------------------------------------------
            // Joueur Idle
            // ----------------------------------------------------------------
            playerIdleSheet = new Image("file:../textures/static wrench-Sheet.png");
            if (!playerIdleSheet.isError()) {
                idleFrameHeight = (int) playerIdleSheet.getHeight();
                idleFrameWidth  = idleFrameHeight;
                if (idleFrameWidth != 0) {
                    idleFrameCount = (int) (playerIdleSheet.getWidth() / idleFrameWidth);
                }
                System.out.println("Feuille Idle chargée : " + idleFrameCount + " frames");
            }

            // ----------------------------------------------------------------
            // Joueur Walk
            // ----------------------------------------------------------------
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
     * Méthode de dessin modifiée pour gérer l'inversion horizontale du joueur.
     *
     * @param backgroundImage Image d'arrière-plan
     * @param playerX Position X du joueur
     * @param playerY Position Y du joueur
     * @param playerWidth Largeur du joueur
     * @param playerHeight Hauteur du joueur
     * @param isWalking Indique si le joueur est en mouvement
     * @param facingRight Indique la direction du joueur (true = face à droite)
     * @param platformImages Liste des images des plateformes
     * @param platformPositions Liste des positions et tailles des plateformes
     * @param enemyPositions Liste des positions et tailles des ennemis
     */
    public void draw(Image backgroundImage,
                     double playerX, double playerY,
                     double playerWidth, double playerHeight,
                     boolean isWalking,
                     boolean facingRight,
                     List<Image> platformImages,
                     List<Double[]> platformPositions,
                     List<Double[]> enemyPositions) {

        // Nettoyage
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        // Arrière-plan
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT);
        }

        // Engrenage (animation)
        if (gearSpriteSheet != null && gearFrameWidth != 0 && gearFrameHeight != 0) {
            long currentTime = System.nanoTime();
            if (currentTime - lastGearFrameTime >= gearFrameDuration) {
                gearFrameIndex = (gearFrameIndex + 1) % gearFrameCount;
                lastGearFrameTime = currentTime;
            }
            int frameX = gearFrameIndex * gearFrameWidth;
            gc.drawImage(gearSpriteSheet, frameX, 0,
                         gearFrameWidth, gearFrameHeight,
                         0, 0,
                         gearFrameWidth, gearFrameHeight);
        }

        // Joueur
        double scaleFactor = 2.0;
        double drawX = (playerX - cameraX.get());
        double drawY = (playerY - cameraY.get()) - playerOffsetY;

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
                             drawX,
                             drawY,
                             playerWidth * scaleFactor,
                             playerHeight * scaleFactor);
            } else {
                gc.save();
                // On translate pour dessiner l'image inversée
                gc.translate(drawX + playerWidth * scaleFactor, drawY);
                gc.scale(-1, 1);
                gc.drawImage(playerWalkSheet,
                             frameX, 0, walkFrameWidth, walkFrameHeight,
                             0,
                             0,
                             playerWidth * scaleFactor,
                             playerHeight * scaleFactor);
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
                             drawX,
                             drawY,
                             playerWidth * scaleFactor,
                             playerHeight * scaleFactor);
            } else {
                gc.save();
                gc.translate(drawX + playerWidth * scaleFactor, drawY);
                gc.scale(-1, 1);
                gc.drawImage(playerIdleSheet,
                             frameX, 0, idleFrameWidth, idleFrameHeight,
                             0,
                             0,
                             playerWidth * scaleFactor,
                             playerHeight * scaleFactor);
                gc.restore();
            }
        }

        // Plateformes
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

        // Ennemis
        gc.setFill(Color.GREEN);
        for (Double[] pos : enemyPositions) {
            double ex = pos[0] - cameraX.get();
            double ey = pos[1] - cameraY.get();
            double ew = pos[2];
            double eh = pos[3];
            gc.fillRect(ex, ey, ew, eh);
        }
    }

    public DoubleProperty cameraXProperty() {
        return cameraX;
    }

    public DoubleProperty cameraYProperty() {
        return cameraY;
    }
}
