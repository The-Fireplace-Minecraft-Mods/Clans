package the_fireplace.clans.util;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClaimedLandCapability;

@MethodsReturnNonnullByDefault
public class CapHelper {

    public static ClaimedLandCapability getClaimedLandCapability(EntityPlayer player) {
        //noinspection ConstantConditions
        return player.getCapability(Clans.CLAIMED_LAND).orElseThrow(() -> new IllegalStateException("Claimed Land Capability is not present for a player!"));
    }

    public static ClaimedLandCapability getClaimedLandCapability(IChunk chunk) {
        //noinspection ConstantConditions
        return chunk instanceof ICapabilityProvider ? ((ICapabilityProvider) chunk).getCapability(Clans.CLAIMED_LAND).orElseThrow(() -> new IllegalStateException("Claimed Land Capability is not present for a chunk!")) : null;
    }

    public static PlayerClanCapability getPlayerClanCapability(EntityPlayer player) {
        //noinspection ConstantConditions
        return player.getCapability(Clans.CLAN_DATA_CAP).orElseThrow(() -> new IllegalStateException("Clan Data Capability is present and not present!"));
    }
}
