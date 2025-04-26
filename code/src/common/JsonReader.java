// code/src/common/JsonReader.java
package src.common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import javafx.application.Platform;
import org.json.JSONObject;

public class JsonReader {
    public static JSONObject getJsonObjectContent(String resourcePath) {
        try (InputStream in = JsonReader.class
                 .getClassLoader()
                 .getResourceAsStream(resourcePath)) {
            if (in == null) {
                System.err.println("Ressource introuvable: " + resourcePath);
                Platform.exit();
                return null;
            }
            String json = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));
            return new JSONObject(json);
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
            return null;
        }
    }
}
