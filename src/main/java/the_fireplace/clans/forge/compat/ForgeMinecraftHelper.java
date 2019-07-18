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
import the_fireplace.clans.abstraction.IMinecraftHelper;
import the_fireplace.clans.forge.ClansForge;
import the_fireplace.clans.forge.FakePlayerUtil;

import javax.annotation.Nullable;

public class ForgeMinecraftHelper implements IMinecraftHelper {
    @Override
    public MinecraftServer getServer() {
        return FMLCommonHandler.instance().getMinecraftServerInstance();
    }

    @Override
    public boolean isPluginLoaded(String id) {
        return Loader.isModLoaded(id);
    }

    @Override
    public Logger getLogger() {
        return ClansForge.getLogger();
    }

    @Override
    public Integer[] getDimensionIds() {
        return DimensionManager.getIDs();
    }

    @Nullable
    @Override
    public Block getBlock(ResourceLocation res) {
        return ForgeRegistries.BLOCKS.getValue(res);
    }

    @Override
    public boolean isAllowedNonPlayerEntity(Entity entity) {
        return FakePlayerUtil.isAllowedFakePlayer(entity);
    }
}
