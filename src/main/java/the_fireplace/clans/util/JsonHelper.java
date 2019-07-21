package the_fireplace.clans.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonHelper {
    public static void attachAddonData(JsonObject obj, HashMap<String, Object> addonDataMap) {
        Gson gson = new Gson();
        JsonArray addonData = new JsonArray();
        for(Map.Entry<String, Object> entry : addonDataMap.entrySet()) {
            JsonObject newEntry = new JsonObject();
            newEntry.addProperty("key", entry.getKey());
            newEntry.add("value", gson.toJsonTree(entry.getValue()));
        }
        obj.add("addonData", addonData);
    }

    public static HashMap<String, Object> getAddonData(JsonObject obj) {
        HashMap<String, Object> addonDataMap = Maps.newHashMap();
        if(!obj.has("addonData"))
            return addonDataMap;

        for(JsonElement entry : obj.getAsJsonArray("addonData")) {
            JsonObject e = (JsonObject) entry;
            String key = e.getAsJsonPrimitive("key").getAsString();
            addonDataMap.put(key, deserialize(e.get("value")));
        }

        return addonDataMap;
    }

    @Nullable
    private static Object deserialize(JsonElement e) {
        if(e.isJsonPrimitive())
            return toObject(e.getAsJsonPrimitive());
        if(e.isJsonArray())
            return toList(e.getAsJsonArray());
        if(e.isJsonObject())
            return toMap(e.getAsJsonObject());
        return null;
    }

    @Nullable
    private static Object toObject(JsonPrimitive jsonPrimitive) {
        if (jsonPrimitive.isBoolean())
            return jsonPrimitive.getAsBoolean();
        if (jsonPrimitive.isString())
            return jsonPrimitive.getAsString();
        if (jsonPrimitive.isNumber()) {
            BigDecimal num = jsonPrimitive.getAsBigDecimal();
            int index = num.toString().indexOf('.');
            if (index == -1) {
                if (num.compareTo(new BigDecimal(Long.MAX_VALUE)) > 0 || num.compareTo(new BigDecimal(Long.MIN_VALUE)) < 0)
                    return num;
                if (num.compareTo(new BigDecimal(Integer.MAX_VALUE)) > 0 || num.compareTo(new BigDecimal(Integer.MIN_VALUE)) < 0)
                    return jsonPrimitive.getAsLong();
                return jsonPrimitive.getAsInt();
            }
            double dvalue = jsonPrimitive.getAsDouble();
            float fvalue = jsonPrimitive.getAsFloat();
            if (String.valueOf(dvalue).equals(String.valueOf(fvalue)))
                return fvalue;
            return dvalue;
        }
        if (jsonPrimitive.isJsonArray())
            return toList(jsonPrimitive.getAsJsonArray());
        if (jsonPrimitive.isJsonObject())
            return toMap(jsonPrimitive.getAsJsonObject());
        return null;
    }

    private static List<Object> toList(JsonArray json){
        List<Object> list = Lists.newArrayList();
        for (int i = 0; i < json.size(); i++) {
            Object value = json.get(i);
            if (value instanceof JsonArray)
                list.add(toList((JsonArray) value));
            else if (value instanceof JsonObject)
                list.add(toMap((JsonObject) value));
            else if (value instanceof JsonPrimitive)
                list.add(toObject((JsonPrimitive) value));
            else
                list.add(value);
        }
        return list;
    }

    private static Map<String, Object> toMap(JsonObject json){
        Map<String, Object> map = Maps.newHashMap();
        Set<Map.Entry<String, JsonElement>> entrySet = json.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value instanceof JsonArray)
                map.put(key, toList((JsonArray) value));
            else if (value instanceof JsonObject)
                map.put(key, toMap((JsonObject) value));
            else if (value instanceof JsonPrimitive)
                map.put(key, toObject((JsonPrimitive) value));
            else
                map.put(key, value);
        }
        return map;
    }
}
