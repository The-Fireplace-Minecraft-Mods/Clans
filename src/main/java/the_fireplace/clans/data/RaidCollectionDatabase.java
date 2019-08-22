package the_fireplace.clans.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import the_fireplace.clans.Clans;

import java.io.*;
import java.util.*;

public final class RaidCollectionDatabase {
	static RaidCollectionDatabase instance = null;
	static boolean isChanged = false;

	public static RaidCollectionDatabase getInstance() {
		if(instance == null) {
			load();
			//TODO remove in 1.5
			loadLegacy();
		}
		return instance;
	}

	public static Map<UUID, List<String>> getCollectItems() {
		return Collections.unmodifiableMap(getInstance().collectItems);
	}

	private HashMap<UUID, List<String>> collectItems = Maps.newHashMap();

	public static boolean hasCollectItems(UUID player){
		return getCollectItems().containsKey(player) && !getCollectItems().get(player).isEmpty();
	}

	public void addCollectItem(UUID player, ItemStack payout){
		if(!collectItems.containsKey(player))
			collectItems.put(player, Lists.newArrayList());
		collectItems.get(player).add(payout.writeToNBT(new NBTTagCompound()).toString());
		isChanged = true;
	}

	public static List<String> getCollectItems(UUID player){
		return Collections.unmodifiableList(hasCollectItems(player) ? getInstance().collectItems.get(player) : Lists.newArrayList());
	}

	public void removeCollectItems(UUID player, Collection<String> toRemove){
		collectItems.get(player).removeAll(toRemove);
		isChanged = true;
	}

	private static void load() {
		instance = new RaidCollectionDatabase();
		JsonParser jsonParser = new JsonParser();
		try {
			Object obj = jsonParser.parse(new FileReader(new File(Clans.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "raidcollectitems.json")));
			if(obj instanceof JsonObject) {
				JsonObject jsonObject = (JsonObject) obj;
				JsonArray clanMap = jsonObject.get("collectItems").getAsJsonArray();
				for (int i = 0; i < clanMap.size(); i++) {
					JsonArray valueList = clanMap.get(i).getAsJsonObject().get("value").getAsJsonArray();
					List<String> values = Lists.newArrayList();
					for(JsonElement v: valueList)
						values.add(v.getAsString());
					instance.collectItems.put(UUID.fromString(clanMap.get(i).getAsJsonObject().get("key").getAsString()), values);
				}
			}
		} catch (FileNotFoundException e) {
			//do nothing, it just hasn't been created yet
		} catch (Exception e) {
			e.printStackTrace();
		}
		isChanged = false;
	}

	@Deprecated//TODO remove in 1.5
	private static void loadLegacy() {
		JsonParser jsonParser = new JsonParser();
		try {
			Object obj = jsonParser.parse(new FileReader(new File(Clans.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "raidblockplacement.json")));
			if(obj instanceof JsonObject) {
				JsonObject jsonObject = (JsonObject) obj;
				JsonArray clanMap = jsonObject.get("placedBlocks").getAsJsonArray();
				for (int i = 0; i < clanMap.size(); i++) {
					JsonArray valueList = clanMap.get(i).getAsJsonObject().get("value").getAsJsonArray();
					List<String> values = Lists.newArrayList();
					for(JsonElement v: valueList)
						values.add(v.getAsString());
					instance.collectItems.put(UUID.fromString(clanMap.get(i).getAsJsonObject().get("key").getAsString()), values);
				}
			}
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
		JsonArray collectItemsMap = new JsonArray();
		for(Map.Entry<UUID, List<String>> entry: instance.collectItems.entrySet()) {
			JsonObject outputEntry = new JsonObject();
			outputEntry.addProperty("key", entry.getKey().toString());
			JsonArray valueList = new JsonArray();
			for(String value: entry.getValue())
				valueList.add(value);
			outputEntry.add("value", valueList);
			collectItemsMap.add(outputEntry);
		}
		obj.add("collectItems", collectItemsMap);
		try {
			FileWriter file = new FileWriter(new File(Clans.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "raidcollectitems.json"));
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