package src.levels;

import javafx.scene.image.Image;

import src.model.Player;
import src.model.Decoration;
import src.model.platforms.GroundPlatform;
import src.model.platforms.BrickPlatform;

public class Level3 extends Level {

    public Level3(Player player) {
        super(player);
    }

    @Override
    protected void initialize() {
        // Arrière-plan principal
        setBackgroundImage("file:../textures/background_level3.png");

        // 1) Sol en briques sur toute la largeur
        double levelWidth = 3000;
        Image groundTex = new Image("file:../textures/sol brique.png");
        double tileW = groundTex.getWidth();
        double groundY = 580;
        for (double x = 0; x < levelWidth; x += tileW) {
            platforms.add(new GroundPlatform(x, groundY, groundTex));
        }

        // 2) Plateformes en briques
        platforms.add(new BrickPlatform(200, 400));
        platforms.add(new BrickPlatform(600, 350));
        platforms.add(new BrickPlatform(1000, 300));

        // 3) Décoration : un lampadaire dans le décor
        /*
        decorations.add(new Decoration(
            500, 300,
            new Image("file:../textures/lampadaire 1.png")
        ));
        */
       Image lamp = new Image("file:../textures/lampadaire 1.png");
        System.out.println("Lampadaire : erreur=" + lamp.isError()
    + ", w=" + lamp.getWidth() + ", h=" + lamp.getHeight());
        decorations.add(new Decoration(500, 300, lamp));

    }
}
