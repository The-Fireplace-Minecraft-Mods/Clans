package the_fireplace.clans.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import the_fireplace.clans.Clans;
import the_fireplace.clans.io.JsonWritable;
import the_fireplace.clans.multithreading.ThreadedSaveHandler;
import the_fireplace.clans.multithreading.ThreadedSaveable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class RaidCollectionDatabase implements ThreadedSaveable, JsonWritable {
	private static RaidCollectionDatabase instance = null;
	private final ThreadedSaveHandler<RaidCollectionDatabase> saveHandler = ThreadedSaveHandler.create(this);
	private static File raidCollectionDatabaseFile;

	public static RaidCollectionDatabase getInstance() {
		if(instance == null) {
			load();
		}
		return instance;
	}

	public static Map<UUID, Set<String>> getCollectItems() {
		return Collections.unmodifiableMap(getInstance().collectItems);
	}

	private final Map<UUID, Set<String>> collectItems = new ConcurrentHashMap<>();

	public static boolean hasCollectItems(UUID player){
		return getCollectItems().containsKey(player) && !getCollectItems().get(player).isEmpty();
	}

	public void addCollectItem(UUID player, ItemStack payout){
		collectItems.putIfAbsent(player, new ConcurrentSet<>());
		collectItems.get(player).add(payout.writeToNBT(new NBTTagCompound()).toString());
		markChanged();
	}

	public static Set<String> getCollectItems(UUID player){
		return Collections.unmodifiableSet(hasCollectItems(player) ? getInstance().collectItems.get(player) : Collections.emptySet());
	}

	public void removeCollectItems(UUID player, Collection<String> toRemove){
		collectItems.get(player).removeAll(toRemove);
		markChanged();
	}

	private static void load() {
		instance = new RaidCollectionDatabase();
		raidCollectionDatabaseFile = new File(Clans.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "raidcollectitems.json");
		JsonParser jsonParser = new JsonParser();
		try {
			try(FileReader fr = new FileReader(raidCollectionDatabaseFile)) {
				Object obj = jsonParser.parse(fr);
				if (obj instanceof JsonObject) {
					JsonObject jsonObject = (JsonObject) obj;
					JsonArray clanMap = jsonObject.get("collectItems").getAsJsonArray();
					for (int i = 0; i < clanMap.size(); i++) {
						JsonArray valueList = clanMap.get(i).getAsJsonObject().get("value").getAsJsonArray();
						Set<String> values = new ConcurrentSet<>();
						for (JsonElement v : valueList)
							values.add(v.getAsString());
						instance.collectItems.put(UUID.fromString(clanMap.get(i).getAsJsonObject().get("key").getAsString()), values);
					}
				}
			}
		} catch (FileNotFoundException e) {
			//do nothing, it just hasn't been created yet
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public JsonObject toJson() {
		JsonObject obj = new JsonObject();
		JsonArray collectItemsMap = new JsonArray();
		for(Map.Entry<UUID, Set<String>> entry: instance.collectItems.entrySet()) {
			JsonObject outputEntry = new JsonObject();
			outputEntry.addProperty("key", entry.getKey().toString());
			JsonArray valueList = new JsonArray();
			for(String value: entry.getValue())
				valueList.add(value);
			outputEntry.add("value", valueList);
			collectItemsMap.add(outputEntry);
		}
		obj.add("collectItems", collectItemsMap);
		return obj;
	}

	@Override
	public void blockingSave() {
		writeToJson(raidCollectionDatabaseFile);
	}

	@Override
	public ThreadedSaveHandler<?> getSaveHandler() {
		return saveHandler;
	}
}