package src.model.game.platforms;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import src.common.ResourcesPaths;
import src.model.game.Platform;
import src.model.game.Player;
import org.json.JSONObject;

public class FragilePlatform extends Platform {
    private int steps = 0;
    private static final int MAX_STEPS = 2;
    private Image fragileTexture;
    private boolean playerWasOn = false;

    public FragilePlatform(double x, double y, String name){
        super(x, y, name);

        this.fragileTexture = new Image("file:" + ResourcesPaths.PLATFORMS_FOLDER + "fragile_overlay.png");


        //Créer une copie modifiable de la texture de base pour pouvoir superposer l'image
        //"fragile" par dessus.
        WritableImage resultImage = new WritableImage((int) this.texture.getWidth(), (int) this.texture.getHeight());
        PixelReader baseReader = this.texture.getPixelReader();
        PixelReader overlayReader = this.fragileTexture.getPixelReader();
        PixelWriter writer = resultImage.getPixelWriter();

        for (int y_ = 0; y_ < this.texture.getHeight(); y_++) {
            for (int x_ = 0; x_ < this.texture.getWidth(); x_++) {
                //Lire les couleurs
                javafx.scene.paint.Color baseColor = baseReader.getColor(x_, y_);
                //On répète l'image "fragile" si l'image de base était plus grande grâce au modulo
                javafx.scene.paint.Color overlayColor = overlayReader.getColor((int)(x_%this.fragileTexture.getWidth()), (int)(y_%this.fragileTexture.getHeight()));

                //Mélanger les couleurs (simple alpha blending)
                double alpha = overlayColor.getOpacity();
                javafx.scene.paint.Color blendedColor = baseColor.interpolate(overlayColor, alpha);

                //Ecrire dans l'image résultante
                writer.setColor(x_, y_, blendedColor);
            }
        }

        this.fragileTexture = resultImage;
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
