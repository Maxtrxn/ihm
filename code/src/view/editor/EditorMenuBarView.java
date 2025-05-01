package src.view.editor;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import src.common.ResourceManager;
import src.controller.editor.EditorMenuBarController;

public class EditorMenuBarView extends MenuBar{
    private Map<String, MenuItem> items;
    EditorMenuBarController controller;
    

    public EditorMenuBarView(EditorMenuBarController controller){
        super();
        this.controller = controller;
        this.items = new HashMap<>();

        //Création du menu "Fichier"
        Menu fileMenu = new Menu(ResourceManager.getString("EditorMenuBarView_fileMenu"));
        MenuItem File_NewLevel = new MenuItem(ResourceManager.getString("EditorMenuBarView_File_NewLevel"));
        File_NewLevel.setOnAction(e -> this.controller.handleFileNewLevel());
        MenuItem File_OpenLevel = new MenuItem(ResourceManager.getString("EditorMenuBarView_File_OpenLevel"));
        File_OpenLevel.setOnAction(e -> this.controller.handleFileOpenLevel());
        MenuItem File_SaveLevel = new MenuItem(ResourceManager.getString("EditorMenuBarView_File_SaveLevel"));
        File_SaveLevel.setOnAction(e -> this.controller.handleFileSaveLevel());
        MenuItem File_DeleteLevel = new MenuItem(ResourceManager.getString("EditorMenuBarView_File_DeleteLevel"));
        File_DeleteLevel.setOnAction(e -> this.controller.handleFileDeleteLevel());
        MenuItem File_Quit = new MenuItem(ResourceManager.getString("EditorMenuBarView_File_Quit"));
        File_Quit.setOnAction(e -> this.controller.handleFileQuit());
        fileMenu.getItems().addAll(File_NewLevel, File_OpenLevel, File_SaveLevel, File_DeleteLevel, new SeparatorMenuItem(), File_Quit);



        //Création du menu "Niveau"
        Menu levelMenu = new Menu(ResourceManager.getString("EditorMenuBarView_levelMenu"));
        MenuItem Level_ChangeLevelName = new MenuItem(ResourceManager.getString("EditorMenuBarView_Level_ChangeLevelName"));
        Level_ChangeLevelName.setOnAction(e -> this.controller.handleLevelChangeLevelName());
        MenuItem Level_ChangeLevelBackground = new MenuItem(ResourceManager.getString("EditorMenuBarView_Level_ChangeLevelBackground"));
        Level_ChangeLevelBackground.setOnAction(e -> this.controller.handleLevelChangeLevelBackground());
        MenuItem Level_TestLevel = new MenuItem(ResourceManager.getString("EditorMenuBarView_Level_TestLevel"));
        Level_TestLevel.setOnAction(e -> this.controller.handleLevelTestLevel());
        levelMenu.getItems().addAll(Level_ChangeLevelName, Level_ChangeLevelBackground, Level_TestLevel);



        //Création du menu "Préférences"
        Menu preferencesMenu = new Menu(ResourceManager.getString("EditorMenuBarView_preferencesMenu"));
        MenuItem Preference_ChangeEditorTheme = new MenuItem(ResourceManager.getString("EditorMenuBarView_Preference_ChangeEditorTheme"));
        Preference_ChangeEditorTheme.setOnAction(e -> this.controller.handlePreferenceEditorTheme());
        preferencesMenu.getItems().addAll(Preference_ChangeEditorTheme);


        
        //On ajoute les menus à la barre
        this.getMenus().addAll(fileMenu, levelMenu, preferencesMenu);
    }


    public MenuItem getMenuItem(String name){
        return this.items.get(name);
    }
}
