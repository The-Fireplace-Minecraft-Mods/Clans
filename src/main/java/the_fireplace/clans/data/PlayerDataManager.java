package the_fireplace.clans.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.util.JsonHelper;

import javax.annotation.Nullable;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

public final class PlayerDataManager {

    private static HashMap<UUID, PlayerData> playerData = Maps.newHashMap();
    private static final File playerDataLocation = new File(Clans.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "clans/player");

    //region getters
    @Nullable
    public static UUID getDefaultClan(UUID player) {
        return getPlayerData(player).defaultClan;
    }

    public static int getCooldown(UUID player) {
        return getPlayerData(player).cooldown;
    }

    @Nullable
    public static UUID getPreviousChunkOwner(UUID player) {
        return getPlayerData(player).prevChunkOwner;
    }

    public static boolean getStoredIsInBorderland(UUID player) {
        return getPlayerData(player).isInBorderland;
    }

    public static boolean getClaimWarning(UUID player) {
        return getPlayerData(player).claimWarning;
    }

    public static int getPreviousY(UUID player) {
        return getPlayerData(player).prevY;
    }

    public static int getPreviousChunkX(UUID player) {
        return getPlayerData(player).prevChunkX;
    }

    public static int getPreviousChunkZ(UUID player) {
        return getPlayerData(player).prevChunkZ;
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
                setDefaultClan(player, ClanCache.getPlayerClans(player).get(0).getClanId());
    }

    public static void setCooldown(UUID player, int cooldown) {
        getPlayerData(player).setCooldown(cooldown);
    }
    //endregion

    //region cached data setters
    public static void setPreviousChunkOwner(UUID player, @Nullable UUID prevChunkOwner, boolean isBorderland) {
        PlayerData data = getPlayerData(player);
        data.prevChunkOwner = prevChunkOwner;
        data.isInBorderland = isBorderland;
    }

    public static void setClaimWarning(UUID player, boolean claimWarning) {
        getPlayerData(player).claimWarning = claimWarning;
    }

    public static void setPreviousY(UUID player, int prevY) {
        getPlayerData(player).prevY = prevY;
    }

    public static void setPreviousChunkX(UUID player, int prevChunkX) {
        getPlayerData(player).prevChunkX = prevChunkX;
    }

    public static void setPreviousChunkZ(UUID player, int prevChunkZ) {
        getPlayerData(player).prevChunkZ = prevChunkZ;
    }

    public static void setShouldDisposeReferences(UUID player, boolean shouldDisposeReferences) {
        getPlayerData(player).shouldDisposeReferences = shouldDisposeReferences;
    }
    //endregion

    //region getPlayerData
    private static PlayerData getPlayerData(UUID player) {
        if(!playerData.containsKey(player))
            playerData.put(player, new PlayerData(player));
        return playerData.get(player);
    }
    //endregion

    //region save
    public static void save() {
        for(Map.Entry<UUID, PlayerData> entry : Sets.newHashSet(playerData.entrySet())) {
            entry.getValue().save();
            if(entry.getValue().shouldDisposeReferences)
                playerData.remove(entry.getKey());
        }
    }
    //endregion

    private static class PlayerData {
        //region Internal variables
        private File playerDataFile;
        private boolean isChanged, saving, shouldDisposeReferences = false;
        //endregion

        //region Cache variables
        @Nullable
        private UUID prevChunkOwner;
        private boolean claimWarning, isInBorderland;
        private int prevY, prevChunkX, prevChunkZ;
        //endregion

        //region Saved variables
        @Nullable
        private UUID defaultClan;
        private int cooldown;

        private HashMap<String, Object> addonData = Maps.newHashMap();
        //endregion

        //region Constructor
        private PlayerData(UUID player) {
            playerDataFile = new File(playerDataLocation, player.toString()+".json");
            load();
        }
        //endregion

        //region load
        private void load() {
            if(!playerDataLocation.exists()) {
                playerDataLocation.mkdirs();
                return;
            }

            JsonParser jsonParser = new JsonParser();
            try {
                Object obj = jsonParser.parse(new FileReader(playerDataFile));
                if(obj instanceof JsonObject) {
                    JsonObject jsonObject = (JsonObject) obj;
                    defaultClan = jsonObject.has("defaultClan") ? UUID.fromString(jsonObject.getAsJsonPrimitive("defaultClan").getAsString()) : null;
                    cooldown = jsonObject.has("cooldown") ? jsonObject.getAsJsonPrimitive("cooldown").getAsInt() : 0;
                    addonData = JsonHelper.getAddonData(jsonObject);
                }
            } catch (FileNotFoundException e) {
                //do nothing, it just hasn't been created yet
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //endregion

        //region save
        private void save() {
            if(!isChanged || saving)
                return;
            saving = true;
            new Thread(() -> {
                JsonObject obj = new JsonObject();
                if(defaultClan != null)
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
            }).run();
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
