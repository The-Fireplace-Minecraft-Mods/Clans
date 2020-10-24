package the_fireplace.clans.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public interface JsonWritable {
    Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    JsonObject toJson();
    default void writeToJson(File file) {
        try {
            try(FileWriter fw = new FileWriter(file)) {
                fw.write(GSON.toJson(this.toJson()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
