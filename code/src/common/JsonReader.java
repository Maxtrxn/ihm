package src.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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


    public static void saveJsonObject(JSONObject object, String filePathFromResources){
        try (FileWriter file = new FileWriter("../resources/" + filePathFromResources)) {
            file.write(object.toString(4));
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
