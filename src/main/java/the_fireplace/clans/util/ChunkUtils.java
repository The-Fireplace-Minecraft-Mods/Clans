package the_fireplace.clans.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClaimedLandCapability;
import the_fireplace.clans.clan.ClanChunkData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class ChunkUtils {
	@Nullable
	public static UUID getChunkOwner(Chunk c){
		ClaimedLandCapability cap = c.getCapability(Clans.CLAIMED_LAND, null);
		if(cap == null)
			return null;
		return cap.getClan();
	}

	/**
	 * Sets the chunk's new owner
	 * @param c
	 * The chunk
	 * @param newOwner
	 * The new owning faction's ID
	 * @return
	 * The old owner, or null if there wasn't one.
	 */
	public static UUID setChunkOwner(Chunk c, UUID newOwner){
		ClaimedLandCapability cap = c.getCapability(Clans.CLAIMED_LAND, null);
		if(cap == null)
			return null;
		UUID oldOwner = cap.getClan();
		cap.setClan(newOwner);
		return oldOwner;
	}

	public static void clearChunkOwner(Chunk c){
		ClaimedLandCapability cap = c.getCapability(Clans.CLAIMED_LAND, null);
		if(cap == null)
			return;
		cap.setClan(null);
	}

	public static boolean hasConnectedClaim(Chunk c, @Nullable UUID checkOwner) {
		if(checkOwner == null)
			checkOwner = getChunkOwner(c);
		if(checkOwner == null)
			return false;
		return !getConnectedClaims(c, checkOwner).isEmpty();
	}

	@SuppressWarnings("Duplicates")
	public static boolean canBeDisconnected(Chunk c, @Nullable UUID checkOwner) {
		if(checkOwner == null)
			checkOwner = getChunkOwner(c);
		if(checkOwner == null)
			return false;
		ChunkPos cPos = c.getPos();
		switch (Clans.cfg.connectedClaimCheck.toLowerCase()) {
			case "sloppy":
				ArrayList<Chunk> conn = getConnectedClaims(c, checkOwner);
				for(Chunk chunk: conn) {
					ArrayList<Chunk> connected = getConnectedClaims(chunk, checkOwner);
					connected.remove(c);
					if(connected.isEmpty())
						return false;
				}
				return true;
			case "thorough":
				Chunk north = c.getWorld().getChunk(cPos.x, cPos.z - 1);
				if(!checkOwner.equals(getChunkOwner(north)))
					north = null;
				Chunk northeast = c.getWorld().getChunk(cPos.x + 1, cPos.z - 1);
				if(!checkOwner.equals(getChunkOwner(northeast)))
					northeast = null;
				Chunk east = c.getWorld().getChunk(cPos.x + 1, cPos.z);
				if(!checkOwner.equals(getChunkOwner(east)))
					east = null;
				Chunk southeast = c.getWorld().getChunk(cPos.x + 1, cPos.z + 1);
				if(!checkOwner.equals(getChunkOwner(southeast)))
					southeast = null;
				Chunk south = c.getWorld().getChunk(cPos.x, cPos.z + 1);
				if(!checkOwner.equals(getChunkOwner(south)))
					south = null;
				Chunk southwest = c.getWorld().getChunk(cPos.x - 1, cPos.z + 1);
				if(!checkOwner.equals(getChunkOwner(southwest)))
					southwest = null;
				Chunk west = c.getWorld().getChunk(cPos.x - 1, cPos.z);
				if(!checkOwner.equals(getChunkOwner(west)))
					west = null;
				Chunk northwest = c.getWorld().getChunk(cPos.x - 1, cPos.z - 1);
				if(!checkOwner.equals(getChunkOwner(northwest)))
					northwest = null;

				if(north == null && east == null && south == null && west == null)
					return true;
				if(northeast == null && northwest == null && southeast == null && southwest == null
						|| north != null && northeast == null && northwest == null
						|| east != null && northeast == null && southeast == null
						|| south != null && southeast == null && southwest == null
						|| west != null && southwest == null && northwest == null)
					return false;
				int nullcount = 0;
				if(north == null)
					nullcount++;
				if(east == null)
					nullcount++;
				if(south == null)
					nullcount++;
				if(west == null)
					nullcount++;
				if(northeast == null)
					nullcount++;
				if(northwest == null)
					nullcount++;
				if(southeast == null)
					nullcount++;
				if(southwest == null)
					nullcount++;
				switch(nullcount) {
					case 0:
					case 1:
					default:
						return true;
					case 2:
						return north == null && northeast == null
								|| northeast == null && east == null
								|| east == null && southeast == null
								|| southeast == null && south == null
								|| south == null && southwest == null
								|| southwest == null && west == null
								|| west == null && northwest == null
								|| northwest == null && north == null;
					case 3:
						return northwest == null && north == null && northeast == null
								|| north == null && northeast == null && east == null
								|| northeast == null && east == null && southeast == null
								|| east == null && southeast == null && south == null
								|| southeast == null && south == null && southwest == null
								|| south == null && southwest == null && west == null
								|| southwest == null && west == null && northwest == null
								|| west == null && northwest == null && north == null;
					case 4:
						return (north == null) != (south == null)
								&& (northeast == null) != (southwest == null)
								&& (east == null) != (west == null)
								&& (southeast == null) != (northwest == null);
					case 5:
						return northwest != null && north != null && northeast != null
								|| north != null && northeast != null && east != null
								|| northeast != null && east != null && southeast != null
								|| east != null && southeast != null && south != null
								|| southeast != null && south != null && southwest != null
								|| south != null && southwest != null && west != null
								|| southwest != null && west != null && northwest != null
								|| west != null && northwest != null && north != null;
					case 6:
						return north != null && northeast != null
								|| northeast != null && east != null
								|| east != null && southeast != null
								|| southeast != null && south != null
								|| south != null && southwest != null
								|| southwest != null && west != null
								|| west != null && northwest != null
								|| northwest != null && north != null;
				}
				
			case "smart":
			default:
				return !new CoordNodeTree(c.x, c.z, checkOwner).hasDetachedNodes();
		}
	}

    public static ArrayList<Chunk> getConnectedClaims(Chunk c, @Nullable UUID checkOwner) {
	    ArrayList<Chunk> adjacent = Lists.newArrayList();
        if(checkOwner == null)
            checkOwner = getChunkOwner(c);
        if(checkOwner == null)
            return adjacent;
        final UUID finalOwner = checkOwner;
        ChunkPos cPos = c.getPos();
        adjacent.add(c.getWorld().getChunk(cPos.x + 1, cPos.z));
		adjacent.add(c.getWorld().getChunk(cPos.x - 1, cPos.z));
		adjacent.add(c.getWorld().getChunk(cPos.x, cPos.z + 1));
		adjacent.add(c.getWorld().getChunk(cPos.x, cPos.z - 1));
		adjacent.removeIf(c2 -> !finalOwner.equals(getChunkOwner(c2)));
        return adjacent;
    }
}
