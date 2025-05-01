package src.controller.editor;

import java.io.File;
import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.common.ResourceManager;
import src.controller.game.GameController;
import src.model.editor.GameEditorModel;
import src.view.editor.EditorMenuBarView;

public class EditorMenuBarController {
    private GameEditorModel model;
    private EditorMenuBarView view;
    private Stage stage;

    public EditorMenuBarController(GameEditorModel model, Stage stage){
        this.model = model;
        this.stage = stage;
        this.view = new EditorMenuBarView(this);

        this.view.getMenuItem(null);
    }

    public EditorMenuBarView getEditorMenuBarView(){
        return this.view;
    }


    // --- File Menu Items ---

    public void handleFileNewLevel(){
        Stage newWindow = new Stage();

        //Les champs input
        Label levelNameLabel = new Label(ResourceManager.getString("new_level_name_label"));
        TextField levelName = new TextField();


        Label levelHeightLabel = new Label(ResourceManager.getString("new_level_height_label"));
        Spinner<Integer> levelHeight = new Spinner<>(128, 1280, 640, 128);
        levelHeight.setEditable(true);
        levelHeight.valueProperty().addListener((observable, oldValue, newValue) -> {
            if ((newValue - 128) % 128 != 0) {
                int correctedValue = ((newValue - 128) / 128) * 128 + 128;
                levelHeight.getValueFactory().setValue(correctedValue);
            }
        });

        Label levelWidthLabel = new Label(ResourceManager.getString("new_level_width_label"));
        Spinner<Integer> levelWidth = new Spinner<>(128, 12800, 6400, 128);
        levelWidth.setEditable(true);
        levelWidth.valueProperty().addListener((observable, oldValue, newValue) -> {
            if ((newValue - 128) % 128 != 0) {
                int correctedValue = ((newValue - 128) / 128) * 128 + 128;
                levelWidth.getValueFactory().setValue(correctedValue);
            }
        });

        //Les boutons
        HBox buttons = new HBox(10);
        Button createButton = new Button(ResourceManager.getString("create_button"));
        createButton.setOnAction(e -> {
            this.model.initLevel(levelName.getText(), levelWidth.getValue(), levelHeight.getValue());
            newWindow.close();
        });
        Button cancelButton = new Button(ResourceManager.getString("cancel_button"));
        cancelButton.setOnAction(e -> {
            newWindow.close();
        });
        buttons.getChildren().addAll(createButton, cancelButton);
        buttons.setStyle("-fx-padding: 10; -fx-alignment: center;");

        //Paramètres de la fenêtre
        VBox layout = new VBox(10, levelNameLabel, levelName, levelHeightLabel, levelHeight, levelWidthLabel, levelWidth, buttons);
        layout.setStyle("-fx-padding: 10; -fx-alignment: center;");
        Scene scene = new Scene(layout, 450, 400);

        newWindow.initModality(Modality.APPLICATION_MODAL);
        newWindow.setScene(scene);
        newWindow.setTitle(ResourceManager.getString("new_level_window_title"));
        newWindow.showAndWait();
    }

    public void handleFileOpenLevel(){
        final String[] levelName = {null};
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(ResourceManager.getString("file_open_level"));

        ListView<String> listView = new ListView<>();
        File directory = new File(ResourceManager.LEVELS_FOLDER);
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    listView.getItems().add(fileName.substring(0, fileName.lastIndexOf('.')));
                }
            }
        }

        Button selectButton = new Button(ResourceManager.getString("select_button"));
        selectButton.setOnAction(e -> {
            levelName[0] = listView.getSelectionModel().getSelectedItem();
            window.close();
        });
        Button cancelButton = new Button(ResourceManager.getString("cancel_button"));
        cancelButton.setOnAction(e -> {window.close();});

        HBox buttons = new HBox(10, selectButton, cancelButton);
        VBox layout = new VBox(10, listView, buttons);
        Scene scene = new Scene(layout, 400, 400);
        window.setScene(scene);
        window.showAndWait();

        if(levelName[0] != null){
            this.model.loadLevel(levelName[0]);
        }
    }

    public void handleFileSaveLevel(){
        Alert alert;
        if(this.model.saveLevel(false)){ //Si le fichier existe déjà
            alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(ResourceManager.getString("info_title"));
            alert.setHeaderText(ResourceManager.getString("Level_save_success"));
            alert.setContentText(ResourceManager.getString("Level_save_folder"));
            alert.showAndWait();
        }else{
            alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(ResourceManager.getString("Level_save_confirm_title"));
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            alert.setContentText(ResourceManager.getString("level_save_overwrite"));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == ButtonType.YES) {
                    this.model.saveLevel(true);
                }
            }  
        }
    }

    public void handleFileDeleteLevel(){
        final String[] levelName = {null};
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(ResourceManager.getString("file_delete_level"));

        ListView<String> listView = new ListView<>();
        File directory = new File(ResourceManager.LEVELS_FOLDER);
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    listView.getItems().add(fileName.substring(0, fileName.lastIndexOf('.')));
                }
            }
        }

        Button selectButton = new Button(ResourceManager.getString("select_button"));
        selectButton.setOnAction(e -> {
            levelName[0] = listView.getSelectionModel().getSelectedItem();
            window.close();
        });
        Button cancelButton = new Button(ResourceManager.getString("cancel_button"));
        cancelButton.setOnAction(e -> {window.close();});

        HBox buttons = new HBox(10, selectButton, cancelButton);
        VBox layout = new VBox(10, listView, buttons);
        Scene scene = new Scene(layout, 400, 400);
        window.setScene(scene);
        window.showAndWait();

        if(levelName[0] != null){
            this.model.deleteLevel(levelName[0]);
        }
    }

    public void handleFileQuit(){
        Platform.exit();
    }

    



    // --- Level Menu Items ---

    public void handleLevelChangeLevelName(){
        if(this.model.getLevel() == null){
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(ResourceManager.getString("info_title"));
            alert.setHeaderText(null);
            alert.setContentText(ResourceManager.getString("level_name_change_error"));
            alert.showAndWait();
            return;
        }

        Stage newWindow = new Stage();
        newWindow.initModality(Modality.APPLICATION_MODAL);


        Label levelNameLabel = new Label(ResourceManager.getString("level_name_change_label"));
        TextField levelName = new TextField();

        HBox buttons = new HBox();
        Button createButton = new Button(ResourceManager.getString("confirm_button"));
        createButton.setOnAction(e -> {
            this.model.setLevelName(levelName.getText());
            newWindow.close();
        });
        Button cancelButton = new Button(ResourceManager.getString("cancel_button"));
        cancelButton.setOnAction(e -> {
            newWindow.close();
        });
        buttons.getChildren().addAll(createButton, cancelButton);
        buttons.setStyle("-fx-padding: 10; -fx-alignment: center;");


        VBox layout = new VBox(10, levelNameLabel, levelName, buttons);
        layout.setStyle("-fx-padding: 10; -fx-alignment: center;");

        Scene scene = new Scene(layout, 300, 200);
        newWindow.setScene(scene);
        newWindow.setTitle(ResourceManager.getString("Level_name_change_title"));
        newWindow.showAndWait();
    }

    public void handleLevelChangeLevelBackground(){
        if(this.model.getLevel() == null){
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(ResourceManager.getString("info_title"));
            alert.setHeaderText(null);
            alert.setContentText(ResourceManager.getString("level_background_error"));
            alert.showAndWait();
            return;
        }

        //On créer un sélecteur de fichier
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(ResourceManager.getString("level_background_title"));

        //On ajoute un filtre au sélecteur de fichier pour n'avoir que des images
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );


        //On ouvre la fenêtre du sélecteur de fichier
        File selectedFile = fileChooser.showOpenDialog(null);
        if(selectedFile != null){
            //On transforme le fichier choisi en instance d'Image et on calcule sa nouvelle hauteur pour qu'elle soit la
            //même que celle de la fentre du niveau (GridPane) en calculant aussi proportionnellement sa largeur 
            Image image = new Image(selectedFile.toURI().toString());
            
            this.model.setLevelBackground(image);
        }
    }

    public void handleLevelTestLevel(){
        if(this.model.getLevel() != null){
            String levelName = this.model.getLevelName();
            this.model.setLevelName("temp");
            this.model.saveLevel(true);
            this.model.setLevelName(levelName);

            Stage gameStage = new Stage();
            gameStage.initModality(Modality.APPLICATION_MODAL);
            gameStage.setTitle(ResourceManager.getString("level_test"));
            new GameController(gameStage, "temp");
            gameStage.showAndWait();
        }else{
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(ResourceManager.getString("info_title"));
            alert.setHeaderText(null);
            alert.setContentText(ResourceManager.getString("level_test_info"));
            alert.showAndWait();
            return;
        } 
    }



    // --- Preferences Menu Items ---
    public void handlePreferenceEditorTheme(){
        final String[] cssFileName = {null};
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(ResourceManager.getString("preferences_editor_theme_title"));

        ListView<String> listView = new ListView<>();
        File directory = new File(ResourceManager.STYLESHEET_FOLDER);
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    listView.getItems().add(fileName);
                }
            }
        }

        Button selectButton = new Button(ResourceManager.getString("select_button"));
        selectButton.setOnAction(e -> {
            cssFileName[0] = listView.getSelectionModel().getSelectedItem();
            window.close();
        });
        Button cancelButton = new Button(ResourceManager.getString("cancel_button"));
        cancelButton.setOnAction(e -> {window.close();});

        HBox buttons = new HBox(10, selectButton, cancelButton);
        VBox layout = new VBox(10, listView, buttons);
        Scene scene = new Scene(layout, 400, 400);
        window.setScene(scene);
        window.showAndWait();

        if(cssFileName[0] != null){
            this.stage.getScene().getStylesheets().clear();
            this.stage.getScene().getStylesheets().add(getClass().getResource("/css/" + cssFileName[0]).toString());
        }
    }

    public void handlePreferenceEditorLanguage(){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(ResourceManager.getString("info_title"));
        alert.setHeaderText(null);
        alert.setContentText(ResourceManager.getString("preferences_editor_language"));
        alert.showAndWait();
        return;
    }

}

