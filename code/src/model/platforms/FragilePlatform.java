package src.model.platforms;

import javafx.scene.image.Image;
import src.model.Platform;

public class FragilePlatform extends Platform {
    private int steps = 0;
    private static final int MAX_STEPS = 2;
    private Image initialTexture;
    private Image fragileTexture;

    public FragilePlatform(double x, double y) {
        super(x, y, new Image("file:../textures/plateforme cuivre.png"));
        this.initialTexture = new Image("file:../textures/plateforme cuivre.png");
        this.fragileTexture = new Image("file:../textures/plateforme_fragile.png");
    }

    public boolean isBroken() {
        return steps >= MAX_STEPS;
    }

    public void step() {
        steps++;
        if (steps == 1) {
            this.texture = fragileTexture;
        }
    }
}