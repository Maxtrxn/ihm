package src.model.game;

import src.common.JsonReader;
import src.common.ResourceManager;

import org.json.JSONObject;

import javafx.scene.image.Image;


public class Enemy extends LevelObject{
    private double speed;
    private double leftBound, rightBound;
    private boolean movingRight = true;


    public Enemy(double x, double y, double speed, double leftBound, double rightBound, String name) {
        super(x, y, name, ResourceManager.ENEMIES_JSON, ResourceManager.ENEMIES_FOLDER);
        this.speed = speed;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
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
        return this.texture;
    }

    /** @param deltaSec  temps écoulé (s) */
    /**
     * @param deltaSec  temps écoulé (s) depuis la dernière frame
     */
    public void update(double deltaSec) {
        double move = speed * deltaSec;
        if (movingRight) {
            x += move;
            if (x >= rightBound) movingRight = false;
        } else {
            x -= move;
            if (x <= leftBound) movingRight = true;
        }
    }

    @Override
    public JSONObject toJSONObject(){
        JSONObject enemyJSON = super.toJSONObject();
        enemyJSON.put("patrolStart", this.leftBound);
        enemyJSON.put("patrolEnd", this.rightBound);
        enemyJSON.put("speed", this.speed);

        return enemyJSON;
    }

    // --- Nouveaux accesseurs pour permettre à Boss de changer y ---
    /** Position horizontale de l'ennemi. */
    public void setX(double x) {
        this.x = x;
    }

    /** Position verticale de l'ennemi. */
    public void setY(double y) {
        this.y = y;
    }

    public double getSpeed() { 
        return speed; 
    }
    public double getLeftBound() { 
        return leftBound; 
    }
    public double getRightBound() { 
        return rightBound; 
    }
}
