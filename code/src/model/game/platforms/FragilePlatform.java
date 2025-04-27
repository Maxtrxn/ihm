package src.model.game.platforms;

import javafx.scene.image.Image;
import src.common.ResourcesPaths;
import src.model.game.Platform;
import src.model.game.Player;
import org.json.JSONObject;

public class FragilePlatform extends Platform {
    private int steps = 0;
    private static final int MAX_STEPS = 2;
    private Image fragileTexture;
    private boolean playerWasOn = false;

    public FragilePlatform(double x, double y) {
        super(x, y, "cuivre");

        JSONObject platformJson = Platform.platformsJson.getJSONObject("fragile");
        this.fragileTexture = new Image("file:" + ResourcesPaths.PLATFORMS_FOLDER + platformJson.getString("textureFileName"));
    }

    /**
     * Retourne true si la plateforme est brisée (après 2 atterrissages).
     */
    public boolean isBroken() {
        return steps >= MAX_STEPS;
    }

    /**
     * Appelée lorsque le joueur atterrit sur la plateforme.
     * - 1er atterrissage => change la texture en "fissurée"
     * - 2e atterrissage => steps = 2 => isBroken() = true => la plateforme sera retirée.
     */
    public void step(Player player) {
        // On incrémente steps UNE SEULE FOIS par "atterrissage"
        if (!playerWasOn) {
            steps++;
            if (steps == 1) {
                this.texture = fragileTexture; // devient fissurée
            }
            playerWasOn = true;
            System.out.println("Steps: " + steps + " / " + MAX_STEPS);
        }
    }

    /**
     * Remet playerWasOn à false dès que le joueur n'est plus sur la plateforme.
     * Ainsi, la prochaine fois qu'il atterrit, on incrémente steps à nouveau.
     */
    public void resetStep(Player player) {
        if (!player.intersects(this)) {
            playerWasOn = false;
        }
    }
}
