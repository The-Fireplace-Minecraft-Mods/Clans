package the_fireplace.clans.forge.legacy;

import net.minecraft.entity.player.EntityPlayer;
import the_fireplace.clans.ClansForge;

@Deprecated
public class CapHelper {

    @Deprecated
    public static PlayerClanCapability getPlayerClanCapability(EntityPlayer player) {
        //noinspection ConstantConditions
        if(!player.hasCapability(ClansForge.CLAN_DATA_CAP, null))
            throw new IllegalStateException("ClansForge Data Capability is not present for a player!");
        //noinspection ConstantConditions
        return player.getCapability(ClansForge.CLAN_DATA_CAP, null);
    }
}