package src.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import org.json.JSONObject;

public class JsonReader {
    //Méthode pour lire un fichier JSON
    private static String readJsonFile(String filePath) {
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

    public static JSONObject getJsonObjectContent(String filePathFromResources){
        String jsonContent = readJsonFile("../resources/" + filePathFromResources);
        
        if(jsonContent == null){
            javafx.application.Platform.exit();
        }   
        
        return new JSONObject(jsonContent);
    }
}
