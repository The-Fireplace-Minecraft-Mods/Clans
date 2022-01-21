package the_fireplace.clans.legacy.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;

public class JsonHelper
{
    @Nullable
    private static Object toObject(JsonPrimitive jsonPrimitive) {
        if (jsonPrimitive.isBoolean()) {
            return jsonPrimitive.getAsBoolean();
        }
        if (jsonPrimitive.isString()) {
            return jsonPrimitive.getAsString();
        }
        if (jsonPrimitive.isNumber()) {
            BigDecimal num = jsonPrimitive.getAsBigDecimal();
            int index = num.toString().indexOf('.');
            if (index == -1) {
                if (num.compareTo(new BigDecimal(Long.MAX_VALUE)) > 0 || num.compareTo(new BigDecimal(Long.MIN_VALUE)) < 0) {
                    return num;
                }
                if (num.compareTo(new BigDecimal(Integer.MAX_VALUE)) > 0 || num.compareTo(new BigDecimal(Integer.MIN_VALUE)) < 0) {
                    return jsonPrimitive.getAsLong();
                }
                return jsonPrimitive.getAsInt();
            }
            double dvalue = jsonPrimitive.getAsDouble();
            float fvalue = jsonPrimitive.getAsFloat();
            if (String.valueOf(dvalue).equals(String.valueOf(fvalue))) {
                return fvalue;
            }
            return dvalue;
        }
        if (jsonPrimitive.isJsonArray()) {
            return toList(jsonPrimitive.getAsJsonArray());
        }
        if (jsonPrimitive.isJsonObject()) {
            return toMap(jsonPrimitive.getAsJsonObject());
        }
        return null;
    }

    private static List<Object> toList(JsonArray json) {
        List<Object> list = Lists.newArrayList();
        for (int i = 0; i < json.size(); i++) {
            Object value = json.get(i);
            if (value instanceof JsonArray) {
                list.add(toList((JsonArray) value));
            } else if (value instanceof JsonObject) {
                list.add(toMap((JsonObject) value));
            } else if (value instanceof JsonPrimitive) {
                list.add(toObject((JsonPrimitive) value));
            } else {
                list.add(value);
            }
        }
        return list;
    }

    private static Map<String, Object> toMap(JsonObject json) {
        Map<String, Object> map = Maps.newHashMap();
        Set<Map.Entry<String, JsonElement>> entrySet = json.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value instanceof JsonArray) {
                map.put(key, toList((JsonArray) value));
            } else if (value instanceof JsonObject) {
                map.put(key, toMap((JsonObject) value));
            } else if (value instanceof JsonPrimitive) {
                map.put(key, toObject((JsonPrimitive) value));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    public static JsonObject toJsonObject(BlockPos pos) {
        JsonObject ret = new JsonObject();
        ret.addProperty("x", pos.getX());
        ret.addProperty("y", pos.getY());
        ret.addProperty("z", pos.getZ());

        return ret;
    }

    public static BlockPos fromJsonObject(JsonObject obj) {
        return new BlockPos(obj.get("x").getAsInt(), obj.get("y").getAsInt(), obj.get("z").getAsInt());
    }

    public static JsonArray toJsonArray(Collection<UUID> uuids) {
        JsonArray arr = new JsonArray();
        for (UUID uuid : uuids) {
            arr.add(uuid.toString());
        }
        return arr;
    }

    public static Collection<UUID> uuidsFromJsonArray(JsonArray arr) {
        Collection<UUID> uuids = new HashSet<>();
        for (JsonElement elem : arr) {
            uuids.add(UUID.fromString(elem.getAsString()));
        }
        return Collections.unmodifiableCollection(uuids);
    }
}
