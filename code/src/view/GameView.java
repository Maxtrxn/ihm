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

    public GameView(GraphicsContext gc) {
        this.gc = gc;
        try {
            // ----------------------------------------------------------------
            // Charger la feuille de sprites de l'engrenage
            // ----------------------------------------------------------------
            gearSpriteSheet = new Image("file:../textures/engrenage_animation-Sheet.png");
            if (!gearSpriteSheet.isError()) {
                gearFrameHeight = (int) gearSpriteSheet.getHeight();
                gearFrameWidth  = gearFrameHeight; // On suppose frames carrées
                if (gearFrameWidth != 0) {
                    gearFrameCount = (int) (gearSpriteSheet.getWidth() / gearFrameWidth);
                }
                System.out.println("Engrenage chargé : " + gearFrameCount + " frames");
            } else {
                System.err.println("Erreur chargement engrenage_animation-Sheet.png.");
            }

            // ----------------------------------------------------------------
            // Charger la feuille de sprites IDLE du joueur
            // ----------------------------------------------------------------
            playerIdleSheet = new Image("file:../textures/static wrench-Sheet.png");
            if (!playerIdleSheet.isError()) {
                idleFrameHeight = (int) playerIdleSheet.getHeight();
                idleFrameWidth  = idleFrameHeight;
                if (idleFrameWidth != 0) {
                    idleFrameCount = (int) (playerIdleSheet.getWidth() / idleFrameWidth);
                }
                System.out.println("Feuille Idle chargée : " + idleFrameCount + " frames");
            } else {
                System.err.println("Erreur chargement static wrench-Sheet.png.");
            }

            // ----------------------------------------------------------------
            // Charger la feuille de sprites WALK du joueur
            // ----------------------------------------------------------------
            playerWalkSheet = new Image("file:../textures/sprite sheet wrench walking.png");
            if (!playerWalkSheet.isError()) {
                walkFrameHeight = (int) playerWalkSheet.getHeight();
                walkFrameWidth  = walkFrameHeight; // On suppose frames carrées
                if (walkFrameWidth != 0) {
                    walkFrameCount = (int) (playerWalkSheet.getWidth() / walkFrameWidth);
                }
                System.out.println("Feuille Walk chargée : " + walkFrameCount + " frames");
            } else {
                System.err.println("Erreur chargement sprite sheet wrench walking.png.");
            }

        } catch (Exception e) {
            System.err.println("Exception loading images: " + e.getMessage());
        }
    }

    public void draw(Image backgroundImage,
                     double playerX, double playerY,
                     double playerWidth, double playerHeight,
                     boolean isWalking,
                     List<Image> platformImages,
                     List<Double[]> platformPositions,
                     List<Double[]> enemyPositions) {

        // Effacer la zone de dessin
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        // -----------------------------------------------------
        // 1) Dessiner l'image d'arrière-plan
        // -----------------------------------------------------
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT);
        }

        // -----------------------------------------------------
        // 2) Dessiner l'animation de l'engrenage
        // -----------------------------------------------------
        if (gearSpriteSheet != null && gearFrameWidth != 0 && gearFrameHeight != 0) {
            long currentTime = System.nanoTime();
            if (currentTime - lastGearFrameTime >= gearFrameDuration) {
                gearFrameIndex = (gearFrameIndex + 1) % gearFrameCount;
                lastGearFrameTime = currentTime;
            }
            int frameX = gearFrameIndex * gearFrameWidth;
            gc.drawImage(gearSpriteSheet,
                         frameX, 0,
                         gearFrameWidth, gearFrameHeight,
                         0, 0, // position à l'écran
                         gearFrameWidth, gearFrameHeight);
        }

        // -----------------------------------------------------
        // 3) Dessiner le joueur
        // -----------------------------------------------------
        double scaleFactor = 2.0;

        if (isWalking && playerWalkSheet != null && walkFrameWidth != 0 && walkFrameHeight != 0) {
            // Animation de marche
            long currentTime = System.nanoTime();
            if (currentTime - lastWalkFrameTime >= walkFrameDuration) {
                walkFrameIndex = (walkFrameIndex + 1) % walkFrameCount;
                lastWalkFrameTime = currentTime;
            }
            int frameX = walkFrameIndex * walkFrameWidth;
            gc.drawImage(playerWalkSheet,
                         frameX, 0,
                         walkFrameWidth, walkFrameHeight,
                         playerX - cameraX.get(), playerY - cameraY.get(),
                         playerWidth * scaleFactor, playerHeight * scaleFactor);

        } else if (!isWalking && playerIdleSheet != null && idleFrameWidth != 0 && idleFrameHeight != 0) {
            // Animation Idle
            long currentTime = System.nanoTime();
            if (currentTime - lastIdleFrameTime >= idleFrameDuration) {
                idleFrameIndex = (idleFrameIndex + 1) % idleFrameCount;
                lastIdleFrameTime = currentTime;
            }
            int frameX = idleFrameIndex * idleFrameWidth;
            gc.drawImage(playerIdleSheet,
                         frameX, 0,
                         idleFrameWidth, idleFrameHeight,
                         playerX - cameraX.get(), playerY - cameraY.get(),
                         playerWidth * scaleFactor, playerHeight * scaleFactor);
        }

        // -----------------------------------------------------
        // 4) Dessiner les plateformes
        // -----------------------------------------------------
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

        // -----------------------------------------------------
        // 5) Dessiner les ennemis
        // -----------------------------------------------------
        gc.setFill(Color.GREEN);
        for (Double[] pos : enemyPositions) {
            double ex = pos[0] - cameraX.get();
            double ey = pos[1] - cameraY.get();
            double ew = pos[2];
            double eh = pos[3];
            gc.fillRect(ex, ey, ew, eh);
        }
    }

    // -----------------------------------------------------
    // Propriétés de la caméra (pour scroll)
    // -----------------------------------------------------
    public DoubleProperty cameraXProperty() {
        return cameraX;
    }

    public DoubleProperty cameraYProperty() {
        return cameraY;
    }
}
