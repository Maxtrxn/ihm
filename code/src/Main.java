package src;

import java.util.Locale;

import javafx.application.Application;
import javafx.stage.Stage;
import src.common.ResourceManager;
import src.controller.MainMenuController;



public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        ResourceManager.setLocale(Locale.FRENCH);
        new MainMenuController(primaryStage);

        primaryStage.setTitle("Steampunk Adventure");
        primaryStage.show();
    }
}