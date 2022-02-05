package dev.the_fireplace.clans.datagen;

import dev.the_fireplace.clans.block.ClansBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.server.AbstractTagProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.nio.file.Path;

public final class BlockTagsProvider extends AbstractTagProvider<Block>
{
    public BlockTagsProvider(DataGenerator root) {
        super(root, Registry.BLOCK);
    }

    @Override
    protected void configure() {
        this.getOrCreateTagBuilder(ClansBlockTags.LOCKABLE_BLOCKS).add(
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.FURNACE,
            Blocks.BLAST_FURNACE,
            Blocks.LOOM,
            Blocks.BARREL,
            Blocks.BEEHIVE,
            Blocks.BREWING_STAND,
            Blocks.CAMPFIRE,
            Blocks.SOUL_CAMPFIRE,
            Blocks.CAULDRON,
            Blocks.LAVA_CAULDRON,
            Blocks.POWDER_SNOW_CAULDRON,
            Blocks.WATER_CAULDRON,
            Blocks.DISPENSER,
            Blocks.DROPPER,
            Blocks.FLOWER_POT,
            Blocks.HOPPER,
            Blocks.JUKEBOX,
            Blocks.LECTERN,
            Blocks.SHULKER_BOX,
            Blocks.WHITE_SHULKER_BOX,
            Blocks.ORANGE_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX,
            Blocks.LIME_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX,
            Blocks.GRAY_SHULKER_BOX,
            Blocks.LIGHT_GRAY_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX,
            Blocks.PURPLE_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX,
            Blocks.BROWN_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX,
            Blocks.RED_SHULKER_BOX,
            Blocks.BLACK_SHULKER_BOX,
            Blocks.SMOKER,
            Blocks.WHITE_BED,
            Blocks.ORANGE_BED,
            Blocks.MAGENTA_BED,
            Blocks.LIGHT_BLUE_BED,
            Blocks.YELLOW_BED,
            Blocks.LIME_BED,
            Blocks.PINK_BED,
            Blocks.GRAY_BED,
            Blocks.LIGHT_GRAY_BED,
            Blocks.CYAN_BED,
            Blocks.PURPLE_BED,
            Blocks.BLUE_BED,
            Blocks.BROWN_BED,
            Blocks.GREEN_BED,
            Blocks.RED_BED,
            Blocks.BLACK_BED,
            Blocks.LEVER,
            Blocks.BIRCH_BUTTON,
            Blocks.ACACIA_BUTTON,
            Blocks.CRIMSON_BUTTON,
            Blocks.DARK_OAK_BUTTON,
            Blocks.JUNGLE_BUTTON,
            Blocks.OAK_BUTTON,
            Blocks.SPRUCE_BUTTON,
            Blocks.WARPED_BUTTON,
            Blocks.STONE_BUTTON,
            Blocks.POLISHED_BLACKSTONE_BUTTON,
            Blocks.BIRCH_PRESSURE_PLATE,
            Blocks.ACACIA_PRESSURE_PLATE,
            Blocks.CRIMSON_PRESSURE_PLATE,
            Blocks.DARK_OAK_PRESSURE_PLATE,
            Blocks.JUNGLE_PRESSURE_PLATE,
            Blocks.OAK_PRESSURE_PLATE,
            Blocks.SPRUCE_PRESSURE_PLATE,
            Blocks.WARPED_PRESSURE_PLATE,
            Blocks.STONE_PRESSURE_PLATE,
            Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE,
            Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Blocks.LEVER,
            Blocks.BIRCH_DOOR,
            Blocks.ACACIA_DOOR,
            Blocks.CRIMSON_DOOR,
            Blocks.DARK_OAK_DOOR,
            Blocks.JUNGLE_DOOR,
            Blocks.OAK_DOOR,
            Blocks.SPRUCE_DOOR,
            Blocks.WARPED_DOOR,
            Blocks.BIRCH_TRAPDOOR,
            Blocks.ACACIA_TRAPDOOR,
            Blocks.CRIMSON_TRAPDOOR,
            Blocks.DARK_OAK_TRAPDOOR,
            Blocks.JUNGLE_TRAPDOOR,
            Blocks.OAK_TRAPDOOR,
            Blocks.SPRUCE_TRAPDOOR,
            Blocks.WARPED_TRAPDOOR,
            Blocks.BIRCH_FENCE_GATE,
            Blocks.ACACIA_FENCE_GATE,
            Blocks.CRIMSON_FENCE_GATE,
            Blocks.DARK_OAK_FENCE_GATE,
            Blocks.JUNGLE_FENCE_GATE,
            Blocks.OAK_FENCE_GATE,
            Blocks.SPRUCE_FENCE_GATE,
            Blocks.WARPED_FENCE_GATE,
            Blocks.DAYLIGHT_DETECTOR,
            Blocks.REPEATER,
            Blocks.COMPARATOR,
            Blocks.BEACON,
            Blocks.COMMAND_BLOCK,
            Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK,
            Blocks.ANVIL,
            Blocks.CHIPPED_ANVIL,
            Blocks.DAMAGED_ANVIL,
            Blocks.BELL,
            Blocks.CARTOGRAPHY_TABLE,
            Blocks.COMPOSTER
        );
    }

    @Override
    protected Path getOutput(Identifier id) {
        return this.root.getOutput().resolve("data/" + id.getNamespace() + "/tags/blocks/" + id.getPath() + ".json");
    }

    @Override
    public String getName() {
        return "Clans Block Tags";
    }
}
