package src.levels;

import javafx.scene.image.Image;
import src.model.Player;
import src.model.Platform;
import src.model.platforms.SteelPlatform;
import src.model.platforms.GroundPlatform;
import src.model.Enemy;

public class Level1 extends Level {

    public Level1(Player player) {
        super(player);
    }

    @Override
    protected void initialize() {
        // Arrière-plan
        setBackgroundImage("file:../textures/background temporaire1.png");

        // ----------------------------------------------
        // 1) Ajouter un sol répété sur la largeur du niveau
        // ----------------------------------------------
        double levelWidth = 3000;    // Largeur totale du niveau
        double tileWidth  = 128;     // Largeur d'une tuile de sol
        double floorY     = 580;     // Coordonnée Y du sol (bas ~600)

        for (double x = 0; x < levelWidth; x += tileWidth) {
            platforms.add(new GroundPlatform(
                x,
                floorY,
                new Image("file:../textures/sol.png") // La texture du sol
            ));
        }

        // ----------------------------------------------
        // 2) Tes autres plateformes en acier
        // ----------------------------------------------
        platforms.add(new SteelPlatform(200, 400));
        platforms.add(new SteelPlatform(600, 350));
        platforms.add(new SteelPlatform(1000, 400));
        platforms.add(new SteelPlatform(0, 580));
        platforms.add(new SteelPlatform(150, 450));

        // ----------------------------------------------
        // 3) Ennemis
        // ----------------------------------------------
        enemies.add(new Enemy(650, 330, 50, 50, 2*60, 600, 800));
        enemies.add(new Enemy(1200, 330, 50, 50, 2*60, 1100, 1300));
    }
}
