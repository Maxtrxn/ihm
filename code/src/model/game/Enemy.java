package src.model.game;


import src.common.JsonReader;
import src.common.ResourcesPaths;

import org.json.JSONObject;

import javafx.scene.image.Image;


public class Enemy {
    protected static JSONObject enemiesJson = JsonReader.getJsonObjectContent(ResourcesPaths.RESOURCE_FOLDER + "enemies.json");

    private double x, y, width, height, speed;
    private String name;
    protected Image texture = null;
    private double leftBound, rightBound;
    private boolean movingRight = true;


    public Enemy(double x, double y, double speed, double leftBound, double rightBound, String name) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.leftBound = leftBound;
        this.rightBound = rightBound;

        JSONObject enemyJson = enemiesJson.getJSONObject(name);
        this.texture = new Image("file:" + ResourcesPaths.ENEMIES_FOLDER + enemyJson.getString("textureFileName"));
        double scaleFactor = enemyJson.getDouble("scaleFactor");
        this.width = texture.getWidth() * scaleFactor;
        this.height = texture.getHeight() * scaleFactor;
    }


    public Enemy(double x, double y, double width, double height, double speed, double leftBound, double rightBound) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed * 60;
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


    public JSONObject toJSONObject(){
        JSONObject enemyJSON = new JSONObject();
        enemyJSON.put("name", this.name);
        enemyJSON.put("x", this.x);
        enemyJSON.put("y", this.y);
        enemyJSON.put("patrolStart", this.leftBound);
        enemyJSON.put("patrolEnd", this.rightBound);
        enemyJSON.put("speed", this.speed);

        return enemyJSON;
    }
}