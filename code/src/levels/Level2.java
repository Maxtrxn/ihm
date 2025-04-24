package src.levels;

import src.model.Player;
import src.model.platforms.CopperPlatform;
import src.model.platforms.FragilePlatform;
import javafx.scene.image.Image;
import src.model.platforms.GroundPlatform;
import src.model.Enemy;

public class Level2 extends Level {

    public Level2(Player player) {
        super(player);
    }

    @Override
    protected void initialize() {
        setBackgroundImage("file:../textures/background_level2.png");

        double levelWidth = 3000;
        Image groundTex = new Image("file:../textures/sol brique.png");
        double tileW = groundTex.getWidth();
        double tileH = groundTex.getHeight();
        double groundY = 600 - tileH;
        for (double x = 0; x < levelWidth; x += tileW) {
            platforms.add(new GroundPlatform(x, groundY, groundTex));
        }

        platforms.add(new FragilePlatform(100, groundY - 300)); // Première plateforme cassée
        platforms.add(new CopperPlatform(400, groundY - 250));
        platforms.add(new CopperPlatform(700, groundY - 300));
        platforms.add(new CopperPlatform(200, groundY - 350));
        platforms.add(new CopperPlatform(400, groundY - 350));

        // Ennemis
        enemies.add(new Enemy(450, 230, 50, 50, 2, 400, 600));
        enemies.add(new Enemy(800, 230, 50, 50, 2, 700, 900));
    }
}
