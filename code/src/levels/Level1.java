package src.levels;

import javafx.scene.image.Image;
import src.model.Player;
import src.model.Platform;
import src.model.Enemy;

public class Level1 extends Level {
    private Image platformTexture;

    public Level1(Player player) {
        super(player);
        this.platformTexture = new Image("file:../textures/plateforme_acier_1.png");
    }

    @Override
    protected void initialize() {
        platforms.add(new Platform(200, 400, 200, 20, platformTexture));
        platforms.add(new Platform(600, 350, 200, 20, platformTexture));
        platforms.add(new Platform(1000, 400, 200, 20, platformTexture));
        platforms.add(new Platform(0, 580, 1600, 20, platformTexture));
        platforms.add(new Platform(150, 450, 100, 20, platformTexture));

        enemies.add(new Enemy(650, 330, 50, 50, 2, 600, 800));
        enemies.add(new Enemy(1200, 330, 50, 50, 2, 1100, 1300));
    }
}