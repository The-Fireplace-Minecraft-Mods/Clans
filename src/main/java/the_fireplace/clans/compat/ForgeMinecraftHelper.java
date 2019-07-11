package the_fireplace.clans.compat;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.Logger;
import the_fireplace.clans.abstraction.IMinecraftHelper;
import the_fireplace.clans.forge.ClansForge;

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
}
