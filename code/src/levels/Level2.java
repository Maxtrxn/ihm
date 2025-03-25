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

        // Plateformes existantes
        platforms.add(new FragilePlatform(100, 300)); // Première plateforme cassée
        platforms.add(new CopperPlatform(400, 250));
        platforms.add(new CopperPlatform(700, 300));
        platforms.add(new CopperPlatform(200, 350));
        platforms.add(new CopperPlatform(400, 350));

        // Ajout d'un sol dur sur toute la largeur du niveau
        // Supposons que la largeur logique du niveau est de 3000 pixels
        // et que chaque CopperPlatform a une largeur par défaut de 96 pixels.
        int levelWidth = 3000;
        int groundY = 580;
        int platformWidth = 96; // ajustez si besoin selon la définition de CopperPlatform
        for (int x = 0; x < levelWidth; x += platformWidth) {
            platforms.add(new CopperPlatform(x, groundY));
        }

        // Ennemis
        enemies.add(new Enemy(450, 230, 50, 50, 2, 400, 600));
        enemies.add(new Enemy(800, 230, 50, 50, 2, 700, 900));
    }
}
