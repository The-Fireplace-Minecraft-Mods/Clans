package the_fireplace.clans.util;

import com.google.common.collect.Lists;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClaimedLandCapability;

import javax.annotation.Nullable;
import java.util.ArrayList;
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
		ChunkPos cPos = c.getPos();
        Chunk checkChunk;
        for(int i=-1;i<2;i+=2)
            for(int j=-1;j<2;j+=2) {
                checkChunk = c.getWorld().getChunk(cPos.x + i, cPos.z + j);
                if (checkOwner.equals(getChunkOwner(checkChunk)))
                    return true;
            }
        return false;
	}

    public static ArrayList<Chunk> getConnectedClaims(Chunk c, @Nullable UUID checkOwner) {
	    ArrayList<Chunk> adjacent = Lists.newArrayList();
        if(checkOwner == null)
            checkOwner = getChunkOwner(c);
        if(checkOwner == null)
            return adjacent;
        ChunkPos cPos = c.getPos();
        Chunk checkChunk;
        for(int i=-1;i<2;i+=2)
            for(int j=-1;j<2;j+=2) {
                checkChunk = c.getWorld().getChunk(cPos.x + i, cPos.z + j);
                if (checkOwner.equals(getChunkOwner(checkChunk)))
                    adjacent.add(checkChunk);
            }
        return adjacent;
    }
}
