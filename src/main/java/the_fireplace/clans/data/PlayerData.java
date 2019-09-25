package the_fireplace.clans.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.logic.PlayerEventLogic;
import the_fireplace.clans.util.JsonHelper;

import javax.annotation.Nullable;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

public final class PlayerData {
    private static HashMap<UUID, PlayerStoredData> playerData = Maps.newHashMap();
    private static final File playerDataLocation = new File(Clans.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "clans/player");

    //region getters
    @Nullable
    public static UUID getDefaultClan(UUID player) {
        return getPlayerData(player).defaultClan;
    }

    public static int getCooldown(UUID player) {
        return getPlayerData(player).cooldown;
    }

    //endregion

    //region saved data setters
    public static void setDefaultClan(UUID player, @Nullable UUID defaultClan) {
        getPlayerData(player).setDefaultClan(defaultClan);
    }

    /**
     * Check if a clan is the player's default clan, and if it is, update the player's default clan to something else.
     * @param player
     * The player to check and update (if needed)
     * @param removeClan
     * The clan the player is being removed from. Use null to forcibly change the player's default clan, regardless of what it currently is.
     */
    public static void updateDefaultClan(UUID player, @Nullable UUID removeClan) {
        UUID oldDef = getDefaultClan(player);
        if(removeClan == null || removeClan.equals(oldDef))
            if(ClanCache.getPlayerClans(player).isEmpty())
                setDefaultClan(player, null);
            else
                setDefaultClan(player, ClanCache.getPlayerClans(player).get(0).getId());
    }

    public static void setCooldown(UUID player, int cooldown) {
        getPlayerData(player).setCooldown(cooldown);
    }
    //endregion

    public static void setShouldDisposeReferences(UUID player, boolean shouldDisposeReferences) {
        getPlayerData(player).shouldDisposeReferences = shouldDisposeReferences;
    }
    //endregion

    //region getPlayerData
    private static PlayerStoredData getPlayerData(UUID player) {
        if(!playerData.containsKey(player))
            playerData.put(player, new PlayerStoredData(player));
        return playerData.get(player);
    }
    //endregion

    //region save
    public static void save() {
        for(Map.Entry<UUID, PlayerStoredData> entry : Sets.newHashSet(playerData.entrySet())) {
            entry.getValue().save();
            if(entry.getValue().shouldDisposeReferences)
                playerData.remove(entry.getKey());
        }
    }
    //endregion

    private static class PlayerStoredData {
        //region Internal variables
        private File playerDataFile;
        private boolean isChanged, saving, shouldDisposeReferences = false;
        //endregion

        //region Saved variables
        @Nullable
        private UUID defaultClan;
        private int cooldown;

        private Map<String, Object> addonData = Maps.newHashMap();
        //endregion

        //region Constructor
        private PlayerStoredData(UUID playerId) {
            playerDataFile = new File(playerDataLocation, playerId.toString()+".json");
            if(!load())
                PlayerEventLogic.onFirstLogin(playerId);
        }
        //endregion

        //region load

        /**
         * @return true if it loaded from a file successfully, false otherwise.
         */
        private boolean load() {
            if(!playerDataLocation.exists()) {
                playerDataLocation.mkdirs();
                return false;
            }

            JsonParser jsonParser = new JsonParser();
            try {
                Object obj = jsonParser.parse(new FileReader(playerDataFile));
                if(obj instanceof JsonObject) {
                    JsonObject jsonObject = (JsonObject) obj;
                    defaultClan = jsonObject.has("defaultClan") ? UUID.fromString(jsonObject.getAsJsonPrimitive("defaultClan").getAsString()) : null;
                    cooldown = jsonObject.has("cooldown") ? jsonObject.getAsJsonPrimitive("cooldown").getAsInt() : 0;
                    addonData = JsonHelper.getAddonData(jsonObject);
                    return true;
                }
            } catch (FileNotFoundException ignored) {
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
        //endregion

        //region save
        private void save() {
            if(!isChanged || saving)
                return;
            saving = true;
            new Thread(() -> {
                JsonObject obj = new JsonObject();
                if (defaultClan != null)
                    obj.addProperty("defaultClan", defaultClan.toString());
                obj.addProperty("cooldown", cooldown);

                JsonHelper.attachAddonData(obj, this.addonData);

                try {
                    FileWriter file = new FileWriter(playerDataFile);
                    file.write(new GsonBuilder().setPrettyPrinting().create().toJson(obj));
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                saving = isChanged = false;
            }).start();
        }
        //endregion

        //region setters
        public void setDefaultClan(@Nullable UUID defaultClan) {
            if(!Objects.equals(this.defaultClan, defaultClan)) {
                this.defaultClan = defaultClan;
                isChanged = true;
            }
        }

        public void setCooldown(int cooldown) {
            if(!Objects.equals(this.cooldown, cooldown)) {
                this.cooldown = cooldown;
                isChanged = true;
            }
        }
        //endregion

        //region Addon Data
        /**
         * Sets addon data for this chunk
         * @param key
         * The key you are giving this data. It should be unique
         * @param value
         * The data itself. This should be a primitive, string, a list or map containg only lists/maps/primitives/strings, or a JsonElement. If not, your data may not save/load properly. All lists will be loaded as ArrayLists. All maps will be loaded as HashMaps.
         */
        public void setCustomData(String key, Object value) {
            if(!value.getClass().isPrimitive() && !value.getClass().isAssignableFrom(BigDecimal.class) && !value.getClass().isAssignableFrom(List.class) && !value.getClass().isAssignableFrom(Map.class) && !value.getClass().isAssignableFrom(JsonElement.class))
                Clans.getMinecraftHelper().getLogger().warn("Custom data may not be properly saved and loaded, as it is not assignable from any supported json deserialization. Key: {}, Value: {}", key, value);
            addonData.put(key, value);
            isChanged = true;
        }

        @Nullable
        public Object getCustomData(String key) {
            return addonData.get(key);
        }
        //endregion
    }
}
