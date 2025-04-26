package src.levels;

import javafx.scene.image.Image;
import src.model.Player;
import src.model.Platform;
import src.model.Decoration;
import src.model.platforms.SteelPlatform;
import src.model.platforms.GroundPlatform;
import src.model.platforms.BrickPlatform;
import src.model.Enemy;

public class Level1 extends Level {

    public Level1(Player player) {
        super(player, "level1");
    }

    @Override
    protected void initialize() {
        super.initialize();
        
        if (true){
            return;
        }
        

        // Arrière-plan
        setBackgroundImage("file:../textures/background temporaire1.png");

        // ----------------------------------------------
        // 1) Ajouter un sol répété sur la largeur du niveau
        // ----------------------------------------------
        double levelWidth = 3000;    // Largeur totale du niveau
        Image groundTex = new Image("file:../textures/sol.png");
        double tileW = groundTex.getWidth();
        double tileH = groundTex.getHeight();
        double groundY = 600 - tileH;
        for (double x = 0; x < levelWidth; x += tileW) {
            platforms.add(new GroundPlatform(x, groundY, groundTex));
        }

        // ----------------------------------------------
        // 2) Tes autres plateformes en acier
        // ----------------------------------------------
        platforms.add(new SteelPlatform(200, groundY - 100));
        platforms.add(new SteelPlatform(1000, groundY - 100));
        platforms.add(new SteelPlatform(1000, 400));
        platforms.add(new SteelPlatform(0, groundY - 300));
        platforms.add(new SteelPlatform(500, groundY - 450));

        // ----------------------------------------------
        // 3) Ennemis
        // ----------------------------------------------
        enemies.add(new Enemy(650, groundY - 300, 50, 50, 2, 600, 800));
        enemies.add(new Enemy(1200, groundY - 50, 50, 50, 2, 1100, 1300));

        // 4) Décoration : un lampadaire dans le décor
        Image lamp = new Image("file:../textures/lampadaire 1.png");
        double lampH = lamp.getHeight();
        decorations.add(new Decoration(1200, groundY - lampH - 300, lamp));
    }

}