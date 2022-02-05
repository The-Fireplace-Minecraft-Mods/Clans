package dev.the_fireplace.clans.block;

import dev.the_fireplace.clans.ClansConstants;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Block;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public final class ClansBlockTags
{
    public static Tag.Identified<Block> LOCKABLE_BLOCKS = build("lockable_blocks");

    private static Tag.Identified<Block> build(String name) {
        return TagFactory.BLOCK.create(new Identifier(ClansConstants.MODID, name));
    }
}
