package src.levels;

import src.model.Player;
import src.model.platforms.SteelPlatform;
import src.model.Enemy;

public class Level1 extends Level {

    public Level1(Player player) {
        super(player);
    }

    @Override
    protected void initialize() {
        setBackgroundImage("file:../textures/background temporaire1.png");

        platforms.add(new SteelPlatform(200, 400));
        platforms.add(new SteelPlatform(600, 350));
        platforms.add(new SteelPlatform(1000, 400));
        platforms.add(new SteelPlatform(0, 580));
        platforms.add(new SteelPlatform(150, 450));

        enemies.add(new Enemy(650, 330, 50, 50, 2, 600, 800));
        enemies.add(new Enemy(1200, 330, 50, 50, 2, 1100, 1300));
    }
}