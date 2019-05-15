package the_fireplace.clans.clan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import the_fireplace.clans.Clans;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public final class NewClanDatabase {
    private static NewClanDatabase instance = null;
    private static boolean isChanged = false;

    public static NewClanDatabase getInstance() {
        if(instance == null) {
            load();
            if(instance.opclan == null)
                if(instance.clans.containsKey(UUID.fromString("00000000-0000-0000-0000-000000000000")))
                    instance.opclan = instance.clans.get(UUID.fromString("00000000-0000-0000-0000-000000000000"));
                else if(ClanDatabase.getInstance().getOpclan() != null)//TODO: Remove this in Clans 1.3, as it is kept for legacy database compat.
                    instance.opclan = new NewClan(ClanDatabase.getInstance().getOpclan().toJsonObject());
                else
                    instance.opclan = new NewClan();
        }
        return instance;
    }

    private HashMap<UUID, NewClan> clans;
    private NewClan opclan = null;

    private NewClanDatabase(){
        clans = Maps.newHashMap();
    }

    public static NewClan getOpClan() {
        NewClan out = getInstance().opclan;
        if(out == null) {
            instance.opclan = new NewClan();
            out = instance.opclan;
        }
        return out;
    }

    public static void markChanged() {
        isChanged = true;
    }

    @Nullable
    public static NewClan getClan(@Nullable UUID clanId){
        return getInstance().clans.get(clanId);
    }

    public static Collection<NewClan> getClans(){
        return getInstance().clans.values();
    }

    static boolean addClan(UUID clanId, NewClan clan){
        if(!getInstance().clans.containsKey(clanId)) {
            getInstance().clans.put(clanId, clan);
            ClanCache.addName(clan);
            if(clan.getClanBanner() != null)
                ClanCache.addBanner(clan.getClanBanner());
            isChanged = true;
            return true;
        }
        return false;
    }

    public static boolean removeClan(UUID clanId){
        if(getInstance().clans.containsKey(clanId)){
            NewClan clan = getInstance().clans.remove(clanId);
            ClanCache.removeName(clan.getClanName());
            if(clan.getClanBanner() != null)
                ClanCache.removeBanner(clan.getClanBanner());
            for(UUID member: clan.getMembers().keySet())
                ClanCache.purgePlayerCache(member);
            isChanged = true;
            return true;
        }
        return false;
    }

    private static void setOpclan(NewClan opclan) {
        instance.opclan = opclan;
    }

    /**
     * An inefficient way to look up a player's clan. For efficiency, use {@link ClanCache#getClansByPlayer(UUID)}
     * @param player
     * The player to get the clan of
     * @return
     * The player's clans, or an empty list if the player isn't in any
     */
    static ArrayList<NewClan> lookupPlayerClans(UUID player){
        ArrayList<NewClan> clans = Lists.newArrayList();
        for(NewClan clan : getInstance().clans.values())
            if(clan.getMembers().keySet().contains(player))
                clans.add(clan);
        return clans;
    }

    private static void load() {
        instance = new NewClanDatabase();
        JsonParser jsonParser = new JsonParser();
        try {
            Object obj = jsonParser.parse(new FileReader(new File(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD).getSaveHandler().getWorldDirectory(), "clans.json")));
            if(obj instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) obj;
                JsonArray clanMap = jsonObject.get("clans").getAsJsonArray();
                for (int i = 0; i < clanMap.size(); i++)
                    addClan(UUID.fromString(clanMap.get(i).getAsJsonObject().get("key").getAsString()), new NewClan(clanMap.get(i).getAsJsonObject().get("value").getAsJsonObject()));
                setOpclan(new NewClan(jsonObject.getAsJsonObject("opclan")));
            } else
                Clans.LOGGER.warn("Json Clan Database not found! This is normal on your first run of Clans 1.2.0 and above.");
        } catch (FileNotFoundException e) {
            //do nothing, it just hasn't been created yet
        } catch (Exception e) {
            e.printStackTrace();
        }
        isChanged = false;
        ClanDatabase.getInstance();
    }

    public static void save() {
        if(!isChanged)
            return;
        JsonObject obj = new JsonObject();
        JsonArray clanMap = new JsonArray();
        for(NewClan clan: getClans()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("key", clan.getClanId().toString());
            entry.add("value", clan.toJsonObject());
            clanMap.add(entry);
        }
        obj.add("clans", clanMap);
        obj.add("opclan", getOpClan().toJsonObject());
        try {
            FileWriter file = new FileWriter(new File(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD).getSaveHandler().getWorldDirectory(), "clans.json"));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(obj);
            file.write(json);
            file.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        isChanged = false;
    }
}