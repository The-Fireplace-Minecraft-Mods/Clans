package the_fireplace.clans.sponge.compat;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import the_fireplace.clans.abstraction.IMinecraftHelper;
import the_fireplace.clans.ClansSponge;

import javax.annotation.Nullable;

public class SpongeMinecraftHelper implements IMinecraftHelper {
    @Override
    public MinecraftServer getServer() {
        return (MinecraftServer)Sponge.getServer();//TODO test
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
        return new Integer[0];//TODO
    }

    @Nullable
    @Override
    public Block getBlock(ResourceLocation res) {
        return Block.getBlockFromName(res.toString());
    }

    @Override
    public boolean isAllowedNonPlayerEntity(Entity entity) {
        return false;
    }
}
