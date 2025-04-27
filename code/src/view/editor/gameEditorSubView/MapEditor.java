package src.view.editor.gameEditorSubView;


import java.util.HashMap;
import java.util.Map;

import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import src.model.game.Decoration;
import src.model.game.Level;
import src.model.game.Platform;
import src.view.editor.GameEditorView;
import src.view.editor.GameEditorView.LevelObjectType;

public class MapEditor extends ScrollPane{
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
    private GameEditorView parent = null;
    private Map<LevelObjectType, Pane> correspondingPane;
    

    //Pour le chargement d'un niveau
    public MapEditor(Level level, GameEditorView parent){
        this(16, (int)level.getLevelHeight() / 16, (int)level.getLevelWidth() / 16, parent);
    }


    //Pour la création d'un nouveau niveau
    public MapEditor(int cellSize, int gridPaneNbRows, int gridPaneNbCols, GameEditorView parent){
        super();
        
        this.gridPaneNbRows = gridPaneNbRows;
        this.gridPaneNbCols = gridPaneNbCols;
        this.cellSize = cellSize;
        this.rectangles = new Rectangle[gridPaneNbRows][gridPaneNbCols];
        this.parent = parent;

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

        initializeGridPane();
        showLevel();
    }


    public void setSelectedLevelObjectImage(ImageView selectedLevelObjectImage, LevelObjectType selectedLevelObjectType){
        if(this.selectedLevelObjectType != null) this.correspondingPane.get(this.selectedLevelObjectType).getChildren().remove(this.selectedLevelObjectImage);
        
        this.selectedLevelObjectImage = selectedLevelObjectImage;
        this.selectedLevelObjectImage.setOpacity(0.5);
        this.selectedLevelObjectType = selectedLevelObjectType;
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
                    

                    if(this.selectedLevelObjectImage != null){
                        this.selectedRectangle = null;
                        Integer col = GridPane.getColumnIndex(rect);
                        Integer row = GridPane.getRowIndex(rect);

                        int colIndex = (col == null) ? 0 : col;
                        int rowIndex = (row == null) ? 0 : row;

                        double levelObjectX = colIndex * cellSize;
                        double levelObjectY = rowIndex * cellSize;

                        switch (this.selectedLevelObjectType) {
                            case LevelObjectType.PLATFORM:
                                this.parent.getController().addPlatform(levelObjectX, levelObjectY);
                                break;
                            case LevelObjectType.DECORATION:
                                this.parent.getController().addDecoration(levelObjectX, levelObjectY, false);
                                break;
                            case LevelObjectType.ENEMY:
                                this.parent.getController().addEnemy(levelObjectX, levelObjectY, 0, 0, 0);
                                break;
                            default:
                                break;
                        }
                        showLevel();
                        
                    }else{
                        this.selectedRectangle = rect;
                        rect.setFill(this.SELECT_COLOR);
                    }
                });
                gridPane.add(rect, j, i);
                rectangles[i][j] = rect;
            }
        }
    }


    public void showOneLayer(int visibleLayer){
        hideAllLayers();
        switch (visibleLayer) {
            case 0: //Montrer que la couche du fond de niveau
                this.backgroundLayer.setVisible(true);
                break;
            case 1: //Montrer que la couche derrière la principale
                this.behindLayer.setVisible(true);
                break;
            case 2: //Montrer que la couche principale
                this.mainLayer.setVisible(true);
                break;
            case 3: //Montrer que la couche devant la principale
                this.foregroundLayer.setVisible(true);
                break;
            default:
                showAllLayers();
                break;
        }
    }

    public void showAllLayers(){
        this.backgroundLayer.setVisible(true);
        this.behindLayer.setVisible(true);
        this.mainLayer.setVisible(true);
        this.foregroundLayer.setVisible(true);
    }

    public void hideAllLayers(){
        this.backgroundLayer.setVisible(false);
        this.behindLayer.setVisible(false);
        this.mainLayer.setVisible(false);
        this.foregroundLayer.setVisible(false);
    }

    public void showGridLines(){
        gridPane.setGridLinesVisible(true);
    }

    public void hideGridLines(){
        gridPane.setGridLinesVisible(false);
    }


    public void showLevel(){
        Level level = this.parent.getController().getLevel();

        this.backgroundLayer.getChildren().clear();
        ImageView bg = new ImageView(level.getBackgroundImage());
        this.backgroundLayer.getChildren().add(bg);
        bg.setLayoutX(0);
        bg.setLayoutY(0);
        
        this.mainLayer.getChildren().clear();
        for (Platform platform : level.getPlatforms()) {
            ImageView temp = new ImageView(platform.getTexture());
            temp.setFitWidth(platform.getWidth());
            temp.setFitHeight(platform.getHeight());
            this.mainLayer.getChildren().add(temp);
            temp.setLayoutX(platform.getX());
            temp.setLayoutY(platform.getY());
        }

        this.behindLayer.getChildren().clear();
        for (Decoration decoration : level.getDecorations()) {
            ImageView temp = new ImageView(decoration.getTexture());
            temp.setFitWidth(decoration.getWidth());
            temp.setFitHeight(decoration.getHeight());
            this.behindLayer.getChildren().add(temp);
            temp.setLayoutX(decoration.getX());
            temp.setLayoutY(decoration.getY());
        }
    }
}
