package src.view.editor;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import src.controller.editor.EditorMenuBarController;

public class EditorMenuBarView extends MenuBar{
    private Map<String, MenuItem> items;
    EditorMenuBarController controller;
    

    public EditorMenuBarView(EditorMenuBarController controller){
        super();
        this.controller = controller;
        this.items = new HashMap<>();

        //Création du menu "Fichier"
        Menu fileMenu = new Menu("Fichier");
        MenuItem File_NewLevel = new MenuItem("Nouveau niveau");
        File_NewLevel.setOnAction(e -> this.controller.handleFileNewLevel());
        MenuItem File_OpenLevel = new MenuItem("Ouvrir niveau");
        File_OpenLevel.setOnAction(e -> this.controller.handleFileOpenLevel());
        MenuItem File_SaveLevel = new MenuItem("Enregistrer");
        File_SaveLevel.setOnAction(e -> this.controller.handleFileSaveLevel());
        MenuItem File_DeleteLevel = new MenuItem("Supprimer un niveau");
        File_DeleteLevel.setOnAction(e -> this.controller.handleFileDeleteLevel());
        MenuItem File_Quit = new MenuItem("Quitter l'éditeur");
        File_Quit.setOnAction(e -> this.controller.handleFileQuit());
        fileMenu.getItems().addAll(File_NewLevel, File_OpenLevel, File_SaveLevel, File_DeleteLevel, new SeparatorMenuItem(), File_Quit);



        //Création du menu "Niveau"
        Menu levelMenu = new Menu("Niveau");
        MenuItem Level_ChangeLevelName = new MenuItem("Changer le nom du niveau");
        Level_ChangeLevelName.setOnAction(e -> this.controller.handleLevelChangeLevelName());
        MenuItem Level_ChangeLevelBackground = new MenuItem("Choisir une image de fond");
        Level_ChangeLevelBackground.setOnAction(e -> this.controller.handleLevelChangeLevelBackground());
        MenuItem Level_TestLevel = new MenuItem("Tester le niveau");
        Level_TestLevel.setOnAction(e -> this.controller.handleLevelTestLevel());
        levelMenu.getItems().addAll(Level_ChangeLevelName, Level_ChangeLevelBackground, Level_TestLevel);



        //Création du menu "Préférences"
        Menu preferencesMenu = new Menu("Préférences");
        MenuItem Preference_ChangeEditorTheme = new MenuItem("Changer le thème de l'éditeur");
        Preference_ChangeEditorTheme.setOnAction(e -> this.controller.handlePreferenceEditorTheme());
        MenuItem Preference_ChangeEditorLanguage = new MenuItem("Changer la langue de l'éditeur");
        Preference_ChangeEditorLanguage.setOnAction(e -> this.controller.handlePreferenceEditorLanguage());
        preferencesMenu.getItems().addAll(Preference_ChangeEditorTheme, Preference_ChangeEditorLanguage);


        
        //On ajoute les menus à la barre
        this.getMenus().addAll(fileMenu, levelMenu, preferencesMenu);
    }


    public MenuItem getMenuItem(String name){
        return this.items.get(name);
    }
}
