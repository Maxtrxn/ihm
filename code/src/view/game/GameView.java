// src/view/GameView.java
package src.view.game;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import src.common.ResourcesPaths;
import src.controller.game.GameController;
import src.model.game.Boss;
import src.model.game.Enemy;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class GameView {
    private static final int WIDTH  = 800;
    private static final int HEIGHT = 600;
    private final DoubleProperty cameraX = new SimpleDoubleProperty(0);
    private final DoubleProperty cameraY = new SimpleDoubleProperty(0);
    private final GraphicsContext gc;
    private Pane root;
    private Scene scene;
    private Canvas canvas;
    private GameController controller;

    // Cache pour le background redimensionné
    private Image cachedBackground = null;
    private double cachedWidth = 0, cachedHeight = 0;

    // ------------------------------------------------------------
    // Engrenage
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
    // Joueur Walk
    // ------------------------------------------------------------
    private Image playerWalkSheet;
    private int walkFrameIndex = 0;
    private int walkFrameCount = 0;
    private int walkFrameWidth = 0;
    private int walkFrameHeight = 0;
    private long lastWalkFrameTime = 0;
    private final long walkFrameDuration = 100_000_000; // 100 ms

    // ------------------------------------------------------------
    // Joueur Jump
    // ------------------------------------------------------------
    private Image playerJumpSheet;
    private int jumpFrameIndex = 0;
    private int jumpFrameCount = 0;
    private int jumpFrameWidth = 0;
    private int jumpFrameHeight = 0;
    private long lastJumpFrameTime = 0;
    private final long jumpFrameDuration = 100_000_000; // 100 ms

    // ------------------------------------------------------------
    // Sprite du vaisseau
    // ------------------------------------------------------------
    private Image spaceshipImage;
    private final double playerOffsetY = 50;

    public GameView(GameController controller, Stage primaryStage) {
        this.controller = controller;
        root = new Pane();
        canvas = new Canvas(WIDTH, HEIGHT);
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());
        root.getChildren().add(canvas);
        scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);

        this.gc = canvas.getGraphicsContext2D();
        canvas.setCache(true);
        canvas.setCacheHint(CacheHint.SPEED);

        try {
            // Engrenage
            gearSpriteSheet = new Image("file:" + ResourcesPaths.TEXTURES_FOLDER + "engrenage_animation-Sheet.png");
            if (!gearSpriteSheet.isError()) {
                gearFrameHeight = (int) gearSpriteSheet.getHeight();
                gearFrameWidth  = gearFrameHeight;
                if (gearFrameWidth != 0) {
                    gearFrameCount = (int) (gearSpriteSheet.getWidth() / gearFrameWidth);
                }
            }

            // Joueur Idle
            playerIdleSheet = new Image("file:" + ResourcesPaths.TEXTURES_FOLDER + "static wrench-Sheet.png");
            if (!playerIdleSheet.isError()) {
                idleFrameHeight = (int) playerIdleSheet.getHeight();
                idleFrameWidth  = idleFrameHeight;
                if (idleFrameWidth != 0) {
                    idleFrameCount = (int) (playerIdleSheet.getWidth() / idleFrameWidth);
                }
            }

            // Joueur Walk
            playerWalkSheet = new Image("file:"+ ResourcesPaths.TEXTURES_FOLDER +"sprite sheet wrench walking.png");
            if (!playerWalkSheet.isError()) {
                walkFrameHeight = (int) playerWalkSheet.getHeight();
                walkFrameWidth  = walkFrameHeight;
                if (walkFrameWidth != 0) {
                    walkFrameCount = (int) (playerWalkSheet.getWidth() / walkFrameWidth);
                }
            }

            // Joueur Jump
            playerJumpSheet = new Image("file:" + ResourcesPaths.TEXTURES_FOLDER + "jump2 wrench-Sheet.png");
            if (!playerJumpSheet.isError()) {
                jumpFrameHeight = (int) playerJumpSheet.getHeight();
                jumpFrameWidth  = jumpFrameHeight;
                if (jumpFrameWidth != 0) {
                    jumpFrameCount = (int) (playerJumpSheet.getWidth() / jumpFrameWidth);
                }
            }

            // Sprite vaisseau
            spaceshipImage = new Image("file:" + ResourcesPaths.TEXTURES_FOLDER + "dirigeable v1.png");
        } catch (Exception e) {
            System.err.println("Exception loading images: " + e.getMessage());
        }
    }

    /**
     * Retourne une version redimensionnée du background (thread-safe).
     */
    private Image getScaledBackground(Image background, double width, double height) {
        if (Platform.isFxApplicationThread()) {
            return createScaledBackground(background, width, height);
        } else {
            final Image[] result = new Image[1];
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                result[0] = createScaledBackground(background, width, height);
                latch.countDown();
            });
            try {
                latch.await();
            } catch (InterruptedException ignored) {}
            return result[0];
        }
    }

    private Image createScaledBackground(Image background, double width, double height) {
        Canvas temp = new Canvas(width, height);
        GraphicsContext gc2 = temp.getGraphicsContext2D();
        gc2.drawImage(background, 0, 0, width, height);
        WritableImage wi = new WritableImage((int) width, (int) height);
        temp.snapshot(null, wi);
        return wi;
    }

    /**
     * Dessine la frame complète, avec décorations, plateformes, ennemis et projectiles.
     */
    public void draw(
        Image background,
        double playerX, double playerY,
        double playerW, double playerH,
        boolean isWalking, boolean facingRight,
        boolean isJumping, boolean spaceshipMode,
        List<Image> decorationImages,
        List<Double[]> decorationPositions,
        List<Image> platformImages,
        List<Double[]> platformPositions,
        List<Enemy> enemies,
        List<Double[]> projectilePositions
    ) {
        double cw = gc.getCanvas().getWidth();
        double ch = gc.getCanvas().getHeight();

        // Fond noir
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, cw, ch);

        // Background mis à l’échelle + cache
        if (background != null) {
            if (cachedBackground == null || cachedWidth != cw || cachedHeight != ch) {
                cachedBackground = getScaledBackground(background, cw, ch);
                cachedWidth = cw; cachedHeight = ch;
            }
            gc.drawImage(cachedBackground, 0, 0);
        }

        // Engrenage
        if (gearSpriteSheet != null && gearFrameWidth > 0) {
            long now = System.nanoTime();
            if (now - lastGearFrameTime >= gearFrameDuration) {
                gearFrameIndex = (gearFrameIndex + 1) % gearFrameCount;
                lastGearFrameTime = now;
            }
            int sx = gearFrameIndex * gearFrameWidth;
            gc.drawImage(gearSpriteSheet,
                         sx, 0, gearFrameWidth, gearFrameHeight,
                         0, 0, gearFrameWidth, gearFrameHeight);
        }

        // 1) Décorations
        for (int i = 0; i < decorationImages.size(); i++) {
            Image img = decorationImages.get(i);
            Double[] pos = decorationPositions.get(i);
            double dx = pos[0] - cameraX.get();
            double dy = pos[1] - cameraY.get();
            double dw = pos[2], dh = pos[3];
            if (img != null) {
                gc.drawImage(img, dx, dy, dw, dh);
            }
        }

        // 2) Plateformes
        for (int i = 0; i < platformImages.size(); i++) {
            Image img = platformImages.get(i);
            Double[] pos = platformPositions.get(i);
            double px = pos[0] - cameraX.get();
            double py = pos[1] - cameraY.get();
            double pw = pos[2], ph = pos[3];
            if (img != null) {
                gc.drawImage(img, px, py, pw, ph);
            } else {
                gc.setFill(Color.BLUE);
                gc.fillRect(px, py, pw, ph);
            }
        }

        // 3) JOUEUR ou VAISSEAU
        double drawX = playerX - cameraX.get();
        double drawY = playerY - cameraY.get() - playerOffsetY;
        if (spaceshipMode && spaceshipImage != null) {
            double imageW = spaceshipImage.getWidth();
            double imageH = spaceshipImage.getHeight();
            double targetH = playerH * 2.0;
            double targetW = targetH * (imageW / imageH);

            if (facingRight) {
                gc.drawImage(spaceshipImage,
                             drawX, drawY,
                             targetW, targetH);
            } else {
                gc.save();
                gc.translate(drawX + targetW, drawY);
                gc.scale(-1, 1);
                gc.drawImage(spaceshipImage,
                             0, 0,
                             targetW, targetH);
                gc.restore();
            }
        } else if (isJumping && playerJumpSheet != null && jumpFrameWidth > 0) {
            long now = System.nanoTime();
            if (now - lastJumpFrameTime >= jumpFrameDuration) {
                jumpFrameIndex = (jumpFrameIndex + 1) % jumpFrameCount;
                lastJumpFrameTime = now;
            }
            int sx = jumpFrameIndex * jumpFrameWidth;
            if (facingRight) {
                gc.drawImage(playerJumpSheet,
                             sx, 0, jumpFrameWidth, jumpFrameHeight,
                             drawX, drawY, playerW * 2, playerH * 2);
            } else {
                gc.save();
                gc.translate(drawX + playerW * 2, drawY);
                gc.scale(-1, 1);
                gc.drawImage(playerJumpSheet,
                             sx, 0, jumpFrameWidth, jumpFrameHeight,
                             0, 0, playerW * 2, playerH * 2);
                gc.restore();
            }
        } else if (isWalking && playerWalkSheet != null && walkFrameWidth > 0) {
            long now = System.nanoTime();
            if (now - lastWalkFrameTime >= walkFrameDuration) {
                walkFrameIndex = (walkFrameIndex + 1) % walkFrameCount;
                lastWalkFrameTime = now;
            }
            int sx = walkFrameIndex * walkFrameWidth;
            if (facingRight) {
                gc.drawImage(playerWalkSheet,
                             sx, 0, walkFrameWidth, walkFrameHeight,
                             drawX, drawY, playerW * 2, playerH * 2);
            } else {
                gc.save();
                gc.translate(drawX + playerW * 2, drawY);
                gc.scale(-1, 1);
                gc.drawImage(playerWalkSheet,
                             sx, 0, walkFrameWidth, walkFrameHeight,
                             0, 0, playerW * 2, playerH * 2);
                gc.restore();
            }
        } else if (playerIdleSheet != null && idleFrameWidth > 0) {
            long now = System.nanoTime();
            if (now - lastIdleFrameTime >= idleFrameDuration) {
                idleFrameIndex = (idleFrameIndex + 1) % idleFrameCount;
                lastIdleFrameTime = now;
            }
            int sx = idleFrameIndex * idleFrameWidth;
            if (facingRight) {
                gc.drawImage(playerIdleSheet,
                             sx, 0, idleFrameWidth, idleFrameHeight,
                             drawX, drawY, playerW * 2, playerH * 2);
            } else {
                gc.save();
                gc.translate(drawX + playerW * 2, drawY);
                gc.scale(-1, 1);
                gc.drawImage(playerIdleSheet,
                             sx, 0, idleFrameWidth, idleFrameHeight,
                             0, 0, playerW * 2, playerH * 2);
                gc.restore();
            }
        }

        
        // 4) Ennemis + boss
        for (Enemy e : enemies) {
            double ex = e.getX() - cameraX.get();
            double ey = e.getY() - cameraY.get();
            double ew = e.getWidth(), eh = e.getHeight();
            Image img = e.getTexture();
            if (e instanceof Boss && ((Boss) e).isHit()) {
                ImageView temp = new ImageView(img);
                Blend blend = new Blend(
                    BlendMode.HARD_LIGHT,
                    null,
                    new ColorInput(0, 0, e.getWidth(), e.getHeight(), Color.color(1.0, 0.0, 0.0, 0.7))
                );
                temp.setEffect(blend);
                WritableImage writableImage = new WritableImage((int) img.getWidth(), (int) img.getHeight());
                temp.snapshot(null, writableImage);
                gc.drawImage(writableImage, ex, ey, ew, eh);
            } else {
                gc.drawImage(img, ex, ey, ew, eh);
            }
        }

        // 5) Projectiles (mode vaisseau)
        if (spaceshipMode) {
            gc.setFill(Color.RED);
            for (Double[] pos : projectilePositions) {
                double px = pos[0] - cameraX.get();
                double py = pos[1] - cameraY.get();
                double pw = pos[2], ph = pos[3];
                gc.fillRect(px, py, pw, ph);
            }
        }
    }

    public DoubleProperty cameraXProperty() { return cameraX; }
    public DoubleProperty cameraYProperty() { return cameraY; }
    public double getCanvasWidth()        { return gc.getCanvas().getWidth(); }
    public double getCanvasHeight()       { return gc.getCanvas().getHeight(); }

    public void resetBackgroundCache() {
        this.cachedBackground = null;
        this.cachedWidth  = -1;
        this.cachedHeight = -1;
    }

    public Scene getScene(){
        return this.scene;
    }
}
