package the_fireplace.clans.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import javax.annotation.Nullable;
import java.io.*;

public final class FileToJsonObject
{
    @Nullable
    public static JsonObject readJsonFile(File file) {
        JsonParser jsonParser = new JsonParser();
        try (BufferedReader br = new BufferedReader(new FileReader(file), Short.MAX_VALUE)) {
            JsonElement jsonElement = jsonParser.parse(br);
            if (jsonElement instanceof JsonObject) {
                return (JsonObject) jsonElement;
            }
        } catch (FileNotFoundException ignored) {
        } catch (IOException | JsonParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
