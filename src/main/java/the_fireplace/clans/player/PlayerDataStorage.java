package the_fireplace.clans.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.util.internal.ConcurrentSet;
import the_fireplace.clans.ClansModContainer;
import the_fireplace.clans.io.JsonReader;
import the_fireplace.clans.io.JsonWritable;
import the_fireplace.clans.legacy.logic.PlayerEventLogic;
import the_fireplace.clans.legacy.model.TerritoryDisplayMode;
import the_fireplace.clans.legacy.util.JsonHelper;
import the_fireplace.clans.multithreading.ThreadedSaveHandler;
import the_fireplace.clans.multithreading.ThreadedSaveable;

import javax.annotation.Nullable;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerDataStorage {
    private static final Map<UUID, PlayerStoredData> PLAYER_DATA = new ConcurrentHashMap<>();
    static final File PLAYER_DATA_LOCATION = new File(ClansModContainer.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "clans/player");

    public static void setShouldDisposeReferences(UUID player, boolean shouldDisposeReferences) {
        getPlayerData(player).shouldDisposeReferences = shouldDisposeReferences;
    }

    static PlayerStoredData getPlayerData(UUID player) {
        if(!PLAYER_DATA.containsKey(player))
            PLAYER_DATA.put(player, new PlayerStoredData(player));
        return PLAYER_DATA.get(player);
    }

    public static void save() {
        for(Map.Entry<UUID, PlayerStoredData> entry : PLAYER_DATA.entrySet()) {
            entry.getValue().save();
            if(entry.getValue().shouldDisposeReferences) {
                PLAYER_DATA.remove(entry.getKey());
                entry.getValue().getSaveHandler().disposeReferences();
            }
        }
    }

    static class PlayerStoredData implements ThreadedSaveable, JsonWritable {
        private final File playerDataFile;
        private final ThreadedSaveHandler<PlayerStoredData> saveHandler = ThreadedSaveHandler.create(this);
        private boolean shouldDisposeReferences = false;

        @Nullable
        private UUID defaultClan;
        private int raidWins;
        private int raidLosses;
        private boolean inviteBlock;
        private boolean showUndergroundMessages;
        private final Set<UUID> invites = new ConcurrentSet<>(), blockedClans = new ConcurrentSet<>();
        private TerritoryDisplayMode territoryDisplayMode;
        private long lastSeen;

        private Map<String, Object> addonData = new ConcurrentHashMap<>();

        private PlayerStoredData(UUID playerId) {
            playerDataFile = new File(PLAYER_DATA_LOCATION, playerId.toString()+".json");
            if(!load()) {
                inviteBlock = false;
                raidWins = raidLosses = 0;
                territoryDisplayMode = TerritoryDisplayMode.ACTION_BAR;
                lastSeen = System.currentTimeMillis();
                PlayerEventLogic.onFirstLogin(playerId);
            }
            //If the player is offline, we should remove references so garbage collection can clean it up when the data is done being used.
            //noinspection ConstantConditions
            if(ClansModContainer.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(playerId) == null) {
                shouldDisposeReferences = true;
                saveHandler.disposeReferences();
            }
        }

        /**
         * @return true if it loaded from a file successfully, false otherwise.
         */
        private boolean load() {
            if(!PLAYER_DATA_LOCATION.exists()) {
                PLAYER_DATA_LOCATION.mkdirs();
                return false;
            }

            JsonReader json = JsonReader.create(playerDataFile);
            if(json == null)
                return false;
            defaultClan = json.readUUID( "defaultClan", null);
            invites.addAll(JsonHelper.uuidsFromJsonArray(json.readArray("invites")));
            blockedClans.addAll(JsonHelper.uuidsFromJsonArray(json.readArray("blockedClans")));
            inviteBlock = json.readBool("inviteBlock", false);
            raidWins = json.readInt("raidKills", 0);
            raidLosses = json.readInt("raidDeaths", 0);
            territoryDisplayMode = TerritoryDisplayMode.valueOf(json.readString("territoryDisplayMode", TerritoryDisplayMode.ACTION_BAR.toString()));
            showUndergroundMessages = json.readBool("showUndergroundMessages", true);
            lastSeen = json.readLong("lastSeen", System.currentTimeMillis());
            addonData = JsonHelper.getAddonData(json.getJsonObject());
            return true;
        }

        @Override
        public JsonObject toJson() {
            JsonObject obj = new JsonObject();
            if (defaultClan != null)
                obj.addProperty("defaultClan", defaultClan.toString());
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

        void setDefaultClan(@Nullable UUID defaultClan) {
            if(!Objects.equals(this.defaultClan, defaultClan)) {
                this.defaultClan = defaultClan;
                markChanged();
            }
        }

        boolean addInvite(UUID clan) {
            boolean ret = invites.add(clan);
            if(ret)
                markChanged();
            return ret;
        }

        boolean removeInvite(UUID clan) {
            boolean ret = invites.remove(clan);
            if(ret)
                markChanged();
            return ret;
        }

        boolean addInviteBlock(UUID clan) {
            boolean ret = blockedClans.add(clan);
            if(ret)
                markChanged();
            return ret;
        }

        boolean removeInviteBlock(UUID clan) {
            boolean ret = blockedClans.remove(clan);
            if(ret)
                markChanged();
            return ret;
        }

        void setGlobalInviteBlock(boolean inviteBlock) {
            if(this.inviteBlock != inviteBlock) {
                this.inviteBlock = inviteBlock;
                markChanged();
            }
        }

        boolean getGlobalInviteBlock() {
            return inviteBlock;
        }

        Set<UUID> getInvites() {
            return invites;
        }

        Set<UUID> getBlockedClans() {
            return blockedClans;
        }

        int getRaidWins() {
            return raidWins;
        }

        int getRaidLosses() {
            return raidLosses;
        }

        void incrementRaidWins() {
            raidWins++;
            markChanged();
        }

        boolean isShowingUndergroundMessages() {
            return showUndergroundMessages;
        }

        TerritoryDisplayMode getTerritoryDisplayMode() {
            return territoryDisplayMode;
        }

        @Nullable
        UUID getDefaultClan() {
            return defaultClan;
        }

        long getLastSeen() {
            return lastSeen;
        }

        void incrementRaidLosses() {
            raidLosses++;
            markChanged();
        }

        void setTerritoryDisplayMode(TerritoryDisplayMode mode) {
            if(mode != territoryDisplayMode) {
                territoryDisplayMode = mode;
                markChanged();
            }
        }

        void setShowUndergroundMessages(boolean show) {
            if(showUndergroundMessages != show) {
                showUndergroundMessages = show;
                markChanged();
            }
        }

        void updateLastSeen() {
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
                ClansModContainer.getMinecraftHelper().getLogger().warn("Custom data may not be properly saved and loaded, as it is not assignable from any supported json deserialization. Key: {}, Value: {}", key, value);
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
