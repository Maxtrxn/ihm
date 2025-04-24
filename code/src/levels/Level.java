package src.levels;

import src.model.Player;
import src.model.Platform;
import src.model.Enemy;
import src.model.Decoration;

import javafx.scene.image.Image;
import java.util.List;
import java.util.ArrayList;

public abstract class Level {
    protected List<Platform> platforms;
    protected List<Enemy> enemies;
    protected List<Decoration> decorations;
    protected Player player;
    protected Image backgroundImage;

    public Level(Player player) {
        this.player      = player;
        this.platforms   = new ArrayList<>();
        this.enemies     = new ArrayList<>();
        this.decorations = new ArrayList<>();
        initialize();
    }

    protected abstract void initialize();

    public List<Platform> getPlatforms() {
        return platforms;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Decoration> getDecorations() {
        return decorations;
    }

    public Image getBackgroundImage() {
        return backgroundImage;
    }

    protected void setBackgroundImage(String imagePath) {
        this.backgroundImage = new Image(imagePath);
    }
}
