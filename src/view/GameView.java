package src.view;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
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

    public GameView(GraphicsContext gc) {
        this.gc = gc;
    }

    public void draw(Player player, List<Platform> platforms, List<Enemy> enemies) {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        gc.setFill(Color.RED);
        gc.fillRect(player.getX() - cameraX.get(), player.getY() - cameraY.get(), player.getWidth(), player.getHeight());

        gc.setFill(Color.BLUE);
        for (Platform platform : platforms) {
            gc.fillRect(platform.getX() - cameraX.get(), platform.getY() - cameraY.get(), platform.getWidth(), platform.getHeight());
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