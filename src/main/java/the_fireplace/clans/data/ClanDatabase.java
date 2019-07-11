package the_fireplace.clans.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.*;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
    private static boolean isChanged = false;

    public static ClanDatabase getInstance() {
        if(instance == null) {
            load();
            if(instance.opclan == null)
                if(instance.clans.containsKey(UUID.fromString("00000000-0000-0000-0000-000000000000")))
                    instance.opclan = instance.clans.get(UUID.fromString("00000000-0000-0000-0000-000000000000"));
                else
                    instance.opclan = new Clan();
        }
        return instance;
    }

    private HashMap<UUID, Clan> clans;
    private Clan opclan = null;

    private ClanDatabase(){
        clans = Maps.newHashMap();
    }

    public static Clan getOpClan() {
        Clan out = getInstance().opclan;
        if(out == null) {
            instance.opclan = new Clan();
            out = instance.opclan;
        }
        return out;
    }

    public static void markChanged() {
        isChanged = true;
    }

    @Nullable
    public static Clan getClan(@Nullable UUID clanId){
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
            markChanged();
            return true;
        }
        return false;
    }

    /**
     * For internal use only. Anyone wishing to do this, use {@link Clan#disband(MinecraftServer, ICommandSender, String, Object...)}
     */
    public static boolean removeClan(UUID clanId){
        if(getInstance().clans.containsKey(clanId)) {
            ClanCache.removeClan(getInstance().clans.remove(clanId));
            ClanChunkData.delClan(clanId);
            markChanged();
            return true;
        }
        return false;
    }

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

    private static void load() {
        instance = new ClanDatabase();
        JsonParser jsonParser = new JsonParser();
        try {
            Object obj = jsonParser.parse(new FileReader(new File(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getSaveHandler().getWorldDirectory(), "clans.json")));
            if(obj instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) obj;
                JsonArray clanMap = jsonObject.get("clans").getAsJsonArray();
                for (int i = 0; i < clanMap.size(); i++)
                    addClan(UUID.fromString(clanMap.get(i).getAsJsonObject().get("key").getAsString()), new Clan(clanMap.get(i).getAsJsonObject().get("value").getAsJsonObject()));
                setOpclan(new Clan(jsonObject.getAsJsonObject("opclan")));
            } else
                Clans.LOGGER.warn("Json Clan Database not found! This is normal on your first run of Clans 1.2.0 and above.");
        } catch (FileNotFoundException e) {
            //do nothing, it just hasn't been created yet
        } catch (Exception e) {
            e.printStackTrace();
        }
        isChanged = false;
    }

    public static void save() {
        if(!isChanged)
            return;
        JsonObject obj = new JsonObject();
        JsonArray clanMap = new JsonArray();
        for(Clan clan: getClans()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("key", clan.getClanId().toString());
            entry.add("value", clan.toJsonObject());
            clanMap.add(entry);
        }
        obj.add("clans", clanMap);
        obj.add("opclan", getOpClan().toJsonObject());
        try {
            FileWriter file = new FileWriter(new File(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getSaveHandler().getWorldDirectory(), "clans.json"));
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
