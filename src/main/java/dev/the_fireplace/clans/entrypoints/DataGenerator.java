package dev.the_fireplace.clans.entrypoints;

import dev.the_fireplace.annotateddi.api.DIContainer;
import dev.the_fireplace.clans.ClansConstants;
import dev.the_fireplace.clans.datagen.BlockTagsProvider;
import dev.the_fireplace.lib.api.datagen.injectables.DataGeneratorFactory;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Paths;

public final class DataGenerator implements DataGeneratorEntrypoint
{
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            ClansConstants.LOGGER.debug("Generating data...");
            net.minecraft.data.DataGenerator gen = DIContainer.get().getInstance(DataGeneratorFactory.class).createAdditive(Paths.get("..", "src", "main", "resources"));
            gen.addProvider(new BlockTagsProvider(gen));

            try {
                gen.run();
            } catch (IOException e) {
                ClansConstants.LOGGER.error(e);
            }
        }
    }
}
