package src.levels;

import javafx.scene.image.Image;
import src.model.Player;
import src.model.Decoration;
import src.model.Enemy;
import src.model.platforms.*;

public class Level4 extends Level {

    public Level4(Player player) {
        super(player);
    }

    @Override
    protected void initialize() {
        // Dimensions logiques du niveau
        double levelWidth = 3000;
        double levelHeight = 800;
        setLevelDimensions(levelWidth, levelHeight);
        
        // Background industriel
        setBackgroundImage("file:../textures/background_level3.png");

        // Sol en acier sur toute la largeur
        Image groundTex = new Image("file:../textures/plateforme_acier_1.png");
        double tileW = groundTex.getWidth();
        double tileH = groundTex.getHeight();
        double groundY = levelHeight - tileH;
        
        for (double x = 0; x < levelWidth; x += tileW) {
            platforms.add(new GroundPlatform(x, groundY, groundTex));
        }

        // Section 1: Plateformes fragiles en escalier
        double y = groundY - 120;
        for (int i = 0; i < 4; i++) {
            platforms.add(new FragilePlatform(200 + i*100, y - i*60));
        }

        // Section 2: Zone de plateformes en cuivre avec ennemis
        platforms.add(new CopperPlatform(600, groundY - 200));
        platforms.add(new CopperPlatform(800, groundY - 200));
        platforms.add(new CopperPlatform(1000, groundY - 200));
        enemies.add(new Enemy(700, groundY - 250, 50, 50, 2, 600, 1000));

        // Section 3: Tour de briques avec ennemis
        double towerX = 1300;
        for (int i = 0; i < 5; i++) {
            platforms.add(new BrickPlatform(towerX, groundY - 100 - i*100));
        }
        enemies.add(new Enemy(towerX, groundY - 550, 50, 50, 3, towerX - 100, towerX + 100));

        // Section 4: Plateformes en acier espacées
        platforms.add(new SteelPlatform(1600, groundY - 150));
        platforms.add(new SteelPlatform(1800, groundY - 250));
        platforms.add(new SteelPlatform(2000, groundY - 350));

        // Section finale: Combinaison complexe
        platforms.add(new FragilePlatform(2200, groundY - 200));
        platforms.add(new CopperPlatform(2400, groundY - 300));
        platforms.add(new BrickPlatform(2600, groundY - 400));
        enemies.add(new Enemy(2400, groundY - 350, 50, 50, 2.5, 2200, 2600));

        // Décorations
        Image lamp = new Image("file:../textures/lampadaire 1.png");
        double lampH = lamp.getHeight();
        decorations.add(new Decoration(600, groundY - lampH, lamp));
        decorations.add(new Decoration(1300, groundY - lampH, lamp));
        decorations.add(new Decoration(2000, groundY - lampH, lamp));
    }
}