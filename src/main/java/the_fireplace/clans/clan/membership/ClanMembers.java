package the_fireplace.clans.clan.membership;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayerMP;
import the_fireplace.clans.clan.ClanData;
import the_fireplace.clans.clan.ClanIdRegistry;
import the_fireplace.clans.clan.accesscontrol.ClanLocks;
import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.io.JsonReader;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.player.InvitedPlayers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClanMembers extends ClanData {
    private static final Map<UUID, ClanMembers> MEMBERS = new ConcurrentHashMap<>();

    public static ClanMembers get(UUID clan) {
        MEMBERS.putIfAbsent(clan, new ClanMembers(clan));
        return MEMBERS.get(clan);
    }

    public static void delete(UUID clan) {
        ClanMembers upkeep = MEMBERS.remove(clan);
        if(upkeep != null)
            upkeep.delete();
    }

    static Collection<UUID> lookupPlayerClans(UUID player){
        Set<UUID> clans = new HashSet<>();
        for(ClanMembers members : MEMBERS.values())
            if(members.getMemberRanks().containsKey(player))
                clans.add(members.clan);
        return Collections.unmodifiableSet(clans);
    }

    private final Map<UUID, EnumRank> members = new ConcurrentHashMap<>();

    private ClanMembers(UUID clan) {
        super(clan, "members");
        loadSavedData();
    }

    public Map<UUID, EnumRank> getMemberRanks() {
        return Collections.unmodifiableMap(members);
    }

    public Collection<UUID> getMembers() {
        return Collections.unmodifiableSet(members.keySet());
    }

    public boolean isMember(UUID playerId) {
        return members.containsKey(playerId);
    }

    public Collection<UUID> getLeaders() {
        Set<UUID> leaders = members.entrySet().stream()
            .filter(entry -> entry.getValue() == EnumRank.LEADER)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        return Collections.unmodifiableCollection(leaders);
    }

    public long getLeaderCount() {
        return members.entrySet().stream().filter(entry -> entry.getValue() == EnumRank.LEADER).count();
    }

    public Map<EntityPlayerMP, EnumRank> getOnlineMemberRanks() {
        Map<EntityPlayerMP, EnumRank> online = new HashMap<>();
        for (Map.Entry<UUID, EnumRank> member : getMemberRanks().entrySet()) {
            EntityPlayerMP player = ClansModContainer.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(member.getKey());
            //noinspection ConstantConditions
            if (player != null)
                online.put(player, member.getValue());
        }
        return Collections.unmodifiableMap(online);
    }

    public Collection<EntityPlayerMP> getOnlineMembers() {
        Set<EntityPlayerMP> online = new HashSet<>();
        for (UUID member : getMembers()) {
            EntityPlayerMP player = ClansModContainer.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(member);
            //noinspection ConstantConditions
            if (player != null)
                online.add(player);
        }
        return Collections.unmodifiableSet(online);
    }

    public Set<EntityPlayerMP> getRaidDefenders() {
        return Collections.unmodifiableSet(getOnlineMembers().stream().filter(e -> !e.isCreative() && !e.isSpectator()).collect(Collectors.toSet()));
    }

    public long getRaidDefenderCount() {
        return getOnlineMembers().stream().filter(e -> !e.isCreative() && !e.isSpectator()).count();
    }

    public int getMemberCount() {
        return members.size();
    }

    public void addMember(UUID player) {
        this.members.put(player, EnumRank.MEMBER);
        cacheNewMember(player);
        markChanged();
    }

    public void addMember(UUID player, EnumRank rank) {
        boolean prevHadMember = members.containsKey(player);
        this.members.put(player, rank);
        if (!prevHadMember) {
            cacheNewMember(player);
        }
        markChanged();
    }

    private void cacheNewMember(UUID player) {
        PlayerClans.cachePlayerClan(player, clan);
        InvitedPlayers.removeInvite(player, clan);
        if (!ClansModContainer.getConfig().isAllowMultiClanMembership() && !AdminControlledClanSettings.get(clan).isServerOwned())
            for (UUID clan : ClanIdRegistry.getIds())
                InvitedPlayers.removeInvite(player, clan);
        ClansModContainer.getDynmapCompat().refreshTooltip(clan);
    }

    public boolean removeMember(UUID player) {
        if (isEssentialMember(player))
            return false;
        boolean removed = this.members.remove(player) != null;
        if (removed) {
            PlayerClans.uncachePlayerClan(player, clan);
            ClansModContainer.getDynmapCompat().refreshTooltip(clan);
            ClanLocks.get(clan).removeLockData(player);
            markChanged();
        }
        return removed;
    }

    public boolean isEssentialMember(UUID player) {
        return members.get(player).equals(EnumRank.LEADER)
            && getLeaders().size() == 1
            && !AdminControlledClanSettings.get(clan).isServerOwned();
    }

    public boolean demoteMember(UUID player) {
        if (canMemberBeDemoted(player)) {
            EnumRank playerRank = members.get(player);
            members.put(player, EnumRank.getNextLowerRankInClan(playerRank));
            markChanged();
            return true;
        }
        return false;
    }

    public boolean canMemberBeDemoted(UUID player) {
        return members.containsKey(player)
            && EnumRank.isAboveMemberRank(members.get(player))
            && !isEssentialMember(player);
    }

    public boolean promoteMember(UUID player) {
        if (members.containsKey(player)) {
            if (members.get(player) == EnumRank.ADMIN) {
                demotePreviousLeadersIfNeeded();
                members.put(player, EnumRank.LEADER);
                markChanged();
                return true;
            } else if (members.get(player) == EnumRank.MEMBER) {
                members.put(player, EnumRank.ADMIN);
                markChanged();
                return true;
            }
        }
        return false;
    }

    private void demotePreviousLeadersIfNeeded() {
        if (!ClansModContainer.getConfig().allowsMultipleClanLeaders()) {
            getLeaders().forEach(leader -> members.put(leader, EnumRank.ADMIN));
        }
    }

    @Override
    public void readFromJson(JsonReader reader) {
        for(JsonElement entry: reader.readArray("members"))
            members.put(UUID.fromString(entry.getAsJsonObject().get("key").getAsString()), EnumRank.valueOf(entry.getAsJsonObject().get("value").getAsString()));
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();

        JsonArray members = new JsonArray();
        for(Map.Entry<UUID, EnumRank> entry : this.members.entrySet()) {
            JsonObject newEntry = new JsonObject();
            newEntry.addProperty("key", entry.getKey().toString());
            newEntry.addProperty("value", entry.getValue().toString());
            members.add(newEntry);
        }
        obj.add("members", members);

        return obj;
    }
}