package src.controller.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import src.model.game.Boss;
import src.model.game.Decoration;
import src.model.game.Enemy;
import src.model.game.GameModel;
import src.model.game.Level;
import src.model.game.Platform;
import src.model.game.Player;
import src.model.game.Projectile;
import src.view.game.GameView;

public class GameController {
    private static final double GRAVITY           = 1800.0;
    private static final double SHIP_SCROLL_SPEED = 200.0; // px/s

    // Flags de déplacement
    private boolean left, right, up, down, jumping, jetpack;

    private Player           player;
    private List<Platform>   platforms;
    private List<Enemy>      enemies;
    private List<Decoration> decorations;
    private final List<Projectile> projectiles = new ArrayList<>();
    private final GameView         view;
    private final GameModel        model;
    private Level            level;

    private double initialPlayerX, initialPlayerY;
    private Timer jetpackTimer;

    // Caméra
    private double cameraX = 0.0, cameraY = 0.0;

    // Boss fight lock
    private boolean inBossFight = false;

    // Boucle JavaFX
    private AnimationTimer gameLoop;

    public GameController(Stage primaryStage) {
        this.view            = new GameView(this, primaryStage);
        this.model           = new GameModel(this);
    }

    public void setPlayer(Player player){
        this.player = player;
        this.initialPlayerX  = player.getX();
        this.initialPlayerY  = player.getY();
    }
    public void setLevel(Level level){
        this.level = level;
        this.platforms = level.getPlatforms();
        this.enemies = level.getEnemies();
        this.decorations = level.getDecorations();
    }


    /** Lie les touches aux flags, et gère tirs vs saut. */
    public void handleInput() {
        Scene scene = this.view.getScene();
        scene.setOnKeyPressed(e -> {
            boolean isShip = level.getPlatforms().isEmpty() && level.getEnemies().isEmpty();

            if (e.getCode() == KeyCode.LEFT)  left  = true;
            if (e.getCode() == KeyCode.RIGHT) right = true;
            if (e.getCode() == KeyCode.UP)    up    = true;
            if (e.getCode() == KeyCode.DOWN)  down  = true;

            if (e.getCode() == KeyCode.SPACE) {
                if (isShip) {
                    fireProjectile();
                } else {
                    jumping = true;
                    if (jetpackTimer == null) {
                        jetpackTimer = new Timer(true);
                        jetpackTimer.schedule(new JetpackTask(), 500L);
                    }
                }
            }
        });

        scene.setOnKeyReleased(e -> {
            boolean isShip = level.getPlatforms().isEmpty() && level.getEnemies().isEmpty();

            if (e.getCode() == KeyCode.LEFT)  left  = false;
            if (e.getCode() == KeyCode.RIGHT) right = false;
            if (e.getCode() == KeyCode.UP)    up    = false;
            if (e.getCode() == KeyCode.DOWN)  down  = false;

            if (e.getCode() == KeyCode.SPACE && !isShip) {
                jumping = false;
                jetpack = false;
                player.setJetpackActive(false);
                if (jetpackTimer != null) {
                    jetpackTimer.cancel();
                    jetpackTimer = null;
                }
            }
        });
    }

    /** Démarre la boucle via AnimationTimer. */
    public void startGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastTime = 0;
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                double deltaSec = (now - lastTime) / 1_000_000_000.0;
                update(deltaSec);
                lastTime = now;
            }
        };
        gameLoop.start();
    }

    /** Stoppe proprement la boucle en cours. */
    public void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    /** Itération de la boucle (~60FPS). */
    private void update(double deltaSec) {
        boolean isShip = level.getPlatforms().isEmpty() && level.getEnemies().isEmpty();

        // 1) Détection de l'entrée en zone de boss (seulement si un boss est encore vivant)
        boolean bossAlive = enemies.stream().anyMatch(e -> e instanceof Boss);
        if (!inBossFight
            && bossAlive
            && player.getX() + player.getWidth() >= level.getBossZoneStart()) {
            inBossFight = true;
        }

        // 2) Mouvement du joueur / vaisseau
        double dx    = 0.0;
        double speed = player.getSpeed() * 1.5; // px/s
        if (left)  { dx -= speed * deltaSec; player.setFacingRight(false); }
        if (right) { dx += speed * deltaSec; player.setFacingRight(true); }

        if (isShip) {
            updateSpaceship(dx, deltaSec);
            updateProjectiles(deltaSec);

            // Défilement auto si pas en boss fight
            if (!inBossFight) {
                cameraX += SHIP_SCROLL_SPEED * deltaSec;
                cameraX = Math.min(cameraX, level.getLevelWidth() - view.getCanvasWidth());
            }

            // Contrainte du joueur dans la zone visible
            double minX    = cameraX;
            double maxXpos = cameraX + view.getCanvasWidth() - player.getWidth();
            if (player.getX() < minX)    player.setX(minX);
            if (player.getX() > maxXpos) player.setX(maxXpos);

        } else {
            updatePlatform(dx, deltaSec);
        }

        // 3) Confinement en zone de boss selon les bornes du JSON
        if (inBossFight) {
            double startX = level.getBossZoneStart();
            double endX   = level.getBossZoneEnd() - player.getWidth();
            if (player.getX() < startX) player.setX(startX);
            if (player.getX() > endX)   player.setX(endX);
        }

        // 4) Passage de niveau lorsque le joueur atteint la fin du level
        if (player.getX() + player.getWidth() >= level.getLevelWidth()) {
            javafx.application.Platform.runLater(model::nextLevel);
        }

        // 5) Sortie du boss fight si le boss est éliminé
        if (inBossFight && !bossAlive) {
            inBossFight = false;
        }

        // 6) Mise à jour caméra et rendu
        updateCamera(isShip);
        render(isShip);
    }

    /** Update en mode spaceship (déplacement libre). */
    private void updateSpaceship(double dx, double deltaSec) {
        double dy = 0.0;
        if (up)   dy -= player.getSpeed() * 1.5 * deltaSec;
        if (down) dy += player.getSpeed() * 1.5 * deltaSec;
        player.move(dx, dy);
    }

    /** Met à jour projectiles (mouvement + collisions). */
    private void updateProjectiles(double deltaSec) {
        Iterator<Projectile> pit = projectiles.iterator();
        while (pit.hasNext()) {
            Projectile p = pit.next();
            p.update(deltaSec);
            if (p.isOutOfBounds(level.getLevelWidth())) {
                pit.remove();
                continue;
            }
            Iterator<Enemy> eit = enemies.iterator();
            while (eit.hasNext()) {
                Enemy enemy = eit.next();
                if (p.intersects(enemy)) {
                    eit.remove();
                    pit.remove();
                    break;
                }
            }
        }
    }

    /** Tire un projectile (mode spaceship). */
    private void fireProjectile() {
        double offsetX = player.isFacingRight() ? player.getWidth() : -10;
        double px      = player.getX() + offsetX;
        double py      = player.getY() + player.getHeight() / 2.0;
        projectiles.add(new Projectile(px, py, player.isFacingRight()));
    }

    /** Update en mode plateforme (gravité + collisions). */
    private void updatePlatform(double dx, double deltaSec) {
        double oldY = player.getY();

        if (jumping && player.canJump() && !jetpack) {
            player.setVelocityY(-603.0);
            player.setOnGround(false);
            player.incrementJumps();
            jumping = false;
        }

        if (jetpack && player.isJetpackActive()) {
            player.setVelocityY(-300.0);
        } else {
            player.setVelocityY(player.velocityY + GRAVITY * deltaSec);
        }

        double dy = player.velocityY * deltaSec;
        player.move(dx, dy);

        handlePlatformCollisions(oldY);
        handleEnemies(deltaSec);

        if (player.getY() > level.getLevelHeight()) {
            resetPlayerPosition();
        }
    }

    private void handlePlatformCollisions(double oldY) {
        for (Platform p : platforms) {
            if (p instanceof src.model.game.platforms.FragilePlatform) {
                ((src.model.game.platforms.FragilePlatform) p).resetStep(player);
            }
        }
        for (Platform p : platforms) {
            double top    = p.getY();
            double botNow = player.getY() + player.getHeight();
            double botOld = oldY + player.getHeight();
            if (player.intersects(p) && player.velocityY > 0 && botOld <= top) {
                player.setY(top - player.getHeight());
                player.velocityY = 0;
                player.setOnGround(true);
                player.resetJumps();
                if (p instanceof src.model.game.platforms.FragilePlatform) {
                    var fp = (src.model.game.platforms.FragilePlatform) p;
                    if (!fp.isBroken()) fp.step(player);
                }
            }
        }
        platforms.removeIf(p ->
            p instanceof src.model.game.platforms.FragilePlatform
            && ((src.model.game.platforms.FragilePlatform) p).isBroken()
        );
    }

    /**
     * Met à jour chaque ennemi et gère les collisions avec le joueur :
     * - Si le joueur atterrit sur un ennemi, on inflige des dégâts ou on le supprime.
     * - Si le joueur touche un ennemi autrement qu’en saut, on le réinitialise (mort).
     *
     * @param deltaSec Temps écoulé depuis la dernière frame (en secondes)
     */
    private void handleEnemies(double deltaSec) {
        List<Enemy> toRemove = new ArrayList<>();

        for (Enemy e : enemies) {
            // 1) Mise à jour du comportement de l'ennemi (patrouille, saut, etc.)
            e.update(deltaSec);

            // 2) Si le joueur atterrit sur l'ennemi
            if (player.landsOn(e)) {
                if (e instanceof Boss) {
                    Boss boss = (Boss) e;
                    boss.hit();
                    // Si le boss est mort après ce hit, on le supprime
                    if (boss.isDead()) {
                        toRemove.add(boss);
                    }
                    // Rebond du joueur après avoir touché le boss
                    player.setVelocityY(-600.0);
                } else {
                    // Ennemis normaux : on les supprime et le joueur rebondit
                    toRemove.add(e);
                    player.setVelocityY(-600.0);
                }
            }
            // 3) Si le joueur entre en collision (hors atterrissage), c'est la mort
            else if (player.intersects(e)) {
                resetPlayerPosition();
            }
        }

        // 4) On retire tous les ennemis marqués pour suppression
        enemies.removeAll(toRemove);
    }

    private void updateCamera(boolean isShip) {
        double cw = view.getCanvasWidth();
        double ch = view.getCanvasHeight();
        double levelH = level.getLevelHeight();
        double levelW = level.getLevelWidth();
    
        if (inBossFight) {
            // --- Boss-fight : centre horizontalement sur la zone de boss ---
            double startX = level.getBossZoneStart();
            double endX   = level.getBossZoneEnd();
            double zoneCenter = (startX + endX) / 2.0;
            cameraX = zoneCenter - cw/2.0;
            cameraX = Math.max(0, Math.min(cameraX, levelW - cw));
    
            // --- Boss-fight vertical : aligner le bas du niveau ---
            if (levelH <= ch) {
                // si le level est plus petit que la fenêtre, on décale négativement
                cameraY = levelH - ch;
            } else {
                // sinon on plaque le bas du level en bas de fenêtre
                cameraY = levelH - ch;
            }
            view.cameraXProperty().set(cameraX);
            view.cameraYProperty().set(cameraY);
            return;
        }
    
        // --- Mode vaisseau (spaceship) : on plaque en Y en haut ---
        if (isShip) {
            cameraY = 0;
        } else {
            // --- Mode plateforme : X suivi lissé ---
            double targetX = player.getX() - cw/2.0;
            cameraX += 0.1 * (targetX - cameraX);
            cameraX = Math.max(0, Math.min(cameraX, levelW - cw));
    
            // --- Mode plateforme : Y ---
            if (levelH <= ch) {
                // niveau plus petit que la fenêtre : aligner en bas
                cameraY = levelH - ch;
            } else {
                // niveau plus grand : centrer sur le joueur avec lissage
                double targetY = player.getY() - ch/2.0;
                cameraY += 0.1 * (targetY - cameraY);
                cameraY = Math.max(0, Math.min(cameraY, levelH - ch));
            }
        }
    
        view.cameraXProperty().set(cameraX);
        view.cameraYProperty().set(cameraY);
    }
    


    /** Dessine tous les éléments du jeu. */
    private void render(boolean isShip) {
        List<Image> decoImgs   = new ArrayList<>();
        List<Double[]> posDeco = new ArrayList<>();
        for (Decoration d : decorations) {
            decoImgs.add(d.getTexture());
            posDeco.add(new Double[]{d.getX(), d.getY(), d.getWidth(), d.getHeight()});
        }

        List<Image> platImgs   = new ArrayList<>();
        List<Double[]> posPl   = new ArrayList<>();
        for (Platform p : platforms) {
            platImgs.add(p.getTexture());
            posPl.add(new Double[]{p.getX(), p.getY(), p.getWidth(), p.getHeight()});
        }

        List<Double[]> posProj = new ArrayList<>();
        for (Projectile p : projectiles) {
            posProj.add(new Double[]{p.getX(), p.getY(), p.getWidth(), p.getHeight()});
        }

        boolean isJumping = !player.onGround;
        view.draw(
            level.getBackgroundImage(),
            player.getX(), player.getY(),
            player.getWidth(), player.getHeight(),
            player.isWalking(), player.isFacingRight(),
            isJumping,
            isShip,
            decoImgs, posDeco,
            platImgs, posPl,
            enemies,
            posProj
        );
    }

    /** Réinitialise le joueur à sa position de départ. */
    private void resetPlayerPosition() {
        player.setX(initialPlayerX);
        player.setY(initialPlayerY);
        player.velocityY = 0;
        player.setOnGround(true);
        player.resetJumps();
        player.setJetpackActive(false);
        if (jetpackTimer != null) {
            jetpackTimer.cancel();
            jetpackTimer = null;
        }
    }

    /** Réinitialise seulement les flags d’état du joueur. */
    public void resetPlayerState() {
        left = right = up = down = jumping = jetpack = false;
        player.setJetpackActive(false);
        player.resetJumps();
        player.setVelocityY(0);
        player.setOnGround(true);
        if (jetpackTimer != null) {
            jetpackTimer.cancel();
            jetpackTimer = null;
        }
    }

    /** TimerTask pour activer le jetpack après 500 ms. */
    private class JetpackTask extends TimerTask {
        @Override
        public void run() {
            jetpack = true;
            player.setJetpackActive(true);
        }
    }
}
