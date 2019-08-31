package the_fireplace.clans.abstraction;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public interface IMinecraftHelper {
    MinecraftServer getServer();
    boolean isPluginLoaded(String id);
    Logger getLogger();
    Integer[] getDimensionIds();
    @Nullable
    Block getBlock(ResourceLocation res);
    boolean isAllowedNonPlayerEntity(@Nullable Entity entity, boolean ifNotFakePlayer);
}
