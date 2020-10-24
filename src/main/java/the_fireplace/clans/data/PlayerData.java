package the_fireplace.clans.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.util.internal.ConcurrentSet;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.io.JsonReader;
import the_fireplace.clans.io.JsonWritable;
import the_fireplace.clans.logic.PlayerEventLogic;
import the_fireplace.clans.model.TerritoryDisplayMode;
import the_fireplace.clans.multithreading.ThreadedSaveHandler;
import the_fireplace.clans.multithreading.ThreadedSaveable;
import the_fireplace.clans.util.JsonHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerData {
    private static final Map<UUID, PlayerStoredData> playerData = new ConcurrentHashMap<>();
    public static final File playerDataLocation = new File(Clans.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "clans/player");

    @Nullable
    public static UUID getDefaultClan(UUID player) {
        return getPlayerData(player).defaultClan;
    }

    public static int getCooldown(UUID player) {
        return getPlayerData(player).cooldown;
    }

    public static Collection<UUID> getInvites(UUID player) {
        return Collections.unmodifiableCollection(getPlayerData(player).invites);
    }

    public static Collection<UUID> getBlockedClans(UUID player) {
        return Collections.unmodifiableCollection(getPlayerData(player).blockedClans);
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

    public static long getLastSeen(UUID player) {
        return getPlayerData(player).lastSeen;
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
                setDefaultClan(player, ClanCache.getPlayerClans(player).stream().findAny().get().getId());
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

    public static void updateLastSeen(UUID player) {
        getPlayerData(player).updateLastSeen();
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
        for(Map.Entry<UUID, PlayerStoredData> entry : playerData.entrySet()) {
            entry.getValue().save();
            if(entry.getValue().shouldDisposeReferences) {
                playerData.remove(entry.getKey());
                entry.getValue().getSaveHandler().disposeReferences();
            }
        }
    }

    private static class PlayerStoredData implements ThreadedSaveable, JsonWritable {
        private final File playerDataFile;
        private final ThreadedSaveHandler<PlayerStoredData> saveHandler = ThreadedSaveHandler.create(this);
        private boolean shouldDisposeReferences = false;

        @Nullable
        private UUID defaultClan;
        private int cooldown, raidWins, raidLosses;
        private boolean inviteBlock, showUndergroundMessages;
        private final Set<UUID> invites = new ConcurrentSet<>(), blockedClans = new ConcurrentSet<>();
        private TerritoryDisplayMode territoryDisplayMode;
        private long lastSeen;

        private Map<String, Object> addonData = new ConcurrentHashMap<>();

        private PlayerStoredData(UUID playerId) {
            playerDataFile = new File(playerDataLocation, playerId.toString()+".json");
            if(!load()) {
                inviteBlock = false;
                raidWins = raidLosses = 0;
                territoryDisplayMode = TerritoryDisplayMode.ACTION_BAR;
                lastSeen = System.currentTimeMillis();
                PlayerEventLogic.onFirstLogin(playerId);
            }
            //If the player is offline, we should remove references so garbage collection can clean it up when the data is done being used.
            //noinspection ConstantConditions
            if(Clans.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(playerId) == null) {
                shouldDisposeReferences = true;
                saveHandler.disposeReferences();
            }
        }

        /**
         * @return true if it loaded from a file successfully, false otherwise.
         */
        private boolean load() {
            if(!playerDataLocation.exists()) {
                playerDataLocation.mkdirs();
                return false;
            }

            JsonObject jsonObject = JsonReader.readJson(playerDataFile);
            if(jsonObject == null)
                return false;
            defaultClan = jsonObject.has("defaultClan") ? UUID.fromString(jsonObject.getAsJsonPrimitive("defaultClan").getAsString()) : null;
            cooldown = jsonObject.has("cooldown") ? jsonObject.getAsJsonPrimitive("cooldown").getAsInt() : 0;
            if(jsonObject.has("invites"))
                invites.addAll(JsonHelper.uuidsFromJsonArray(jsonObject.getAsJsonArray("invites")));
            if(jsonObject.has("blockedClans"))
                blockedClans.addAll(JsonHelper.uuidsFromJsonArray(jsonObject.getAsJsonArray("blockedClans")));
            inviteBlock = jsonObject.has("inviteBlock") && jsonObject.getAsJsonPrimitive("inviteBlock").getAsBoolean();
            raidWins = jsonObject.has("raidKills") ? jsonObject.getAsJsonPrimitive("raidKills").getAsInt() : 0;
            raidLosses = jsonObject.has("raidDeaths") ? jsonObject.getAsJsonPrimitive("raidDeaths").getAsInt() : 0;
            territoryDisplayMode = jsonObject.has("territoryDisplayMode") ? TerritoryDisplayMode.valueOf(jsonObject.getAsJsonPrimitive("territoryDisplayMode").getAsString()) : TerritoryDisplayMode.ACTION_BAR;
            showUndergroundMessages = !jsonObject.has("showUndergroundMessages") || jsonObject.getAsJsonPrimitive("showUndergroundMessages").getAsBoolean();
            lastSeen = jsonObject.has("lastSeen") ? jsonObject.getAsJsonPrimitive("lastSeen").getAsLong() : 0;
            addonData = JsonHelper.getAddonData(jsonObject);
            return true;
        }

        @Override
        public JsonObject toJson() {
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
            obj.addProperty("lastSeen", lastSeen);

            JsonHelper.attachAddonData(obj, this.addonData);

            return obj;
        }

        @Override
        public void blockingSave() {
            writeToJson(playerDataFile);
        }

        @Override
        public ThreadedSaveHandler<?> getSaveHandler() {
            return saveHandler;
        }

        public void setDefaultClan(@Nullable UUID defaultClan) {
            if(!Objects.equals(this.defaultClan, defaultClan)) {
                this.defaultClan = defaultClan;
                markChanged();
            }
        }

        public void setCooldown(int cooldown) {
            if(!Objects.equals(this.cooldown, cooldown)) {
                this.cooldown = cooldown;
                markChanged();
            }
        }

        public boolean addInvite(UUID clan) {
            boolean ret = invites.add(clan);
            if(ret)
                markChanged();
            return ret;
        }

        public boolean removeInvite(UUID clan) {
            boolean ret = invites.remove(clan);
            if(ret)
                markChanged();
            return ret;
        }

        public boolean addInviteBlock(UUID clan) {
            boolean ret = blockedClans.add(clan);
            if(ret)
                markChanged();
            return ret;
        }

        public boolean removeInviteBlock(UUID clan) {
            boolean ret = blockedClans.remove(clan);
            if(ret)
                markChanged();
            return ret;
        }

        public void setGlobalInviteBlock(boolean inviteBlock) {
            if(this.inviteBlock != inviteBlock) {
                this.inviteBlock = inviteBlock;
                markChanged();
            }
        }

        public void incrementRaidWins() {
            raidWins++;
            markChanged();
        }

        public void incrementRaidLosses() {
            raidLosses++;
            markChanged();
        }

        public void setTerritoryDisplayMode(TerritoryDisplayMode mode) {
            if(mode != territoryDisplayMode) {
                territoryDisplayMode = mode;
                markChanged();
            }
        }

        public void setShowUndergroundMessages(boolean show) {
            if(showUndergroundMessages != show) {
                showUndergroundMessages = show;
                markChanged();
            }
        }

        public void updateLastSeen() {
            lastSeen = System.currentTimeMillis();
            markChanged();
        }

        /**
         * Sets addon data for this player
         * @param key
         * The key you are giving this data. It should be unique
         * @param value
         * The data itself. This should be a primitive, string, a list or map containg only lists/maps/primitives/strings, or a JsonElement. If not, your data may not save/load properly. All lists will be loaded as ArrayLists. All maps will be loaded as HashMaps.
         */
        public void setCustomData(String key, Object value) {
            if(isUnserializable(value))
                Clans.getMinecraftHelper().getLogger().warn("Custom data may not be properly saved and loaded, as it is not assignable from any supported json deserialization. Key: {}, Value: {}", key, value);
            addonData.put(key, value);
            markChanged();
        }

        private boolean isUnserializable(Object value) {
            return !value.getClass().isPrimitive()
                && !value.getClass().isAssignableFrom(BigDecimal.class)
                && !value.getClass().isAssignableFrom(List.class)
                && !value.getClass().isAssignableFrom(Map.class)
                && !value.getClass().isAssignableFrom(JsonElement.class);
        }

        @Nullable
        public Object getCustomData(String key) {
            return addonData.get(key);
        }
    }
}
