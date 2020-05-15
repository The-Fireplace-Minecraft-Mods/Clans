package the_fireplace.clans.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.logic.PlayerEventLogic;
import the_fireplace.clans.model.TerritoryDisplayMode;
import the_fireplace.clans.util.JsonHelper;

import javax.annotation.Nullable;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

public final class PlayerData {
    private static final HashMap<UUID, PlayerStoredData> playerData = Maps.newHashMap();
    public static final File playerDataLocation = new File(Clans.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "clans/player");

    @Nullable
    public static UUID getDefaultClan(UUID player) {
        return getPlayerData(player).defaultClan;
    }

    public static int getCooldown(UUID player) {
        return getPlayerData(player).cooldown;
    }

    public static List<UUID> getInvites(UUID player) {
        return Collections.unmodifiableList(getPlayerData(player).invites);
    }

    public static List<UUID> getBlockedClans(UUID player) {
        return Collections.unmodifiableList(getPlayerData(player).blockedClans);
    }

    public static boolean getIsBlockingAllInvites(UUID player) {
        return getPlayerData(player).inviteBlock;
    }

    public static int getRaidWins(UUID player) {
        return getPlayerData(player).raidWins;
    }

    public static int getRaidLosses(UUID player) {
        return getPlayerData(player).raidLosses;
    }

    public static double getRaidWLR(UUID player) {
        return ((double) getRaidWins(player))/((double) getRaidLosses(player));
    }

    public static TerritoryDisplayMode getTerritoryDisplayMode(UUID player) {
        return getPlayerData(player).territoryDisplayMode;
    }

    public static boolean showUndergroundMessages(UUID player) {
        return getPlayerData(player).showUndergroundMessages;
    }

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

    /**
     * Adds an invite from a clan to a player's data.
     * @return true if the player did not already have an invite pending from that clan, false otherwise.
     */
    public static boolean addInvite(UUID player, UUID clan) {
        ClanCache.cacheInvite(clan, player);
        return getPlayerData(player).addInvite(clan);
    }

    /**
     * Removes an invite from a clan from a player's data.
     * @return true if the invite was removed, or false if they didn't have a pending invite from the specified clan.
     */
    public static boolean removeInvite(UUID player, UUID clan) {
        ClanCache.uncacheInvite(clan, player);
        return getPlayerData(player).removeInvite(clan);
    }

    /**
     * Adds an invite block for a clan to a player's data. This also deletes any existing invites from the clan being blocked.
     * @return true if the player did not already block that clan, false otherwise.
     */
    public static boolean addInviteBlock(UUID player, UUID clan) {
        boolean ret = getPlayerData(player).addInviteBlock(clan);
        if(ret)
            removeInvite(player, clan);
        return ret;
    }

    /**
     * Removes an invite block for a clan from a player's data.
     * @return true if the clan was unblocked, or false if the specified clan wasn't blocked.
     */
    public static boolean removeInviteBlock(UUID player, UUID clan) {
        return getPlayerData(player).removeInviteBlock(clan);
    }

    public static void setGlobalInviteBlock(UUID player, boolean block) {
        getPlayerData(player).setGlobalInviteBlock(block);
    }

    public static void incrementRaidLosses(UUID player) {
        getPlayerData(player).incrementRaidLosses();
    }

    public static void incrementRaidWins(UUID player) {
        getPlayerData(player).incrementRaidWins();
    }

    public static void setTerritoryDisplayMode(UUID player, TerritoryDisplayMode mode) {
        getPlayerData(player).setTerritoryDisplayMode(mode);
    }

    public static void setShowUndergroundMessages(UUID player, boolean showUndergroundMessages) {
        getPlayerData(player).setShowUndergroundMessages(showUndergroundMessages);
    }

    public static void setShouldDisposeReferences(UUID player, boolean shouldDisposeReferences) {
        getPlayerData(player).shouldDisposeReferences = shouldDisposeReferences;
    }

    private static PlayerStoredData getPlayerData(UUID player) {
        if(!playerData.containsKey(player))
            playerData.put(player, new PlayerStoredData(player));
        return playerData.get(player);
    }

    public static void save() {
        for(Map.Entry<UUID, PlayerStoredData> entry : Sets.newHashSet(playerData.entrySet())) {
            entry.getValue().save();
            if(entry.getValue().shouldDisposeReferences)
                playerData.remove(entry.getKey());
        }
    }

    private static class PlayerStoredData {
        private final File playerDataFile;
        private boolean isChanged, saving, shouldDisposeReferences = false;

        @Nullable
        private UUID defaultClan;
        private int cooldown, raidWins, raidLosses;
        private boolean inviteBlock, showUndergroundMessages;
        private List<UUID> invites, blockedClans;
        private TerritoryDisplayMode territoryDisplayMode;

        private Map<String, Object> addonData = Maps.newHashMap();

        private PlayerStoredData(UUID playerId) {
            playerDataFile = new File(playerDataLocation, playerId.toString()+".json");
            if(!load()) {
                invites = Lists.newArrayList();
                blockedClans = Lists.newArrayList();
                inviteBlock = false;
                raidWins = raidLosses = 0;
                PlayerEventLogic.onFirstLogin(playerId);
            }
            //If the player is offline, we should remove references so garbage collection can clean it up when the data is done being used.
            //noinspection ConstantConditions
            if(Clans.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(playerId) == null)
                shouldDisposeReferences = true;
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
                    invites = jsonObject.has("invites") ? JsonHelper.uuidListFromJsonArray(jsonObject.getAsJsonArray("invites")) : Lists.newArrayList();
                    blockedClans = jsonObject.has("blockedClans") ? JsonHelper.uuidListFromJsonArray(jsonObject.getAsJsonArray("blockedClans")) : Lists.newArrayList();
                    inviteBlock = jsonObject.has("inviteBlock") && jsonObject.getAsJsonPrimitive("inviteBlock").getAsBoolean();
                    raidWins = jsonObject.has("raidKills") ? jsonObject.getAsJsonPrimitive("raidKills").getAsInt() : 0;
                    raidLosses = jsonObject.has("raidDeaths") ? jsonObject.getAsJsonPrimitive("raidDeaths").getAsInt() : 0;
                    territoryDisplayMode = jsonObject.has("territoryDisplayMode") ? TerritoryDisplayMode.valueOf(jsonObject.getAsJsonPrimitive("territoryDisplayMode").getAsString()) : TerritoryDisplayMode.ACTION_BAR;
                    showUndergroundMessages = !jsonObject.has("showUndergroundMessages") || jsonObject.getAsJsonPrimitive("showUndergroundMessages").getAsBoolean();
                    addonData = JsonHelper.getAddonData(jsonObject);
                    return true;
                }
            } catch (FileNotFoundException ignored) {
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private void save() {
            if(!isChanged || saving)
                return;
            saving = true;
            new Thread(() -> {
                JsonObject obj = new JsonObject();
                if (defaultClan != null)
                    obj.addProperty("defaultClan", defaultClan.toString());
                obj.addProperty("cooldown", cooldown);
                obj.add("invites", JsonHelper.toJsonArray(invites));
                obj.add("blockedClans", JsonHelper.toJsonArray(blockedClans));
                obj.addProperty("inviteBlock", inviteBlock);
                obj.addProperty("raidKills", raidWins);
                obj.addProperty("raidDeaths", raidLosses);
                obj.addProperty("territoryDisplayMode", territoryDisplayMode.toString());
                obj.addProperty("showUndergroundMessages", showUndergroundMessages);

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

        public boolean addInvite(UUID clan) {
            boolean ret = !invites.contains(clan);
            invites.add(clan);
            isChanged = ret;
            return ret;
        }

        public boolean removeInvite(UUID clan) {
            boolean ret = invites.remove(clan);
            isChanged = ret;
            return ret;
        }

        public boolean addInviteBlock(UUID clan) {
            boolean ret = !blockedClans.contains(clan);
            blockedClans.add(clan);
            isChanged = ret;
            return ret;
        }

        public boolean removeInviteBlock(UUID clan) {
            boolean ret = blockedClans.remove(clan);
            isChanged = ret;
            return ret;
        }

        public void setGlobalInviteBlock(boolean inviteBlock) {
            if(this.inviteBlock != inviteBlock) {
                this.inviteBlock = inviteBlock;
                isChanged = true;
            }
        }

        public void incrementRaidWins() {
            raidWins++;
            isChanged = true;
        }

        public void incrementRaidLosses() {
            raidLosses++;
            isChanged = true;
        }

        public void setTerritoryDisplayMode(TerritoryDisplayMode mode) {
            if(mode != territoryDisplayMode) {
                territoryDisplayMode = mode;
                isChanged = true;
            }
        }

        public void setShowUndergroundMessages(boolean show) {
            if(showUndergroundMessages != show) {
                showUndergroundMessages = show;
                isChanged = true;
            }
        }

        /**
         * Sets addon data for this player
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
    }
}
