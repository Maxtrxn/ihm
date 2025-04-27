package src.view.editor.gameEditorSubView;

import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import src.view.editor.GameEditorView;
import javafx.geometry.Pos;
import src.view.editor.gameEditorSubView.MapEditor;

public class MapEditorSettings extends HBox{
    private GameEditorView parent;
    private VBox shownLayerSelection;

    public MapEditorSettings(GameEditorView parent){
        super(10);
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("steampunk-hbox");

        this.parent = parent;
        this.shownLayerSelection = new VBox();
        this.getChildren().add(this.shownLayerSelection);

        ToggleGroup rbGroup = new ToggleGroup();
        for (int i = 0; i <= 4; i++) {
            String rbText;
            switch (i) {
                case 0:
                    rbText = "Afficher seulement la couche du fond d'écran";
                    break;
                case 1:
                    rbText = "Afficher seulement la couche derrière la principale";
                    break;
                case 2:
                    rbText = "Afficher seulement la couche principale";
                    break;
                case 3:
                    rbText = "Afficher seulement la couche devant la principale";
                    break;
                default:
                    rbText = "Afficher toutes les couches";
                    break;
            }
            RadioButton rb = new RadioButton(rbText);
            rb.getStyleClass().add("steampunk-radio");
            rb.setToggleGroup(rbGroup);
            rb.setUserData(i); // associe la valeur 0-4
            this.shownLayerSelection.getChildren().add(rb);
        }

        // Mise à jour du label lorsqu'une sélection change
        rbGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                this.parent.updateVisibleLayer((int)newToggle.getUserData());
            }
        });

        ToggleButton gridLinesVisible = new ToggleButton("Masquer le cadrillage");
        gridLinesVisible.setOnAction(e -> {
            if (gridLinesVisible.isSelected()) {
                ((MapEditor)this.parent.getCenter()).hideGridLines();
                gridLinesVisible.setText("Afficher le cadrillage");
            } else {
                ((MapEditor)this.parent.getCenter()).showGridLines();
                gridLinesVisible.setText("Masquer le cadrillage");
            }
        });
        this.getChildren().add(gridLinesVisible);
    }
}
