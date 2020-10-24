package the_fireplace.clans.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public interface JsonWritable {
    Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    JsonObject toJson();
    default void writeToJson(File file) {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(file), Short.MAX_VALUE)) {
            GSON.toJson(this.toJson(), bw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
