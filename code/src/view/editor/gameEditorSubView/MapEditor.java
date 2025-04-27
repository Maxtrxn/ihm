package src.view.editor.gameEditorSubView;

import java.rmi.Remote;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import src.view.editor.GameEditorView;

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
    private ImageView selectedPlatformImage = null;
    private Rectangle selectedRectangle = null;
    private GameEditorView parent = null;


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
        initializeGridPane();
    }


    public void setSelectedPlatformImage(ImageView selectedPlatformImage){
        this.mainLayer.getChildren().remove(this.selectedPlatformImage);
        this.selectedPlatformImage = selectedPlatformImage;
    }


    private void initializeGridPane(){
        gridPane.setStyle("-fx-grid-lines-visible: true;");
        for (int i = 0; i < gridPaneNbRows; i++) {
            for (int j = 0; j < gridPaneNbCols; j++) {
                Rectangle rect = new Rectangle(this.cellSize, this.cellSize);
                rect.setFill(this.DEFAULT_COLOR);
                rect.setOnMouseEntered((MouseEvent e) -> {
                    if(this.selectedPlatformImage == null){
                        if(rect.getFill() != this.SELECT_COLOR){
                            rect.setFill(this.HOVER_COLOR);
                        }
                    }else{
                        Integer col = GridPane.getColumnIndex(rect);
                        Integer row = GridPane.getRowIndex(rect);

                        int colIndex = (col == null) ? 0 : col;
                        int rowIndex = (row == null) ? 0 : row;

                        this.mainLayer.getChildren().add(this.selectedPlatformImage);
                        this.selectedPlatformImage.setLayoutX(colIndex * cellSize);
                        this.selectedPlatformImage.setLayoutY(rowIndex * cellSize);
                        
                        //PLACEMENT DU BLOC DANS LE GRIDPANE MAIS VOIR CHATGPT POUR ÇA--------------------------------------------------------------------------------------------
                        
                    }
                    
                });
                rect.setOnMouseExited((MouseEvent e) -> {
                    if(this.selectedPlatformImage == null){
                        if(rect.getFill() != this.SELECT_COLOR){
                            rect.setFill(this.DEFAULT_COLOR);
                        }
                    }else{
                        this.mainLayer.getChildren().remove(this.selectedPlatformImage);
                    }
                });

                //Ajout de la gestion du clique de la souris dans le grid pane
                rect.setOnMousePressed((MouseEvent e) -> {
                    if(this.selectedRectangle != null) this.selectedRectangle.setFill(this.DEFAULT_COLOR);
                    

                    if(this.selectedPlatformImage != null){
                        this.selectedRectangle = null;
                        Integer col = GridPane.getColumnIndex(rect);
                        Integer row = GridPane.getRowIndex(rect);

                        int colIndex = (col == null) ? 0 : col;
                        int rowIndex = (row == null) ? 0 : row;

                        ImageView temp = new ImageView(this.selectedPlatformImage.getImage());
                        this.mainLayer.getChildren().add(temp);

                        double platformX = colIndex * cellSize;
                        double platformY = rowIndex * cellSize;
                        temp.setLayoutX(platformX);
                        temp.setLayoutY(platformY);
                        this.parent.getController().addPlatform(platformX, platformY);
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

    public void setMapBackground(Image image){
        double newHeight = gridPane.getHeight();
        double ratio = newHeight / image.getHeight(); 
        double newWidth = image.getWidth() * ratio;
        
        //On la transforme maintenant en BackgroundImage ce qui permet de la faire se répéter horizontalement dans le fond
        //et on lui donne la nouvelle taille qu'on a calculé avant. 
        BackgroundImage backgroundImage = new BackgroundImage(
            image,
            BackgroundRepeat.REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.DEFAULT,
            new BackgroundSize(newWidth,newHeight,false, false,false, false)
        );

        gridPane.setBackground(new Background(backgroundImage));
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
        gridPane.setStyle("-fx-grid-lines-visible: true;");
    }

    public void hideGridLines(){
        gridPane.setStyle("-fx-grid-lines-visible: false;");
    }
}
