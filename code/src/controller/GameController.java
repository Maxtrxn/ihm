package src.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import src.Game;
import src.levels.Level;
import src.model.Enemy;
import src.model.Platform;
import src.model.Player;
import src.model.platforms.FragilePlatform;
import src.view.GameView;
import javafx.scene.image.Image;

public class GameController {
   private static final double GRAVITY = 0.5;
   private static final double GAME_SPEED = 2.0;
   private boolean left;
   private boolean right;
   private boolean jumping;
   private boolean jetpack;
   private Player player;
   private List<Platform> platforms;
   private List<Enemy> enemies;
   private GameView view;
   private double initialPlayerX;
   private double initialPlayerY;
   private Timer jetpackTimer;
   private Game game;
   private Level level;

   // Dimensions "logiques" du niveau (à adapter !)
   // Par ex. 3000 px de large, 600 px de haut
   private double levelWidth = 3000.0;
   private double levelHeight = 600.0;

   // Caméra
   private double cameraX = 0.0;
   private double cameraY = 0.0;

   public GameController(Player player,
                         List<Platform> platforms,
                         List<Enemy> enemies,
                         GameView view,
                         Game game,
                         Level level) {
      this.player = player;
      this.platforms = platforms;
      this.enemies = enemies;
      this.view = view;
      this.game = game;
      this.level = level;
      this.initialPlayerX = player.getX();
      this.initialPlayerY = player.getY();
   }

   public void handleInput(Scene scene) {
      scene.setOnKeyPressed(event -> {
         if (event.getCode() == KeyCode.LEFT) {
            this.left = true;
         }
         if (event.getCode() == KeyCode.RIGHT) {
            this.right = true;
         }
         if (event.getCode() == KeyCode.SPACE) {
            this.jumping = true;
            if (this.jetpackTimer == null) {
               this.jetpackTimer = new Timer(true);
               this.jetpackTimer.schedule(new JetpackTask(), 500L);
            }
         }
      });
      
      scene.setOnKeyReleased(event -> {
         if (event.getCode() == KeyCode.LEFT) {
            this.left = false;
         }
         if (event.getCode() == KeyCode.RIGHT) {
            this.right = false;
         }
         if (event.getCode() == KeyCode.SPACE) {
            this.jumping = false;
            this.jetpack = false;
            this.player.setJetpackActive(false);
            if (this.jetpackTimer != null) {
               this.jetpackTimer.cancel();
               this.jetpackTimer = null;
            }
         }
      });
   }

   public void startGameLoop() {
      new GameLoopThread().start();
   }

   private void update() {
      double dx = 0.0;
      double speedFactor = 1.5;

      // Déplacements latéraux
      if (this.left) {
         dx -= this.player.getSpeed() * speedFactor;
         this.player.setFacingRight(false);
      }
      if (this.right) {
         dx += this.player.getSpeed() * speedFactor;
         this.player.setFacingRight(true);
      }

      // Saut
      if (this.jumping && this.player.canJump() && !this.jetpack) {
         this.player.velocityY = -10.0;
         this.player.incrementJumps();
         this.jumping = false;
      }

      // Jetpack ou gravité
      if (this.jetpack && this.player.isJetpackActive()) {
         this.player.velocityY = -5.0;
      } else {
         this.player.velocityY += GRAVITY;
      }

      // Application
      double dy = this.player.velocityY * 2.0;
      this.player.move(dx, dy);

      // Plateformes (fragiles, collisions)
      Iterator<Platform> it = this.platforms.iterator();
      while (it.hasNext()) {
         Platform p = it.next();
         if (p instanceof FragilePlatform) {
            ((FragilePlatform) p).resetStep(this.player);
         }
      }
      it = this.platforms.iterator();
      while (it.hasNext()) {
         Platform p = it.next();
         // Collision par le haut
         if (this.player.intersects(p) && this.player.velocityY > 0.0) {
            this.player.setY(p.getY() - this.player.getHeight());
            this.player.velocityY = 0.0;
            this.player.onGround = true;
            this.player.resetJumps();
            if (p instanceof FragilePlatform) {
               FragilePlatform fp = (FragilePlatform) p;
               if (!fp.isBroken()) {
                  fp.step(this.player);
               }
               if (fp.isBroken()) {
                  it.remove();
               }
            }
         }
      }

      // Ennemis
      Iterator<Enemy> enemyIt = this.enemies.iterator();
      while (enemyIt.hasNext()) {
         Enemy enemy = enemyIt.next();
         enemy.update();
         if (this.player.landsOn(enemy)) {
            enemyIt.remove();
            this.player.velocityY = -10.0;
         } else if (this.player.intersects(enemy)) {
            this.resetPlayerPosition();
         }
      }

      // Tombe hors du niveau ?
      if (this.player.getY() > levelHeight) {
         this.resetPlayerPosition();
      }

      // Passe au niveau suivant ?
      if (this.player.getX() > 1600.0) {
         javafx.application.Platform.runLater(() -> {
            this.game.nextLevel();
         });
      }

      // Caméra
      this.updateCamera();

      // Prépare les données pour l'affichage
      ArrayList<Image> platformImages = new ArrayList<>();
      ArrayList<Double[]> platformPositions = new ArrayList<>();
      for (Platform p : this.platforms) {
         platformImages.add(p.getTexture());
         platformPositions.add(new Double[]{p.getX(), p.getY(), p.getWidth(), p.getHeight()});
      }

      ArrayList<Double[]> enemyPositions = new ArrayList<>();
      for (Enemy enemy : this.enemies) {
         enemyPositions.add(new Double[]{enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight()});
      }

      // Dessin
      this.view.draw(
         this.level.getBackgroundImage(),
         this.player.getX(), this.player.getY(),
         this.player.getWidth(), this.player.getHeight(),
         this.player.isWalking(), this.player.isFacingRight(),
         platformImages, platformPositions, enemyPositions
      );
   }

   /**
    * Centre la caméra sur le joueur (en X et Y),
    * borne aux limites du niveau (levelWidth, levelHeight).
    */
   private void updateCamera() {
      double canvasWidth = this.view.getCanvasWidth();
      double canvasHeight = this.view.getCanvasHeight();

      // Centre sur le joueur
      double targetX = this.player.getX() - canvasWidth / 2.0;
      double targetY = this.player.getY() - canvasHeight / 2.0;

      // Lissage
      this.cameraX += 0.1 * (targetX - this.cameraX);
      this.cameraY += 0.1 * (targetY - this.cameraY);

      // Borne gauche/haut
      if (this.cameraX < 0) {
         this.cameraX = 0;
      }
      if (this.cameraY < 0) {
         this.cameraY = 0;
      }

      // Borne droite/bas
      double maxCamX = levelWidth - canvasWidth;
      if (this.cameraX > maxCamX) {
         this.cameraX = maxCamX;
      }
      double maxCamY = levelHeight - canvasHeight;
      if (this.cameraY > maxCamY) {
         this.cameraY = maxCamY;
      }

      // Mise à jour de la GameView
      this.view.cameraXProperty().set(this.cameraX);
      this.view.cameraYProperty().set(this.cameraY);
   }

   private void resetPlayerPosition() {
      this.player.setX(this.initialPlayerX);
      this.player.setY(this.initialPlayerY);
      this.player.velocityY = 0.0;
      this.player.onGround = true;
      this.player.resetJumps();
      this.player.setJetpackActive(false);
      if (this.jetpackTimer != null) {
         this.jetpackTimer.cancel();
         this.jetpackTimer = null;
      }
   }

   public void resetPlayerState() {
      this.left = false;
      this.right = false;
      this.jumping = false;
      this.jetpack = false;
      this.player.setJetpackActive(false);
      this.player.resetJumps();
      this.player.setVelocityY(0.0);
      this.player.setOnGround(true);
      if (this.jetpackTimer != null) {
         this.jetpackTimer.cancel();
         this.jetpackTimer = null;
      }
   }

   // TimerTask pour le jetpack
   private class JetpackTask extends TimerTask {
      @Override
      public void run() {
         jetpack = true;
         player.setJetpackActive(true);
      }
   }

   // Thread de boucle de jeu
   private class GameLoopThread extends Thread {
      public GameLoopThread() {
         setDaemon(true);
      }
      @Override
      public void run() {
         while (true) {
            try {
               Thread.sleep(16);
            } catch (InterruptedException e) {
               break;
            }
            update();
         }
      }
   }
}
