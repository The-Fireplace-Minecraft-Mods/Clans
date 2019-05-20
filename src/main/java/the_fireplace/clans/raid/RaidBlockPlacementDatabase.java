package the_fireplace.clans.raid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import the_fireplace.clans.Clans;

import java.io.*;
import java.util.*;

public final class RaidBlockPlacementDatabase {
	static RaidBlockPlacementDatabase instance = null;
	static boolean isChanged = false;

	public static RaidBlockPlacementDatabase getInstance() {
		if(instance == null)
			load();
		return instance;
	}

	public static HashMap<UUID, List<String>> getPlacedBlocks() {
		return getInstance().placedBlocks;
	}

	HashMap<UUID, List<String>> placedBlocks = Maps.newHashMap();

	public static boolean hasPlacedBlocks(UUID player){
		return getPlacedBlocks().containsKey(player) && !getPlacedBlocks().get(player).isEmpty();
	}

	public void addPlacedBlock(UUID player, ItemStack payout){
		if(!placedBlocks.containsKey(player))
			placedBlocks.put(player, Lists.newArrayList());
		placedBlocks.get(player).add(payout.writeToNBT(new NBTTagCompound()).toString());
		isChanged = true;
	}

	public static List<String> getPlacedBlocks(UUID player){
		return hasPlacedBlocks(player) ? getInstance().placedBlocks.get(player) : Lists.newArrayList();
	}

	public void removePlacedBlocks(UUID player, Collection<String> toRemove){
		placedBlocks.get(player).removeAll(toRemove);
		isChanged = true;
	}

	private static void load() {
		instance = new RaidBlockPlacementDatabase();
		JsonParser jsonParser = new JsonParser();
		try {
			Object obj = jsonParser.parse(new FileReader(new File(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getSaveHandler().getWorldDirectory(), "raidblockplacement.json")));
			if(obj instanceof JsonObject) {
				JsonObject jsonObject = (JsonObject) obj;
				JsonArray clanMap = jsonObject.get("placedBlocks").getAsJsonArray();
				for (int i = 0; i < clanMap.size(); i++) {
					JsonArray valueList = clanMap.get(i).getAsJsonObject().get("value").getAsJsonArray();
					List<String> values = Lists.newArrayList();
					for(JsonElement v: valueList)
						values.add(v.getAsString());
					instance.placedBlocks.put(UUID.fromString(clanMap.get(i).getAsJsonObject().get("key").getAsString()), values);
				}
			} else
				Clans.LOGGER.warn("Json Raid Placed Block Database not found! This is normal on your first run of Clans 1.2.0 and above, and when there is nothing to restore.");
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
		JsonArray placedBlocksMap = new JsonArray();
		for(Map.Entry<UUID, List<String>> entry: instance.placedBlocks.entrySet()) {
			JsonObject outputEntry = new JsonObject();
			outputEntry.addProperty("key", entry.getKey().toString());
			JsonArray valueList = new JsonArray();
			for(String value: entry.getValue())
				valueList.add(value);
			outputEntry.add("value", valueList);
			placedBlocksMap.add(outputEntry);
		}
		obj.add("placedBlocks", placedBlocksMap);
		try {
			FileWriter file = new FileWriter(new File(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getSaveHandler().getWorldDirectory(), "raidblockplacement.json"));
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