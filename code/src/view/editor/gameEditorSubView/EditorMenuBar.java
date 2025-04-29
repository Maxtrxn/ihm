package src.view.editor.gameEditorSubView;

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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.common.JsonReaderException;
import src.view.editor.GameEditorView;

public class EditorMenuBar extends MenuBar{
    private GameEditorView parent;

    public EditorMenuBar(GameEditorView parent){
        this.parent = parent;

        //Création du menu "Fichier"
        Menu fichierMenu = new Menu("Fichier");
        //Création des éléments que contient "Fichier"
        MenuItem newLevelItem = new MenuItem("Nouveau");
        newLevelItem.setOnAction(e -> newLevelItemAction());
        MenuItem openLevelItem = new MenuItem("Charger un niveau");
        openLevelItem.setOnAction(event -> openLevelItemAction());
        MenuItem deleteLevelItem = new MenuItem("Supprimer un niveau");
        deleteLevelItem.setOnAction(event -> deleteLevelItemAction());
        MenuItem choseBackgroundItem = new MenuItem("Choisir une image de fond");
        choseBackgroundItem.setOnAction(event -> choseBackgroundItemAction());
        MenuItem saveLevelItem = new MenuItem("Enregistrer");
        saveLevelItem.setOnAction(event -> saveLevelItemAction());
        MenuItem changeLevelNameItem = new MenuItem("Changer le nom du niveau");
        changeLevelNameItem.setOnAction(event -> changeLevelNameItemAction());
        MenuItem quitItem = new MenuItem("Quitter l'éditeur");
        quitItem.setOnAction(event -> Platform.exit());
        fichierMenu.getItems().addAll(newLevelItem, openLevelItem, saveLevelItem, deleteLevelItem, choseBackgroundItem, changeLevelNameItem, new SeparatorMenuItem(), quitItem);

        //Création du menu "Paramètres"
        Menu parametresMenu = new Menu("Paramètres");
        //Création des éléments que contient "Paramètres"
        MenuItem preferencesItem = new MenuItem("Préférences");
        parametresMenu.getItems().add(preferencesItem);

        //Création du menu "Édition"
        Menu editionMenu = new Menu("Édition");
        //Création des éléments que contient "Édition"
        MenuItem copierItem = new MenuItem("Copier");
        MenuItem collerItem = new MenuItem("Coller");
        editionMenu.getItems().addAll(copierItem, collerItem);


        //Création du menu "Niveau"
        Menu levelMenu = new Menu("Niveau");
        //Création des éléments que contient "Édition"
        MenuItem levelPropertyItem = new MenuItem("Propriétés du niveau");
        MenuItem testLevelItem = new MenuItem("Tester le niveau");
        testLevelItem.setOnAction(e -> testLevelItemAction());
        levelMenu.getItems().addAll(levelPropertyItem, testLevelItem);

        //On ajoute les menus à la barre
        this.getMenus().addAll(fichierMenu, parametresMenu, editionMenu, levelMenu);
    }


    private void choseBackgroundItemAction(){
        if(this.parent.getCenter() == null){
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez choisir ou créer un niveau avant de changer le fond de niveau");
            alert.showAndWait();
            return;
        }

        //On créer un sélecteur de fichier
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");

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
            
            parent.updateBackground(image);
        }
    }


    private void newLevelItemAction(){
        Stage newWindow = new Stage();
        newWindow.initModality(Modality.APPLICATION_MODAL);


        Label levelNameLabel = new Label("Sélectionnez le nom niveau :");
        TextField levelName = new TextField();

        Label cellSizeLabel = new Label("Sélectionnez la taille des cases de l'éditeur (16-128) :");
        Spinner<Integer> cellSizeSelection = new Spinner<>(16, 128, 16, 8);
        cellSizeSelection.setEditable(true);

        Label nbRowsLabel = new Label("Sélectionnez le nombre de ligne pour la grille (1-10000) :");
        Spinner<Integer> nbRows = new Spinner<>(1, 10000, 40, 1);
        nbRows.setEditable(true);

        Label nbColsLabel = new Label("Sélectionnez le nombre de colonne pour la grille (1-10000) :");
        Spinner<Integer> nbCols = new Spinner<>(1, 10000, 400, 1);
        nbCols.setEditable(true);


        HBox buttons = new HBox();
        Button createButton = new Button("Créer");
        createButton.setOnAction(e -> {
            this.parent.initLevel(levelName.getText(), cellSizeSelection.getValue(), nbRows.getValue(), nbCols.getValue());
            newWindow.close();
        });
        Button cancelButton = new Button("Annuler");
        cancelButton.setOnAction(e -> {
            newWindow.close();
        });
        buttons.getChildren().addAll(createButton, cancelButton);
        buttons.setStyle("-fx-padding: 10; -fx-alignment: center;");


        VBox layout = new VBox(10, levelNameLabel, levelName, cellSizeLabel, cellSizeSelection, nbRowsLabel, nbRows, nbColsLabel, nbCols, buttons);
        layout.setStyle("-fx-padding: 10; -fx-alignment: center;");

        Scene scene = new Scene(layout, 300, 400);
        newWindow.setScene(scene);
        newWindow.setTitle("Paramètres du niveau");
        newWindow.showAndWait();
    }

    private void saveLevelItemAction(){
        Alert alert;
        try {
            this.parent.getController().saveLevel(false);
            alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText("L'enregistrement a réussi !");
            alert.setContentText("Le niveau est dans Jeu/resources/levels");
        } catch (Exception e) {
            if(e instanceof JsonReaderException){
                alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Choix de confirmation");
                alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                alert.setContentText("Ce nom de niveau existe déjà, voulez vous le remplacer ?");
            }else{
                alert = new Alert(AlertType.INFORMATION);
                alert.setContentText("Il y a eu une erreur lors de l'enregistrement");
            }
            alert.setHeaderText("L'enregistrement a échoué !");
        }
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == ButtonType.YES) {
                this.parent.getController().saveLevel(true);
            }
        }
    }


    private void openLevelItemAction(){
        final String[] levelName = {null};
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Sélectionnez un niveau à charger");

        ListView<String> listView = new ListView<>();
        File directory = new File("../resources/levels/");
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    listView.getItems().add(fileName.substring(0, fileName.lastIndexOf('.')));
                }
            }
        }

        Button selectButton = new Button("Sélectionner");
        selectButton.setOnAction(e -> {
            levelName[0] = listView.getSelectionModel().getSelectedItem();
            window.close();
        });
        Button cancelButton = new Button("Annuler");
        cancelButton.setOnAction(e -> {window.close();});

        HBox buttons = new HBox(10, selectButton, cancelButton);
        VBox layout = new VBox(10, listView, buttons);
        Scene scene = new Scene(layout, 400, 400);
        window.setScene(scene);
        window.showAndWait();

        if(levelName[0] != null){
            this.parent.getController().loadLevel(levelName[0]);
        }
    }


    private void changeLevelNameItemAction(){
        if(this.parent.getCenter() == null){
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez choisir ou créer un niveau avant de changer le nom à sauvegarder");
            alert.showAndWait();
            return;
        }

        Stage newWindow = new Stage();
        newWindow.initModality(Modality.APPLICATION_MODAL);


        Label levelNameLabel = new Label("Sélectionnez le nom niveau :");
        TextField levelName = new TextField();

        HBox buttons = new HBox();
        Button createButton = new Button("Changer");
        createButton.setOnAction(e -> {
            this.parent.updateLevelName(levelName.getText());
            newWindow.close();
        });
        Button cancelButton = new Button("Annuler");
        cancelButton.setOnAction(e -> {
            newWindow.close();
        });
        buttons.getChildren().addAll(createButton, cancelButton);
        buttons.setStyle("-fx-padding: 10; -fx-alignment: center;");


        VBox layout = new VBox(10, levelNameLabel, levelName, buttons);
        layout.setStyle("-fx-padding: 10; -fx-alignment: center;");

        Scene scene = new Scene(layout, 300, 200);
        newWindow.setScene(scene);
        newWindow.setTitle("Changement du nom de niveau pour la sauvegarde");
        newWindow.showAndWait();
    }



    private void deleteLevelItemAction(){
        final String[] levelName = {null};
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Sélectionnez un niveau à supprimer");

        ListView<String> listView = new ListView<>();
        File directory = new File("../resources/levels/");
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    listView.getItems().add(fileName.substring(0, fileName.lastIndexOf('.')));
                }
            }
        }

        Button selectButton = new Button("Sélectionner");
        selectButton.setOnAction(e -> {
            levelName[0] = listView.getSelectionModel().getSelectedItem();
            window.close();
        });
        Button cancelButton = new Button("Annuler");
        cancelButton.setOnAction(e -> {window.close();});

        HBox buttons = new HBox(10, selectButton, cancelButton);
        VBox layout = new VBox(10, listView, buttons);
        Scene scene = new Scene(layout, 400, 400);
        window.setScene(scene);
        window.showAndWait();

        if(levelName[0] != null){
            this.parent.getController().deleteLevel(levelName[0]);
        }
    }


    public void testLevelItemAction(){
        if(this.parent.getCenter() != null){
            this.parent.getController().saveLevel(true);

            Stage gameStage = new Stage();
            gameStage.initModality(Modality.APPLICATION_MODAL);
            gameStage.setTitle("Test du niveau");
            this.parent.getController().testLevel(gameStage);
            gameStage.showAndWait();
        }else{
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez choisir ou créer un niveau avant de le tester");
            alert.showAndWait();
            return;
        } 
    }
}










