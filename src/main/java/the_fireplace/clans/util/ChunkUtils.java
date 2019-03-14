package the_fireplace.clans.util;

import com.google.common.collect.Lists;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClaimedLandCapability;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class ChunkUtils {
	@Nullable
	public static UUID getChunkOwner(IChunk c){
		if(c instanceof ICapabilityProvider) {
			//noinspection ConstantConditions
			ClaimedLandCapability cap = ((ICapabilityProvider)c).getCapability(Clans.CLAIMED_LAND, null).orElse(null);
			//noinspection ConstantConditions
			if (cap == null)
				return null;
			return cap.getClan();
		} else
			return null;
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
	public static UUID setChunkOwner(IChunk c, UUID newOwner){
		if(c instanceof ICapabilityProvider) {
			//noinspection ConstantConditions
			ClaimedLandCapability cap = ((ICapabilityProvider)c).getCapability(Clans.CLAIMED_LAND, null).orElse(null);
			//noinspection ConstantConditions
			if(cap == null)
				return null;
			UUID oldOwner = cap.getClan();
			cap.setClan(newOwner);
			return oldOwner;
		} else
			return null;
	}

	public static void clearChunkOwner(IChunk c){
		if(c instanceof ICapabilityProvider) {
			//noinspection ConstantConditions
			ClaimedLandCapability cap = ((ICapabilityProvider)c).getCapability(Clans.CLAIMED_LAND, null).orElse(null);
			//noinspection ConstantConditions
			if(cap == null)
				return;
			cap.setClan(null);
		}
	}

	public static boolean hasConnectedClaim(IChunk c, @Nullable UUID checkOwner) {
		if(checkOwner == null)
			checkOwner = getChunkOwner(c);
		if(checkOwner == null)
			return false;
		ChunkPos cPos = c.getPos();
        return checkOwner.equals(getChunkOwner(Objects.requireNonNull(c.getWorldForge()).getChunk(cPos.x + 1, cPos.z))) || checkOwner.equals(getChunkOwner(Objects.requireNonNull(c.getWorldForge()).getChunk(cPos.x - 1, cPos.z))) || checkOwner.equals(getChunkOwner(Objects.requireNonNull(c.getWorldForge()).getChunk(cPos.x, cPos.z + 1))) || checkOwner.equals(getChunkOwner(Objects.requireNonNull(c.getWorldForge()).getChunk(cPos.x, cPos.z - 1)));
	}

    public static ArrayList<IChunk> getConnectedClaims(IChunk c, @Nullable UUID checkOwner) {
	    ArrayList<IChunk> adjacent = Lists.newArrayList();
        if(checkOwner == null)
            checkOwner = getChunkOwner(c);
        if(checkOwner == null)
            return adjacent;
        ChunkPos cPos = c.getPos();
		adjacent.add(Objects.requireNonNull(c.getWorldForge()).getChunk(cPos.x + 1, cPos.z));
		adjacent.add(c.getWorldForge().getChunk(cPos.x - 1, cPos.z));
		adjacent.add(c.getWorldForge().getChunk(cPos.x, cPos.z + 1));
		adjacent.add(c.getWorldForge().getChunk(cPos.x, cPos.z - 1));
        return adjacent;
    }
}
