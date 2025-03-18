package src.model.platforms;

import javafx.scene.image.Image;
import src.model.Platform;

public class SteelPlatform extends Platform {

    public SteelPlatform(double x, double y) {
        super(x, y, new Image("file:../textures/plateforme_acier_1.png"));

        if (this.texture.isError()) {
            System.err.println("Error loading SteelPlatform texture.");
        }

        // Facteur de réduction (ajuste si tu veux plus petit/grand)
        double scaleFactor = 0.3;

        // On réduit la bounding box (collision + affichage) à 30% de la taille initiale
        this.width  = this.width  * scaleFactor;
        this.height = this.height * scaleFactor;

        // On ne modifie pas this.y, donc le "haut" reste à y
        // (Si tu veux garder le bas au même endroit, voir plus bas *)

        System.out.println("[SteelPlatform] new bounding box => x=" + this.x
            + ", y=" + this.y + ", w=" + this.width + ", h=" + this.height);
    }
}
