package the_fireplace.clans.sponge.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.world.Location;
import the_fireplace.clans.logic.LandProtectionEventLogic;
import the_fireplace.clans.logic.RaidManagementLogic;
import the_fireplace.clans.sponge.SpongeHelper;

public class LandProtectionEvents {
    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        Player player = event.getContext().get(EventContextKeys.PLAYER).orElse(null);
        BlockSnapshot blockSnapshot = event.getContext().get(EventContextKeys.BLOCK_HIT).orElse(null);
        if(blockSnapshot == null || player == null)
            return;
        if(!LandProtectionEventLogic.shouldCancelBlockBroken((World)player.getWorld(), SpongeHelper.getPos(blockSnapshot.getPosition()), (EntityPlayer) player))
            RaidManagementLogic.onBlockBroken((World)player.getWorld(), SpongeHelper.getPos(blockSnapshot.getPosition()), (IBlockState)blockSnapshot.getState());
    }

    @Listener
    public void onCropTrample(ChangeBlockEvent.Place event) {
        if(!event.getTransactions().isEmpty()) {
            Player player = event.getContext().get(EventContextKeys.PLAYER).orElse(null);
            BlockSnapshot source = event.getTransactions().get(0).getOriginal();
            if(player == null)
                return;
            if(source.getState().getType().equals(BlockTypes.FARMLAND) && source.getLocation().isPresent())
                event.setCancelled(LandProtectionEventLogic.shouldCancelCropTrample((World)player.getWorld(), SpongeHelper.getPos(source.getPosition()), (EntityPlayer)player));
        }
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event) {
        Player player = event.getContext().get(EventContextKeys.PLAYER).orElse(null);
        BlockSnapshot blockSnapshot = event.getTransactions().get(0).getFinal();
        Location location = blockSnapshot.getLocation().orElse(null);
        if(player == null || location == null)
            return;
        event.setCancelled(LandProtectionEventLogic.shouldCancelBlockPlacement((World)player.getWorld(), SpongeHelper.getPos(blockSnapshot.getPosition()), (EntityPlayer)player, ((EntityPlayer) player).getActiveHand().equals(EnumHand.MAIN_HAND) ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND));
        if(!event.isCancelled())
            RaidManagementLogic.onBlockPlaced((World)player.getWorld(), SpongeHelper.getPos(blockSnapshot.getPosition()), (EntityPlayer)player, ((EntityPlayer) player).getActiveHand().equals(EnumHand.MAIN_HAND) ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND, ((IBlockState)blockSnapshot.getState()).getBlock());
    }
}
