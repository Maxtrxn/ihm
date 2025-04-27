package src.model.game.platforms;

import javafx.scene.image.Image;
import src.common.ResourcesPaths;
import src.model.game.Platform;
import src.model.game.Player;
import org.json.JSONObject;

public class FragilePlatform extends Platform {
    private int steps = 0;
    private static final int MAX_STEPS = 2;
    private boolean playerWasOn = false;

    // Texture « fissurée »
    private final Image crackedTexture;
    private final double scaleFactor;

    public FragilePlatform(double x, double y) {
        // Charge la texture normale depuis platformsJson sous "fragile"
        super(x, y, "fragile");

        JSONObject json = Platform.platformsJson.getJSONObject("fragile");
        this.scaleFactor   = json.getDouble("scaleFactor");
        String crackedFile = json.getString("crackedTextureFileName");
        this.crackedTexture = new Image("file:" + ResourcesPaths.PLATFORMS_FOLDER + crackedFile);
    }

    /**
     * Appelée quand le joueur atterrit sur la plateforme.
     * - 1er atterrissage : on passe à la texture fissurée.
     * - 2e atterrissage : la plateforme sera considérée comme « cassée ».
     */
    public void step(Player player) {
        if (!playerWasOn) {
            steps++;
            if (steps == 1) {
                // applique la texture fissurée
                this.texture = crackedTexture;
                this.width   = crackedTexture.getWidth()  * scaleFactor;
                this.height  = crackedTexture.getHeight() * scaleFactor;
            }
            playerWasOn = true;
        }
    }

    /** Remet playerWasOn à false dès que le joueur n'est plus sur la plateforme. */
    public void resetStep(Player player) {
        if (!player.intersects(this)) {
            playerWasOn = false;
        }
    }

    /** True si la plateforme doit être retirée (après 2 atterrissages). */
    public boolean isBroken() {
        return steps >= MAX_STEPS;
    }
}
