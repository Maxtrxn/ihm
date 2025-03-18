package src.model;

import javafx.scene.image.Image;

public abstract class Platform {
    protected double x, y, width, height;
    protected Image texture;

    public Platform(double x, double y, Image texture) {
        this.x = x;
        this.y = y;
        this.texture = texture;

        // Dimensions initiales basées sur la taille d'origine de la texture
        this.width = texture.getWidth();
        this.height = texture.getHeight();
    }

    public double getX()      { return x; }
    public double getY()      { return y; }
    public double getWidth()  { return width; }
    public double getHeight() { return height; }
    public Image getTexture() { return texture; }
}
