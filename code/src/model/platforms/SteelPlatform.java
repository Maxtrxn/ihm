package src.model.platforms;

import javafx.scene.image.Image;
import src.model.Platform;

public class SteelPlatform extends Platform {
    
    public SteelPlatform(double x, double y) {
        super(x, y, new Image("file:../textures/plateforme_acier_1.png"));
        if (this.texture.isError()) {
            System.out.println("Error loading SteelPlatform texture.");
        }
    }
}