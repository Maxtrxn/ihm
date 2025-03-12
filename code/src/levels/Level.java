package src.levels;

import src.model.Player;
import src.model.Platform;
import src.model.Enemy;
import java.util.List;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.image.Image;


import java.util.ArrayList;

public abstract class Level {
    protected List<Platform> platforms;
    protected List<Enemy> enemies;
    protected Player player;
    protected BackgroundImage backgroundImage;

    public Level(Player player) {
        this.player = player;
        this.platforms = new ArrayList<>();
        this.enemies = new ArrayList<>();
        initialize();
    }

    protected abstract void initialize();

    public List<Platform> getPlatforms() {
        return platforms;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public BackgroundImage getBackgroundImage() {
        return backgroundImage;
    }

    //Premier repeat : horizontal, Deuxième : Vertical
    protected void setBackgroundImage(Image background) {
        this.backgroundImage = new BackgroundImage(background, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
    }
}