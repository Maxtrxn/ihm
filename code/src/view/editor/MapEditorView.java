package src.view.editor;

import java.beans.PropertyChangeEvent;
import java.nio.channels.Pipe.SourceChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.controller.editor.GameEditorController.LevelObjectType;
import src.common.ResourceManager;
import src.controller.editor.MapEditorController;
import src.model.game.Decoration;
import src.model.game.Enemy;
import src.model.game.Level;
import src.model.game.LevelObject;
import src.model.game.Platform;
import org.json.JSONObject;

public class MapEditorView{
    private final Color DEFAULT_COLOR = Color.rgb(255, 255, 255, 0.0); //Couleur de base d'une case : transparent
    private final Color HOVER_COLOR = Color.rgb(0, 0, 255, 0.3); //Couleur de la case sélectionnée : bleu translucide
    private final Color SELECT_COLOR = Color.rgb(0, 255, 0, 0.3); //Couleur de la case sélectionnée : rouge translucide
    private final int defaultCellSize = 16;
    private MapEditorController controller;
    private ScrollPane mainRegion;
    private HBox settingsRegion;
    private StackPane layers;
    private Pane backgroundLayer;
    private Pane behindLayer;
    private Pane mainLayer;
    private Pane foregroundLayer;
    private GridPane gridPane;
    private int gridPaneNbRows;
    private int gridPaneNbCols;
    private int cellSize;
    private ImageView selectedLevelObjectImage = null;
    private ObjectProperty<ImageView> clickSelectedLevelObjectImage = null;
    private Pane selectedLevelObjectImageCorrespondingPane = null;
    private LevelObjectType selectedLevelObjectType;
    private Rectangle selectedRectangle = null;
    

    public MapEditorView(MapEditorController controller, Level level){
        this.controller = controller;
        this.cellSize = defaultCellSize;
        this.gridPaneNbRows = (int)level.getLevelHeight() / this.cellSize;
        this.gridPaneNbCols = (int)level.getLevelWidth() / this.cellSize;
        this.mainRegion = new ScrollPane();
        this.settingsRegion = new HBox();
        this.layers = new StackPane();
        this.backgroundLayer = new Pane();
        this.behindLayer = new Pane();
        this.mainLayer = new Pane();
        this.foregroundLayer = new Pane();
        this.gridPane = new GridPane();
        this.layers.getChildren().addAll(backgroundLayer, behindLayer, mainLayer, foregroundLayer, gridPane);
        this.mainRegion.setContent(this.layers);
        
        this.clickSelectedLevelObjectImage = new SimpleObjectProperty<>();
        this.clickSelectedLevelObjectImage.addListener((observable, oldValue, newValue) -> {if (oldValue != null) oldValue.setEffect(null);});


        initMainRegion();
        initSettingsRegion();

        
        initGridPane();
        showLevel(level);
    }

    public ScrollPane getMainRegion(){return this.mainRegion;}
    public HBox getSettingsRegion(){return this.settingsRegion;}

    private void initMainRegion(){
        this.mainRegion.addEventFilter(ScrollEvent.ANY, event -> {
            //On utilise addEventFilter sur ScrollEvent.ANY pour retirer celui que le ScrollPane avait de base
            //qui scrollait verticalement mais pour la map qui est étendue horizontalement c'est mieux que
            //le scroll soit horizontal.

            if (event.getDeltaY() != 0) {
                //Si la molette défile verticalement, on défile horizontalement
                this.mainRegion.setHvalue(this.mainRegion.getHvalue() - (event.getDeltaY() / 1000));
            }
            event.consume();
        });
    }

    private void initSettingsRegion(){

    }

    public void updateSelectedLevelObjectImage(String levelObjectName){
        if(levelObjectName == null){
            this.selectedLevelObjectImage = null;
            this.selectedLevelObjectImageCorrespondingPane = null;
            return;
        }

        String pathToLevelObjectTexture = null;
        double scaleFactor = 1.0;
        if(ResourceManager.PLATFORMS_JSON.has(levelObjectName)){
            pathToLevelObjectTexture = ResourceManager.PLATFORMS_FOLDER + ResourceManager.PLATFORMS_JSON.getJSONObject(levelObjectName).getString("textureFileName");
            this.selectedLevelObjectImageCorrespondingPane = this.mainLayer;
            scaleFactor = ResourceManager.PLATFORMS_JSON.getJSONObject(levelObjectName).getDouble("scaleFactor");
        }else if(ResourceManager.DECORATIONS_JSON.has(levelObjectName)){
            pathToLevelObjectTexture = ResourceManager.DECORATIONS_FOLDER + ResourceManager.DECORATIONS_JSON.getJSONObject(levelObjectName).getString("textureFileName");
            this.selectedLevelObjectImageCorrespondingPane = this.behindLayer;
            scaleFactor = ResourceManager.DECORATIONS_JSON.getJSONObject(levelObjectName).getDouble("scaleFactor");
        }
        else if(ResourceManager.ENEMIES_JSON.has(levelObjectName)){
            pathToLevelObjectTexture = ResourceManager.ENEMIES_FOLDER + ResourceManager.ENEMIES_JSON.getJSONObject(levelObjectName).getString("textureFileName");
            this.selectedLevelObjectImageCorrespondingPane = this.mainLayer;
            scaleFactor = ResourceManager.ENEMIES_JSON.getJSONObject(levelObjectName).getDouble("scaleFactor");
        }
        this.selectedLevelObjectImage = new ImageView(new Image("file:" + pathToLevelObjectTexture));
        this.selectedLevelObjectImage.setOpacity(0.5);
        this.selectedLevelObjectImage.setFitHeight(this.selectedLevelObjectImage.getImage().getHeight() * scaleFactor);
        this.selectedLevelObjectImage.setFitWidth(this.selectedLevelObjectImage.getImage().getWidth() * scaleFactor);
    }

    private void initGridPane(){
        this.gridPane.setGridLinesVisible(true);
        for (int i = 0; i < this.gridPaneNbRows; i++) {
            for (int j = 0; j < this.gridPaneNbCols; j++) {
                Rectangle rect = new Rectangle(this.cellSize, this.cellSize);
                rect.setFill(this.DEFAULT_COLOR);
                this.gridPane.add(rect, j, i);

                //Ajout de la gestion du déplacement de la souris dans le gridpane
                rect.setOnMouseEntered((MouseEvent e) -> {
                    if(this.selectedLevelObjectImage != null){
                        Integer col = GridPane.getColumnIndex(rect);
                        Integer row = GridPane.getRowIndex(rect);
                        int colIndex = (col == null) ? 0 : col;
                        int rowIndex = (row == null) ? 0 : row;

                        this.selectedLevelObjectImageCorrespondingPane.getChildren().add(this.selectedLevelObjectImage);
                        this.selectedLevelObjectImage.setLayoutX(colIndex * cellSize);
                        this.selectedLevelObjectImage.setLayoutY(rowIndex * cellSize);
                    }
                });
                rect.setOnMouseExited((MouseEvent e) -> {
                    if(this.selectedLevelObjectImage != null){
                        this.selectedLevelObjectImageCorrespondingPane.getChildren().remove(this.selectedLevelObjectImage);
                    }
                });

                //Ajout de la gestion du clique de la souris dans le grid pane
                rect.setOnMousePressed((MouseEvent e) -> {
                    Integer col = GridPane.getColumnIndex(rect);
                    Integer row = GridPane.getRowIndex(rect);
                    int colIndex = (col == null) ? 0 : col;
                    int rowIndex = (row == null) ? 0 : row;
                    double cellTopLeftX = colIndex * cellSize;
                    double cellTopLeftY = rowIndex * cellSize;
                    this.controller.handleMouseClick(cellTopLeftX, cellTopLeftY);
                });
            }
        }
    }



    public void handleEnemyPlacement(double levelObjectX, double levelObjectY){
        Stage newWindow = new Stage();
        newWindow.initModality(Modality.APPLICATION_MODAL);

        Label leftPatrolDistanceLabel = new Label("Distance de la patrouille vers la gauche (en pixel) :");
        Spinner<Integer> leftPatrolDistanceSelection = new Spinner<>(0, 200, 100, 1);
        leftPatrolDistanceSelection.setEditable(true);

        Label rightPatrolDistanceLabel = new Label("Distance de la patrouille vers la droite (en pixel) :");
        Spinner<Integer> rightPatrolDistanceSelection = new Spinner<>(0, 200, 100, 1);
        rightPatrolDistanceSelection.setEditable(true);

        Label speedLabel = new Label("Vitesse de l'ennemi :");
        Spinner<Integer> speedSelection = new Spinner<>(0, 20, 2, 1);
        speedSelection.setEditable(true);

        HBox buttons = new HBox();
        Button createButton = new Button("Confirmer");
        createButton.setOnAction(e -> {
            double leftBound = leftPatrolDistanceSelection.getValue();
            double rightBound = rightPatrolDistanceSelection.getValue();
            double speed = speedSelection.getValue();
            this.controller.addEnemy(levelObjectX, levelObjectY, levelObjectX - leftBound, levelObjectX + rightBound, speed);
            newWindow.close();
        });
        Button cancelButton = new Button("Annuler");
        cancelButton.setOnAction(e -> {
            newWindow.close();
        });
        buttons.getChildren().addAll(createButton, cancelButton);
        buttons.setStyle("-fx-padding: 10; -fx-alignment: center;");


        VBox layout = new VBox(10, leftPatrolDistanceLabel, leftPatrolDistanceSelection, rightPatrolDistanceLabel,
        rightPatrolDistanceSelection, speedLabel, speedSelection, buttons);
        layout.setStyle("-fx-padding: 10; -fx-alignment: center;");

        Scene scene = new Scene(layout, 300, 400);
        newWindow.setScene(scene);
        newWindow.setTitle("Paramètres de l'ennemi");
        newWindow.showAndWait();
    }


    public void updateClickSelectedLevelObject(LevelObject levelObject){
        if(levelObject == null){
            this.clickSelectedLevelObjectImage.set(null);
            return;
        } 

        for (Pane panes : Arrays.asList(this.behindLayer, this.mainLayer, this.foregroundLayer)) {
            for (Node image : panes.getChildren()) {
                if(image instanceof ImageView){
                    if(image.getLayoutX() == levelObject.getX() && image.getLayoutY() == levelObject.getY()){
                        Blend blend = new Blend(
                            BlendMode.MULTIPLY,
                            null,
                            new ColorInput(0, 0, levelObject.getWidth(), levelObject.getHeight(), Color.color(0.0, 1.0, 0.0, 0.6))
                        );
                        image.setEffect(blend);
                        this.clickSelectedLevelObjectImage.set((ImageView)image);
                        return;
                    }
                    
                }
            }
        }
    }


    //On affiche visuellement le niveau en ajoutant chaque éléments sous leur forme graphique
    //dans les différente couche que contient la vue
    public void showLevel(Level level){
        //On clear tout ce qui est déjà affiché
        this.backgroundLayer.getChildren().clear();
        this.mainLayer.getChildren().clear();
        this.behindLayer.getChildren().clear();
        this.foregroundLayer.getChildren().clear();

        
        //On ajoute le background à la couche de fond
        ImageView bg = new ImageView(level.getBackgroundImage());
        this.backgroundLayer.getChildren().add(bg);
        bg.setLayoutX(0);
        bg.setLayoutY(0);
        
        //On ajoute les éléments qui vont dans la couche principale et celle de derrière
        for(LevelObject levelObject : level.getLevelObjects()){
            ImageView temp = new ImageView(levelObject.getTexture());
            temp.setFitWidth(levelObject.getWidth());
            temp.setFitHeight(levelObject.getHeight());
            temp.setLayoutX(levelObject.getX());
            temp.setLayoutY(levelObject.getY());

            /* 
            if(levelObject == this.clickSelectedLevelObject){
                Blend blend = new Blend(
                    BlendMode.MULTIPLY,
                    null,
                    new ColorInput(0, 0, levelObject.getWidth(), levelObject.getHeight(), Color.color(0.0, 1.0, 0.0, 0.6))
                );
                temp.setEffect(blend);
            }*/

            if(levelObject instanceof Platform || levelObject instanceof Enemy){
                this.mainLayer.getChildren().add(temp);
            }else if(levelObject instanceof Decoration){
                this.behindLayer.getChildren().add(temp);
            }
        }
    }
}
