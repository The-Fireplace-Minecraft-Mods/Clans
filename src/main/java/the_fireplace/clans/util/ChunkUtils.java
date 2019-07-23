package the_fireplace.clans.util;

import com.google.common.collect.Lists;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.data.ClaimDataManager;
import the_fireplace.clans.model.ChunkPositionWithData;
import the_fireplace.clans.model.CoordNodeTree;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;

public class ChunkUtils {
	@Nullable
	public static UUID getChunkOwner(Chunk c){
		return ClaimDataManager.getChunkClanId(c.x, c.z, c.getWorld().provider.getDimension());
	}

	public static void clearChunkOwner(Chunk c){
		ClaimDataManager.delChunk(getChunkOwner(c), new ChunkPositionWithData(c));
	}

	public static boolean hasConnectedClaim(Chunk c, @Nullable UUID checkOwner) {
		if(checkOwner == null)
			checkOwner = getChunkOwner(c);
		if(checkOwner == null)
			return false;
		return !getConnectedClaims(c, checkOwner).isEmpty();
	}

	public static boolean hasConnectedClaim(ChunkPositionWithData c, @Nullable UUID checkOwner) {
		if(checkOwner == null)
			checkOwner = ClaimDataManager.getChunkClanId(c);
		if(checkOwner == null)
			return false;
		return !getConnectedClaims(c, checkOwner).isEmpty();
	}

	@SuppressWarnings("Duplicates")
	public static boolean canBeAbandoned(Chunk c, @Nullable UUID checkOwner) {
		if(checkOwner == null)
			checkOwner = getChunkOwner(c);
		if(checkOwner == null)
			return false;
		ChunkPos cPos = c.getPos();
		switch (Clans.getConfig().getConnectedClaimCheck().toLowerCase()) {
			case "quicker":
				ArrayList<Chunk> conn = getConnectedClaims(c, checkOwner);
				for(Chunk chunk: conn) {
					ArrayList<Chunk> connected = getConnectedClaims(chunk, checkOwner);
					connected.remove(c);
					if(connected.isEmpty())
						return false;
				}
				return true;
			case "quick":
				boolean north = checkOwner.equals(getChunkOwner(c.getWorld().getChunk(cPos.x, cPos.z - 1)));
				boolean northeast = checkOwner.equals(getChunkOwner(c.getWorld().getChunk(cPos.x + 1, cPos.z - 1)));
				boolean east = checkOwner.equals(getChunkOwner(c.getWorld().getChunk(cPos.x + 1, cPos.z)));
				boolean southeast = checkOwner.equals(getChunkOwner(c.getWorld().getChunk(cPos.x + 1, cPos.z + 1)));
				boolean south = checkOwner.equals(getChunkOwner(c.getWorld().getChunk(cPos.x, cPos.z + 1)));
				boolean southwest = checkOwner.equals(getChunkOwner(c.getWorld().getChunk(cPos.x - 1, cPos.z + 1)));
				boolean west = checkOwner.equals(getChunkOwner(c.getWorld().getChunk(cPos.x - 1, cPos.z)));
				boolean northwest = checkOwner.equals(getChunkOwner(c.getWorld().getChunk(cPos.x - 1, cPos.z - 1)));

				if(!north && !east && !south && !west)
					return true;
				if(!northeast && !northwest && !southeast && !southwest
						|| north && !northeast && !northwest
						|| east && !northeast && !southeast
						|| south && !southeast && !southwest
						|| west && !southwest && !northwest)
					return false;
				int nullcount = 0;
				if(!north)
					nullcount++;
				if(!east)
					nullcount++;
				if(!south)
					nullcount++;
				if(!west)
					nullcount++;
				if(!northeast)
					nullcount++;
				if(!northwest)
					nullcount++;
				if(!southeast)
					nullcount++;
				if(!southwest)
					nullcount++;
				switch(nullcount) {
					case 0:
					case 1:
					default:
						return true;
					case 2:
						return !north && !northeast
								|| !northeast && !east
								|| !east && !southeast
								|| !southeast && !south
								|| !south && !southwest
								|| !southwest && !west
								|| !west && !northwest
								|| !northwest && !north;
					case 3:
						return !northwest && !north && !northeast
								|| !north && !northeast && !east
								|| !northeast && !east && !southeast
								|| !east && !southeast && !south
								|| !southeast && !south && !southwest
								|| !south && !southwest && !west
								|| !southwest && !west && !northwest
								|| !west && !northwest && !north;
					case 4:
						return north == !south
								&& northeast == !southwest
								&& east == !west
								&& southeast == !northwest;
					case 5:
						return northwest && north && northeast
								|| north && northeast && east
								|| northeast && east && southeast
								|| east && southeast && south
								|| southeast && south && southwest
								|| south && southwest && west
								|| southwest && west && northwest
								|| west && northwest && north;
					case 6:
						return north && northeast
								|| northeast && east
								|| east && southeast
								|| southeast && south
								|| south && southwest
								|| southwest && west
								|| west && northwest
								|| northwest && north;
				}
				
			case "smart":
			default:
				return !new CoordNodeTree(c.x, c.z, c.getWorld().provider.getDimension(), checkOwner).forDisconnectionCheck().hasDetachedNodes();
		}
	}

    public static ArrayList<Chunk> getConnectedClaims(Chunk c, @Nullable UUID checkOwner) {
	    ArrayList<Chunk> adjacent = Lists.newArrayList();
        if(checkOwner == null)
            checkOwner = getChunkOwner(c);
        if(checkOwner == null)
            return adjacent;
        final UUID checkOwnerFinal = checkOwner;
        ChunkPos cPos = c.getPos();
        adjacent.add(c.getWorld().getChunk(cPos.x + 1, cPos.z));
		adjacent.add(c.getWorld().getChunk(cPos.x - 1, cPos.z));
		adjacent.add(c.getWorld().getChunk(cPos.x, cPos.z + 1));
		adjacent.add(c.getWorld().getChunk(cPos.x, cPos.z - 1));
		adjacent.removeIf(c2 -> !checkOwnerFinal.equals(getChunkOwner(c2)));
        return adjacent;
    }

	public static ArrayList<ChunkPositionWithData> getConnectedClaims(ChunkPositionWithData c, @Nullable UUID checkOwner) {
		ArrayList<ChunkPositionWithData> adjacent = Lists.newArrayList();
		if(checkOwner == null)
			checkOwner = ClaimDataManager.getChunkClanId(c);
		if(checkOwner == null)
			return adjacent;
		final UUID checkOwnerFinal = checkOwner;
		adjacent.add(c.offset(1, 0));
		adjacent.add(c.offset(-1, 0));
		adjacent.add(c.offset(0, 1));
		adjacent.add(c.offset(0, -1));
		adjacent.removeIf(c2 -> !checkOwnerFinal.equals(ClaimDataManager.getChunkClanId(c2)));
		return adjacent;
	}
}
