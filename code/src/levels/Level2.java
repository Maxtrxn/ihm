package src.levels;

import javafx.scene.image.Image;
import src.model.Player;
import src.model.Platform;
import src.model.Enemy;

public class Level2 extends Level {
    private Image platformTexture;

    public Level2(Player player) {
        super(player);
        this.platformTexture = new Image("file:/home/matthieu/Documents/projet_steampunk/04_03_2024/ihm-master/textures/plateforme_acier_1.png");
    }

    @Override
    protected void initialize() {
        platforms.add(new Platform(100, 300, 200, 20, platformTexture));
        platforms.add(new Platform(400, 250, 200, 20, platformTexture));
        platforms.add(new Platform(700, 300, 200, 20, platformTexture));
        platforms.add(new Platform(0, 580, 1600, 20, platformTexture));
        platforms.add(new Platform(200, 350, 100, 20, platformTexture));

        enemies.add(new Enemy(450, 230, 50, 50, 2, 400, 600));
        enemies.add(new Enemy(800, 230, 50, 50, 2, 700, 900));
    }
}