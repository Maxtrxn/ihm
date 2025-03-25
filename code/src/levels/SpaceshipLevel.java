package src.levels;

import src.model.Player;
import java.util.ArrayList;

public class SpaceshipLevel extends Level {

    public SpaceshipLevel(Player player) {
        super(player);
    }

    @Override
    protected void initialize() {
        // Pas de background (fond noir)
        // setBackgroundImage("file:../textures/black_background.png"); // si vous le souhaitez

        // Aucune plateforme ni ennemi
        this.platforms = new ArrayList<>();
        this.enemies   = new ArrayList<>();
    }
}
