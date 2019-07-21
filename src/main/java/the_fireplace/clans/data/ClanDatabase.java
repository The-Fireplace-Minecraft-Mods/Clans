package the_fireplace.clans.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.*;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.model.Clan;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public final class ClanDatabase {
    private static ClanDatabase instance = null;
    public static final File clanDataLocation = new File(Clans.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "clans/clan");

    public static ClanDatabase getInstance() {
        if(instance == null) {
            load();
            loadLegacy();
            if(instance.opclan == null)
                if(instance.clans.containsKey(UUID.fromString("00000000-0000-0000-0000-000000000000")))
                    instance.opclan = instance.clans.get(UUID.fromString("00000000-0000-0000-0000-000000000000"));
                else
                    instance.opclan = new Clan();
        }
        return instance;
    }

    private HashMap<UUID, Clan> clans;
    @Deprecated
    private Clan opclan = null;

    private ClanDatabase(){
        clans = Maps.newHashMap();
    }

    /**
     * Get the server's opclan. Will be removed in 1.4 as multiple opclans will be allowed.
     */
    @Deprecated
    public static Clan getOpClan() {
        Clan out = getInstance().opclan;
        if(out == null) {
            instance.opclan = new Clan();
            out = instance.opclan;
        }
        return out;
    }

    @Nullable
    public static Clan getClan(@Nullable UUID clanId){
        if(clanId == null)
            return null;
        return getInstance().clans.get(clanId);
    }

    public static Collection<Clan> getClans(){
        return Sets.newHashSet(getInstance().clans.values());
    }

    public static boolean addClan(UUID clanId, Clan clan){
        if(!getInstance().clans.containsKey(clanId)) {
            getInstance().clans.put(clanId, clan);
            ClanCache.addName(clan);
            if(clan.getClanBanner() != null)
                ClanCache.addBanner(clan.getClanBanner());
            clan.markChanged();
            return true;
        }
        return false;
    }

    /**
     * For internal use only. Anyone wishing to do this, use {@link Clan#disband(MinecraftServer, ICommandSender, String, Object...)}
     */
    public static boolean removeClan(UUID clanId){
        if(getInstance().clans.containsKey(clanId)) {
            Clan removed = getInstance().clans.remove(clanId);
            ClanCache.removeClan(removed);
            ClaimDataManager.delClan(clanId);
            removed.getClanDataFile().delete();
            return true;
        }
        return false;
    }

    @Deprecated
    private static void setOpclan(Clan opclan) {
        instance.opclan = opclan;
    }

    /**
     * For internal use only. Anyone wishing to do this, use {@link ClanCache#getPlayerClans(UUID)}
     * @param player
     * The player to get the clan of
     * @return
     * The player's clans, or an empty list if the player isn't in any
     */
    public static ArrayList<Clan> lookupPlayerClans(UUID player){
        ArrayList<Clan> clans = Lists.newArrayList();
        for(Clan clan : getInstance().clans.values())
            if(clan.getMembers().keySet().contains(player))
                clans.add(clan);
        return clans;
    }

    private static void loadLegacy() {
        File oldFile = new File(Clans.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "clans.json");
        if(!oldFile.exists())
            return;
        JsonParser jsonParser = new JsonParser();
        try {
            Object obj = jsonParser.parse(new FileReader(oldFile));
            if(obj instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) obj;
                JsonArray clanMap = jsonObject.get("clans").getAsJsonArray();
                for (int i = 0; i < clanMap.size(); i++) {
                    Clan loadedClan = new Clan(clanMap.get(i).getAsJsonObject().get("value").getAsJsonObject());
                    loadedClan.markChanged();
                    addClan(UUID.fromString(clanMap.get(i).getAsJsonObject().get("key").getAsString()), loadedClan);
                }
                Clan opclan = new Clan(jsonObject.getAsJsonObject("opclan"));
                opclan.markChanged();
                setOpclan(opclan);
            }
        } catch (FileNotFoundException e) {
            //do nothing
        } catch (Exception e) {
            e.printStackTrace();
        }
        oldFile.delete();
    }

    private static void load() {
        instance = new ClanDatabase();
        if(!clanDataLocation.exists())
            clanDataLocation.mkdirs();
        for(File file: clanDataLocation.listFiles()) {
            try {
                Clan loadedClan = Clan.load(file);
                if(loadedClan != null)
                    instance.clans.put(loadedClan.getClanId(), loadedClan);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void save() {
        for(Clan clan: getClans())
            clan.save();
    }
}
