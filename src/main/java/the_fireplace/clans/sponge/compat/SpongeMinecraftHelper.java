package the_fireplace.clans.sponge.compat;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import the_fireplace.clans.ClansSponge;
import the_fireplace.clans.abstraction.IMinecraftHelper;

import javax.annotation.Nullable;
import java.util.List;

public class SpongeMinecraftHelper implements IMinecraftHelper {
    @Override
    public MinecraftServer getServer() {
        return (MinecraftServer)Sponge.getServer();
    }

    @Override
    public boolean isPluginLoaded(String id) {
        return Sponge.getPluginManager().isLoaded(id);
    }

    @Override
    public Logger getLogger() {
        return ClansSponge.logger;
    }

    @Override
    public Integer[] getDimensionIds() {
        List<Integer> dimIds = Lists.newArrayList();
        for(DimensionType type: DimensionType.values())
            dimIds.add(type.getId());
        return (Integer[]) dimIds.toArray();
    }

    @Nullable
    @Override
    public Block getBlock(ResourceLocation res) {
        return Block.getBlockFromName(res.toString());
    }

    @Override
    public boolean isAllowedNonPlayerEntity(Entity entity, boolean whenNotFakePlayer) {
        return whenNotFakePlayer;
    }
}
