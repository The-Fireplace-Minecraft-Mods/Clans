package the_fireplace.clans.io;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.io.File;
import java.util.UUID;

public class JsonReader
{
    protected final JsonObject obj;

    protected JsonReader(JsonObject obj) {
        this.obj = obj;
    }

    public static JsonReader create(JsonObject obj) {
        return new JsonReader(obj);
    }

    @Nullable
    public static JsonReader create(File file) {
        JsonObject obj = FileToJsonObject.readJsonFile(file);
        if (obj == null) {
            return null;
        }
        return new JsonReader(obj);
    }

    public JsonObject getJsonObject() {
        return obj;
    }

    @Nullable
    public UUID readUUID(String key, @Nullable UUID ifAbsent) {
        return obj.has(key) ? UUID.fromString(obj.get(key).getAsString()) : ifAbsent;
    }

    public String readString(String key, String ifAbsent) {
        return obj.has(key) ? obj.get(key).getAsString() : ifAbsent;
    }

    public long readLong(String key, long ifAbsent) {
        return obj.has(key) ? obj.get(key).getAsLong() : ifAbsent;
    }

    public int readInt(String key, int ifAbsent) {
        return obj.has(key) ? obj.get(key).getAsInt() : ifAbsent;
    }

    public double readDouble(String key, double ifAbsent) {
        return obj.has(key) ? obj.get(key).getAsDouble() : ifAbsent;
    }

    public float readFloat(String key, float ifAbsent) {
        return obj.has(key) ? obj.get(key).getAsFloat() : ifAbsent;
    }

    public boolean readBool(String key, boolean ifAbsent) {
        return obj.has(key) ? obj.get(key).getAsBoolean() : ifAbsent;
    }

    public JsonArray readArray(String key) {
        return obj.has(key) ? obj.get(key).getAsJsonArray() : new JsonArray();
    }
}
