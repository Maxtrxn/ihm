package src.levels;

import src.model.Player;
import src.model.Platform;
import src.model.Enemy;

public class Level2 extends Level {

    public Level2(Player player) {
        super(player);
    }

    @Override
    protected void initialize() {
        platforms.add(new Platform(100, 300, 200, 20));
        platforms.add(new Platform(400, 250, 200, 20));
        platforms.add(new Platform(700, 300, 200, 20));
        platforms.add(new Platform(0, 580, 1600, 20));
        platforms.add(new Platform(200, 350, 100, 20));

        enemies.add(new Enemy(450, 230, 50, 50, 2, 400, 600));
        enemies.add(new Enemy(800, 230, 50, 50, 2, 700, 900));
    }
}