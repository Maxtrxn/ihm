package src.view.editor;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
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
import src.controller.editor.MapEditorController;
import src.model.game.Decoration;
import src.model.game.Enemy;
import src.model.game.Level;
import src.model.game.LevelObject;
import src.model.game.Platform;

public class MapEditorView extends ScrollPane{
    private MapEditorController controller;
    private Rectangle[][] rectangles;
    private StackPane layers;
    private Pane backgroundLayer;
    private Pane behindLayer;
    private Pane mainLayer;
    private Pane foregroundLayer;
    private GridPane gridPane;
    private int gridPaneNbRows;
    private int gridPaneNbCols;
    private int cellSize;
    private final Color DEFAULT_COLOR = Color.rgb(255, 255, 255, 0.0); //Couleur de base d'une case : blanc pour l'instant mais transparent après
    private final Color HOVER_COLOR = Color.rgb(0, 0, 255, 0.3); //Couleur de la case sélectionnée : bleu translucide
    private final Color SELECT_COLOR = Color.rgb(0, 255, 0, 0.3); //Couleur de la case sélectionnée : rouge translucide
    private ImageView selectedLevelObjectImage = null;
    private LevelObjectType selectedLevelObjectType;
    private Rectangle selectedRectangle = null;
    private Map<LevelObjectType, Pane> correspondingPane;
    private LevelObject clickSelectedLevelObject = null;

    public MapEditorView(MapEditorController controller, Level level){
        this.controller = controller;
        this.cellSize = 16;
        this.gridPaneNbRows = (int)level.getLevelHeight() / this.cellSize;
        this.gridPaneNbCols = (int)level.getLevelWidth() / this.cellSize;
        this.rectangles = new Rectangle[this.gridPaneNbRows][this.gridPaneNbCols];
        
        this.addEventFilter(ScrollEvent.ANY, event -> {
            //On utilise addEventFilter sur ScrollEvent.ANY pour retirer celui que le ScrollPane avait de base
            //qui scrollait verticalement mais pour la map qui est étendue horizontalement c'est mieux que
            //le scroll soit horizontal.

            if (event.getDeltaY() != 0) {
                //Si la molette défile verticalement, on défile horizontalement
                this.setHvalue(this.getHvalue() - (event.getDeltaY() / 1000));
            }
            event.consume();
        });

        this.layers = new StackPane();
        this.backgroundLayer = new Pane();
        this.behindLayer = new Pane();
        this.mainLayer = new Pane();
        this.foregroundLayer = new Pane();
        this.gridPane = new GridPane();
        this.layers.getChildren().addAll(backgroundLayer, behindLayer, mainLayer, foregroundLayer, gridPane);
        this.setContent(this.layers);

        this.correspondingPane = new HashMap<>();
        correspondingPane.put(LevelObjectType.PLATFORM, this.mainLayer);
        correspondingPane.put(LevelObjectType.DECORATION, this.behindLayer);
        correspondingPane.put(LevelObjectType.ENEMY, this.mainLayer);
        correspondingPane.put(LevelObjectType.FRAGILE_PLATFORM, this.mainLayer);
        correspondingPane.put(LevelObjectType.BOSS, this.mainLayer);

        initializeGridPane();
        showLevel(level);
    }



    private void initializeGridPane(){
        //gridPane.setStyle("-fx-grid-lines-visible: true;");
        gridPane.setGridLinesVisible(true);
        for (int i = 0; i < gridPaneNbRows; i++) {
            for (int j = 0; j < gridPaneNbCols; j++) {
                Rectangle rect = new Rectangle(this.cellSize, this.cellSize);
                rect.setFill(this.DEFAULT_COLOR);
                rect.setOnMouseEntered((MouseEvent e) -> {
                    if(this.selectedLevelObjectImage == null){
                        if(rect.getFill() != this.SELECT_COLOR){
                            rect.setFill(this.HOVER_COLOR);
                        }
                    }else{
                        Integer col = GridPane.getColumnIndex(rect);
                        Integer row = GridPane.getRowIndex(rect);

                        int colIndex = (col == null) ? 0 : col;
                        int rowIndex = (row == null) ? 0 : row;

                        this.correspondingPane.get(this.selectedLevelObjectType).getChildren().add(this.selectedLevelObjectImage);
                        this.selectedLevelObjectImage.setLayoutX(colIndex * cellSize);
                        this.selectedLevelObjectImage.setLayoutY(rowIndex * cellSize);  
                    }
                    
                });
                rect.setOnMouseExited((MouseEvent e) -> {
                    if(this.selectedLevelObjectImage == null){
                        if(rect.getFill() != this.SELECT_COLOR){
                            rect.setFill(this.DEFAULT_COLOR);
                        }
                    }else{
                        this.correspondingPane.get(this.selectedLevelObjectType).getChildren().remove(this.selectedLevelObjectImage);
                    }
                });

                //Ajout de la gestion du clique de la souris dans le grid pane
                rect.setOnMousePressed((MouseEvent e) -> {
                    if(this.selectedRectangle != null) this.selectedRectangle.setFill(this.DEFAULT_COLOR);
                    
                    Integer col = GridPane.getColumnIndex(rect);
                    Integer row = GridPane.getRowIndex(rect);

                    int colIndex = (col == null) ? 0 : col;
                    int rowIndex = (row == null) ? 0 : row;

                    double levelObjectX = colIndex * cellSize;
                    double levelObjectY = rowIndex * cellSize;

                    if(this.selectedLevelObjectImage != null){
                        this.selectedRectangle = null;

                        switch (this.selectedLevelObjectType) {
                            case LevelObjectType.FRAGILE_PLATFORM:
                            case LevelObjectType.PLATFORM:
                                this.controller.addPlatform(levelObjectX, levelObjectY);
                                break;
                            case LevelObjectType.DECORATION:
                                this.controller.addDecoration(levelObjectX, levelObjectY, false);
                                break;
                            case LevelObjectType.BOSS:
                            case LevelObjectType.ENEMY:
                                handleEnemyPlacement(levelObjectX, levelObjectY);
                                break;
                            default:
                                break;
                        }
                        
                        
                    }else{
                        //On envoie le centre du rectangle pour les coordonnées de la selection
                        this.clickSelectedLevelObject = this.controller.clickSelectLevelObject(levelObjectX + cellSize/2, levelObjectY + cellSize/2);
                        this.selectedRectangle = rect;
                        rect.setFill(this.SELECT_COLOR);
                    }
                });
                gridPane.add(rect, j, i);
                rectangles[i][j] = rect;
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



    public void showLevel(Level level){

        this.backgroundLayer.getChildren().clear();
        ImageView bg = new ImageView(level.getBackgroundImage());
        this.backgroundLayer.getChildren().add(bg);
        bg.setLayoutX(0);
        bg.setLayoutY(0);
        
        this.mainLayer.getChildren().clear();
        this.behindLayer.getChildren().clear();
        this.foregroundLayer.getChildren().clear();

        for(LevelObject levelObject : level.getLevelObjects()){
            ImageView temp = new ImageView(levelObject.getTexture());
            temp.setFitWidth(levelObject.getWidth());
            temp.setFitHeight(levelObject.getHeight());
            temp.setLayoutX(levelObject.getX());
            temp.setLayoutY(levelObject.getY());

            if(levelObject == this.clickSelectedLevelObject){
                Blend blend = new Blend(
                    BlendMode.MULTIPLY,
                    null,
                    new ColorInput(0, 0, levelObject.getWidth(), levelObject.getHeight(), Color.color(0.0, 1.0, 0.0, 0.6))
                );
                temp.setEffect(blend);
            }

            if(levelObject instanceof Platform || levelObject instanceof Enemy){
                this.mainLayer.getChildren().add(temp);
            }else if(levelObject instanceof Decoration){
                this.behindLayer.getChildren().add(temp);
            }
        }
    }
}
