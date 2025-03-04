package src.model;

import javafx.scene.image.Image;

public class Platform {
    private double x, y, width, height;
    private Image texture;

    public Platform(double x, double y, double width, double height, Image texture) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.texture = texture;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Image getTexture() {
        return texture;
    }
}