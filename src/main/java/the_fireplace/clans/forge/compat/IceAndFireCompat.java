package the_fireplace.clans.forge.compat;

import com.github.alexthe666.iceandfire.api.event.DragonFireDamageWorldEvent;
import com.github.alexthe666.iceandfire.api.event.DragonFireEvent;
import com.github.alexthe666.iceandfire.api.event.GenericGriefEvent;
import com.github.alexthe666.iceandfire.entity.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.abstraction.IProtectionCompat;
import the_fireplace.clans.data.ClaimData;
import the_fireplace.clans.logic.LandProtectionLogic;
import the_fireplace.clans.model.ChunkPositionWithData;

import javax.annotation.Nullable;
import java.util.UUID;

public class IceAndFireCompat implements IProtectionCompat {
    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public boolean isOwnable(Entity entity) {
        return false;
    }

    @Nullable
    @Override
    public UUID getOwnerId(Entity entity) {
        return null;
    }

    @Override
    public boolean isMob(Entity entity) {
        //Untamed entities that should be feared by animals and/or villagers are probably mobs.
        return (entity instanceof IVillagerFear && ((IVillagerFear) entity).shouldFear() || entity instanceof IAnimalFear && ((IAnimalFear) entity).shouldAnimalsFear(entity)) && (!(entity instanceof EntityTameable) || ((EntityTameable) entity).getOwnerId() == null);
    }

    @Override
    public boolean isContainer(World world, BlockPos pos, @Nullable IBlockState state, @Nullable TileEntity tileEntity) {
        return false;
    }

    //We cannot use projectile impact event for these because they bypass it, so it's better to attempt to stop the projectile from being spawned in the first place.
    //This does not stop all dragon breath, just the ones that use this projectile. Fire breath, for example, can be used to light things on fire without spawning a fire charge, and there is nothing I can do to prevent that, as far as I can tell.
    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent event) {
        if(!event.getEntity().world.isRemote) {
            if(event.getEntity() instanceof EntityDragonFireCharge || event.getEntity() instanceof EntityDragonIceCharge || event.getEntity() instanceof EntityPixieCharge || event.getEntity() instanceof EntitySeaSerpentBubbles) {
                Entity owner = LandProtectionLogic.getOwner(((EntityFireball)event.getEntity()).shootingEntity);
                EntityPlayer player = owner instanceof EntityPlayer ? (EntityPlayer)owner : null;
                RayTraceResult rayTraceResult = ProjectileHelper.forwardsRaycast(event.getEntity(), false, true, ((EntityFireball)event.getEntity()).shootingEntity);
                @SuppressWarnings("ConstantConditions")
                BlockPos pos = rayTraceResult != null ? rayTraceResult.getBlockPos() : event.getEntity().getPosition();
                event.setCanceled(
                    (player == null && (Clans.getConfig().shouldProtectWilderness() || ClaimData.getChunkClan(new ChunkPositionWithData(event.getEntity().world.getChunk(pos))) != null))
                        || LandProtectionLogic.shouldCancelBlockBroken(event.getEntity().world, pos, player, false)
                        || LandProtectionLogic.shouldCancelBlockPlacement(event.getEntity().world, pos, player, null, false));
            }
        }
    }

    @SubscribeEvent
    public void mobGrief(EntityMobGriefingEvent event) {
        if(event.getEntity() == null || event.getEntity().world == null)
            return;
        if(!event.getEntity().world.isRemote) {
            if(event.getEntity() instanceof EntityDragonBase) {
                Entity owner = LandProtectionLogic.getOwner(event.getEntity());
                EntityPlayer player = owner instanceof EntityPlayer ? (EntityPlayer)owner : null;
                if((player == null && (Clans.getConfig().shouldProtectWilderness() || ClaimData.getChunkClan(new ChunkPositionWithData(event.getEntity().world.getChunk(event.getEntity().getPosition()))) != null))
                    || LandProtectionLogic.shouldCancelBlockBroken(event.getEntity().world, event.getEntity().getPosition(), player, false))
                    event.setResult(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public void dragonFire(DragonFireEvent event) {
        event.setCanceled(LandProtectionLogic.shouldCancelBlockBroken(event.getEntity().world, new BlockPos(event.getTargetX(), event.getTargetY(), event.getTargetZ()), event.getDragon().getRidingPlayer() != null ? event.getDragon().getRidingPlayer() : (event.getDragon().getOwner() instanceof EntityPlayerMP ? (EntityPlayerMP) event.getDragon().getOwner() : null), false));
    }

    @SubscribeEvent
    public void dragonBreak(DragonFireDamageWorldEvent event) {
        event.setCanceled(LandProtectionLogic.shouldCancelBlockBroken(event.getEntity().world, new BlockPos(event.getTargetX(), event.getTargetY(), event.getTargetZ()), event.getDragon().getRidingPlayer() != null ? event.getDragon().getRidingPlayer() : (event.getDragon().getOwner() instanceof EntityPlayerMP ? (EntityPlayerMP) event.getDragon().getOwner() : null), false));
    }

    @SubscribeEvent
    public void nondragonBreak(GenericGriefEvent event) {
        event.setCanceled(LandProtectionLogic.shouldCancelBlockBroken(event.getEntity().world, new BlockPos(event.getTargetX(), event.getTargetY(), event.getTargetZ()), (event.getEntity() instanceof EntityTameable && ((EntityTameable) event.getEntity()).getOwner() instanceof EntityPlayerMP) ? ((EntityPlayerMP)((EntityTameable) event.getEntity()).getOwner()) : null, false));
    }
}
