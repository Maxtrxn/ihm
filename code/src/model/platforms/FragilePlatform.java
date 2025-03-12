package src.model.platforms;

import javafx.scene.image.Image;
import src.model.Platform;
import src.model.Player;

public class FragilePlatform extends Platform {
    private int steps = 0;
    private static final int MAX_STEPS = 2;
    private Image fragileTexture;
    private boolean playerWasOn = false;

    public FragilePlatform(double x, double y) {
        super(x, y, new Image("file:../textures/plateforme cuivre.png"));
        this.fragileTexture = new Image("file:../textures/plateforme_fragile.png");
    }

    public boolean isBroken() {
        return steps >= MAX_STEPS;
    }

    public void step(Player player) {
        if (!playerWasOn) {  // On applique l'effet seulement une fois par atterrissage
            steps++;
            if (steps == 1) {
                this.texture = fragileTexture;
            }
            playerWasOn = true; // On marque que le joueur est dessus
            System.out.println("Steps: " + steps + " / " + MAX_STEPS); // Pour le débogage
        }
    }

    public void resetStep(Player player) {
        if (!player.intersects(this)) { // Vérifie si le joueur est encore sur la plateforme
            playerWasOn = false;
        }
    }
}
