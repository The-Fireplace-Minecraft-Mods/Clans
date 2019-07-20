package the_fireplace.clans.sponge.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.world.Location;
import the_fireplace.clans.Clans;
import the_fireplace.clans.logic.LandProtectionEventLogic;
import the_fireplace.clans.logic.ServerEventLogic;
import the_fireplace.clans.sponge.compat.SpongeMinecraftHelper;

public class LandProtectionEvents {

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        Player player = event.getContext().get(EventContextKeys.PLAYER).orElse(null);
        BlockSnapshot blockSnapshot = event.getContext().get(EventContextKeys.BLOCK_HIT).orElse(null);
        if(blockSnapshot == null)
            return;
        Location location = blockSnapshot.getLocation().orElse(null);
        if(player == null || location == null)
            return;
        LandProtectionEventLogic.onBlockBroken((World)player.getWorld(), new BlockPos(location.getX(), location.getY(), location.getZ()), (EntityPlayer) player);
    }
}
