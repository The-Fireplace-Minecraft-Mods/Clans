package the_fireplace.clans.io;

import java.io.File;

public interface JsonReadable
{
    void readFromJson(JsonReader reader);

    default void load(File file) {
        JsonReader reader = JsonReader.create(file);
        if (reader != null) {
            readFromJson(reader);
        }
    }
}
