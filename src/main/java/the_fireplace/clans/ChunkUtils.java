package the_fireplace.clans;

import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.clan.ClaimedLandCapability;

import javax.annotation.Nullable;
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
}
