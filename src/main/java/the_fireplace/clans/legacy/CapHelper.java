package the_fireplace.clans.legacy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;

@Deprecated
public class CapHelper {

    @Deprecated
    public static ClaimedLandCapability getClaimedLandCapability(EntityPlayer player) {
        //noinspection ConstantConditions
        if(!player.hasCapability(Clans.CLAIMED_LAND, null))
            throw new IllegalStateException("Claimed Land Capability is not present for a player!");
        //noinspection ConstantConditions
        return player.getCapability(Clans.CLAIMED_LAND, null);
    }

    @Deprecated
    public static ClaimedLandCapability getClaimedLandCapability(Chunk chunk) {
        if(!chunk.hasCapability(Clans.CLAIMED_LAND, null))
            throw new IllegalStateException("Claimed Land Capability is not present for a chunk!");
        return chunk.getCapability(Clans.CLAIMED_LAND, null);
    }

    @Deprecated
    public static PlayerClanCapability getPlayerClanCapability(EntityPlayer player) {
        //noinspection ConstantConditions
        if(!player.hasCapability(Clans.CLAN_DATA_CAP, null))
            throw new IllegalStateException("Clans Data Capability is not present for a player!");
        //noinspection ConstantConditions
        return player.getCapability(Clans.CLAN_DATA_CAP, null);
    }
}