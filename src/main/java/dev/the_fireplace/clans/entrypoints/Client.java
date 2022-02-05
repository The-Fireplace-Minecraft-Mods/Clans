package dev.the_fireplace.clans.entrypoints;

import com.google.inject.Injector;
import dev.the_fireplace.annotateddi.api.entrypoints.ClientDIModInitializer;
import dev.the_fireplace.clans.ClansConstants;
import dev.the_fireplace.clans.datagen.BlockTagsProvider;
import dev.the_fireplace.lib.api.datagen.injectables.DataGeneratorFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.data.DataGenerator;

import java.io.IOException;
import java.nio.file.Paths;

@Environment(EnvType.CLIENT)
public final class Client implements ClientDIModInitializer
{
    @Override
    public void onInitializeClient(Injector diContainer) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            ClansConstants.LOGGER.debug("Generating data...");
            DataGenerator gen = diContainer.getInstance(DataGeneratorFactory.class).createAdditive(Paths.get("..", "src", "main", "resources"));
            gen.addProvider(new BlockTagsProvider(gen));

            try {
                gen.run();
            } catch (IOException e) {
                ClansConstants.LOGGER.error(e);
            }
        }
    }
}
