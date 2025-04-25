package src.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import org.json.JSONObject;

public class JsonReader {
    public JsonReader(){
    }
    
    //Méthode pour lire un fichier JSON
    private String readJsonFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try {
            File file = new File(filePath);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine());
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return content.toString();
    }

    public JSONObject getJsonObjectContent(String filePathFromResources){
        String jsonContent = readJsonFile(getClass().getResource(filePathFromResources).getPath());
        if(jsonContent == null){
            javafx.application.Platform.exit();
        }

        return new JSONObject(jsonContent);
    }
}
