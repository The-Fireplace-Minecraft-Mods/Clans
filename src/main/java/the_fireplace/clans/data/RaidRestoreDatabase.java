package the_fireplace.clans.data;

import com.google.common.collect.Maps;
import com.google.gson.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.FMLCommonHandler;
import the_fireplace.clans.Clans;
import the_fireplace.clans.model.ChunkPosition;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public final class RaidRestoreDatabase {
	static RaidRestoreDatabase instance = null;
	static boolean isChanged = false;

	public static RaidRestoreDatabase getInstance() {
		if(instance == null)
			load();
		return instance;
	}

	HashMap<ChunkPosition, ChunkRestoreData> raidedChunks = Maps.newHashMap();

	public static void addRestoreBlock(int dim, Chunk c, BlockPos pos, String block) {
		ChunkPosition coords = new ChunkPosition(c.x, c.z, dim);
		if(!getInstance().raidedChunks.containsKey(coords))
			getInstance().raidedChunks.put(coords, new ChunkRestoreData());
		if(getInstance().raidedChunks.get(coords).hasRestoreBlock(pos.getX(), pos.getY(), pos.getZ()))
			Clans.getMinecraftHelper().getLogger().error("Block restore cache being written when it already exists at position ({}, {}, {}). Block being written is: {}.", pos.getX(), pos.getY(), pos.getZ(), block);
		getInstance().raidedChunks.get(coords).addRestoreBlock(pos.getX(), pos.getY(), pos.getZ(), block);
		isChanged = true;
	}

	public static String popRestoreBlock(int dim, Chunk c, BlockPos pos) {
		ChunkPosition coords = new ChunkPosition(c.x, c.z, dim);
		if(!getInstance().raidedChunks.containsKey(coords))
			return null;
		String block = getInstance().raidedChunks.get(coords).popRestoreBlock(pos.getX(), pos.getY(), pos.getZ());
		if(block != null)
			isChanged = true;
		return block;
	}

	public static void addRemoveBlock(int dim, Chunk c, BlockPos pos) {
		ChunkPosition coords = new ChunkPosition(c.x, c.z, dim);
		if(!getInstance().raidedChunks.containsKey(coords))
			getInstance().raidedChunks.put(coords, new ChunkRestoreData());
		getInstance().raidedChunks.get(coords).addRemoveBlock(pos.getX(), pos.getY(), pos.getZ());
		isChanged = true;
	}

	public static boolean delRemoveBlock(int dim, Chunk c, BlockPos pos) {
		ChunkPosition coords = new ChunkPosition(c.x, c.z, dim);
		if(!getInstance().raidedChunks.containsKey(coords))
			return false;
		boolean block = getInstance().raidedChunks.get(coords).delRemoveBlock(pos.getX(), pos.getY(), pos.getZ());
		if(block)
			isChanged = true;
		return block;
	}

	public static ChunkRestoreData popChunkRestoreData(int dim, Chunk c) {
		ChunkRestoreData d = getInstance().raidedChunks.remove(new ChunkPosition(c.x, c.z, dim));
		if(d != null)
			isChanged = true;
		return d;
	}

	private static void load() {
		instance = new RaidRestoreDatabase();
		JsonParser jsonParser = new JsonParser();
		try {
			Object obj = jsonParser.parse(new FileReader(new File(Clans.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "raids.json")));
			if(obj instanceof JsonObject) {
				JsonObject jsonObject = (JsonObject) obj;
				JsonArray clanMap = jsonObject.get("restoreChunks").getAsJsonArray();
				for (int i = 0; i < clanMap.size(); i++)
					instance.raidedChunks.put(new ChunkPosition(clanMap.get(i).getAsJsonObject().get("key").getAsJsonObject()), new ChunkRestoreData(clanMap.get(i).getAsJsonObject().get("value").getAsJsonObject()));
			} else
				Clans.getMinecraftHelper().getLogger().warn("Json Raid Restore Database not found! This is normal on your first run of ClansForge 1.2.0 and above, and when there is nothing to restore.");
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
		JsonArray chunkRestoreMap = new JsonArray();
		for(Map.Entry<ChunkPosition, ChunkRestoreData> entry: instance.raidedChunks.entrySet()) {
			JsonObject outputEntry = new JsonObject();
			outputEntry.add("key", entry.getKey().toJsonObject());
			outputEntry.add("value", entry.getValue().toJsonObject());
			chunkRestoreMap.add(outputEntry);
		}
		obj.add("restoreChunks", chunkRestoreMap);
		try {
			FileWriter file = new FileWriter(new File(Clans.getMinecraftHelper().getServer().getWorld(0).getSaveHandler().getWorldDirectory(), "raids.json"));
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
