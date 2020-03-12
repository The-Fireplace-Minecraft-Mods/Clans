package the_fireplace.clans.forge.compat;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.Logger;
import the_fireplace.clans.Clans;
import the_fireplace.clans.forge.FakePlayerUtil;

import javax.annotation.Nullable;

public class ForgeMinecraftHelper {
    public MinecraftServer getServer() {
        return FMLCommonHandler.instance().getMinecraftServerInstance();
    }

    public boolean isPluginLoaded(String id) {
        return Loader.isModLoaded(id);
    }

    public Logger getLogger() {
        return Clans.getLogger();
    }

    public Integer[] getDimensionIds() {
        return DimensionManager.getIDs();
    }

    @Nullable
    public Block getBlock(ResourceLocation res) {
        return ForgeRegistries.BLOCKS.getValue(res);
    }

    public boolean isAllowedNonPlayerEntity(@Nullable Entity entity, boolean ifNotFakePlayer) {
        return FakePlayerUtil.isAllowedFakePlayer(entity, ifNotFakePlayer);
    }
}
