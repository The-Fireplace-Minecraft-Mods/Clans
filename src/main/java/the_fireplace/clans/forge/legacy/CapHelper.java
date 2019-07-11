package the_fireplace.clans.forge.legacy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.forge.ClansForge;

@Deprecated
public class CapHelper {

    @Deprecated
    public static ClaimedLandCapability getClaimedLandCapability(EntityPlayer player) {
        //noinspection ConstantConditions
        if(!player.hasCapability(ClansForge.CLAIMED_LAND, null))
            throw new IllegalStateException("Claimed Land Capability is not present for a player!");
        //noinspection ConstantConditions
        return player.getCapability(ClansForge.CLAIMED_LAND, null);
    }

    @Deprecated
    public static ClaimedLandCapability getClaimedLandCapability(Chunk chunk) {
        if(!chunk.hasCapability(ClansForge.CLAIMED_LAND, null))
            throw new IllegalStateException("Claimed Land Capability is not present for a chunk!");
        return chunk.getCapability(ClansForge.CLAIMED_LAND, null);
    }

    @Deprecated
    public static PlayerClanCapability getPlayerClanCapability(EntityPlayer player) {
        //noinspection ConstantConditions
        if(!player.hasCapability(ClansForge.CLAN_DATA_CAP, null))
            throw new IllegalStateException("ClansForge Data Capability is not present for a player!");
        //noinspection ConstantConditions
        return player.getCapability(ClansForge.CLAN_DATA_CAP, null);
    }
}