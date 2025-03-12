package src.model.platforms;

import javafx.scene.image.Image;
import src.model.Platform;

public class CopperPlatform extends Platform {

    public CopperPlatform(double x, double y) {
        super(x, y, new Image("file:../textures/plateforme cuivre.png"));
    }
}