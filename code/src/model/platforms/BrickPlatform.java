package src.model.platforms;

import javafx.scene.image.Image;
import src.model.Platform;

public class BrickPlatform extends Platform {

    public BrickPlatform(double x, double y) {
        super(x, y, new Image("file:../textures/plateforme brique.png"));
    }
}
