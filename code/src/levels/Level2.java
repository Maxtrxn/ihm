package src.levels;

import src.model.Player;
import src.model.SteelPlatform;
import src.model.Enemy;

public class Level2 extends Level {

    public Level2(Player player) {
        super(player);
    }

    @Override
    protected void initialize() {
        platforms.add(new SteelPlatform(100, 300));
        platforms.add(new SteelPlatform(400, 250));
        platforms.add(new SteelPlatform(700, 300));
        platforms.add(new SteelPlatform(0, 580));
        platforms.add(new SteelPlatform(200, 350));

        enemies.add(new Enemy(450, 230, 50, 50, 2, 400, 600));
        enemies.add(new Enemy(800, 230, 50, 50, 2, 700, 900));
    }
}