package src.model;

import javafx.scene.image.Image;

public class Decoration {
    private double x, y, width, height;
    private Image texture;

    public Decoration(double x, double y, Image texture) {
        this.x = x;
        this.y = y;
        this.texture = texture;
        this.width = texture.getWidth();
        this.height = texture.getHeight();
    }

    public double getX()      { return x; }
    public double getY()      { return y; }
    public double getWidth()  { return width; }
    public double getHeight() { return height; }
    public Image getTexture() { return texture; }
}
