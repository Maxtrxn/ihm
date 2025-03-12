package src.levels;

import src.model.Player;
import src.model.platforms.CopperPlatform;
import src.model.platforms.FragilePlatform;
import src.model.Enemy;

public class Level2 extends Level {

    public Level2(Player player) {
        super(player);
    }

    @Override
    protected void initialize() {
        setBackgroundImage("file:../textures/background_level2.png");

        platforms.add(new FragilePlatform(100, 300)); // Première plateforme cassée
        platforms.add(new CopperPlatform(400, 250));
        platforms.add(new CopperPlatform(700, 300));
        platforms.add(new CopperPlatform(0, 580));
        platforms.add(new CopperPlatform(200, 350));

        enemies.add(new Enemy(450, 230, 50, 50, 2, 400, 600));
        enemies.add(new Enemy(800, 230, 50, 50, 2, 700, 900));
    }
}