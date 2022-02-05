package dev.the_fireplace.clans.config.defaults;

import com.google.common.collect.Lists;
import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.domain.config.PerClanProtectionConfig;

import java.util.List;

@Implementation(name = "default")
public final class PerClanProtectionConfigDefaults implements PerClanProtectionConfig
{
    @Override
    public boolean isForceConnectedClaims() {
        return true;
    }

    @Override
    public boolean isEnableBorderlands() {
        return true;
    }

    @Override
    public boolean isPreventMobsOnClaims() {
        return true;
    }

    @Override
    public boolean isPreventMobsOnBorderlands() {
        return true;
    }

    @Override
    public boolean isAllowTntChainingOnClaims() {
        return true;
    }

    @Override
    public List<String> getLockableBlocks() {
        return Lists.newArrayList(//TODO use a block tag for this maybe?
            "minecraft:chest",
            "minecraft:furnace",
            "minecraft:lit_furnace",
            "minecraft:jukebox",
            "minecraft:white_shulker_box",
            "minecraft:orange_shulker_box",
            "minecraft:magenta_shulker_box",
            "minecraft:light_blue_shulker_box",
            "minecraft:yellow_shulker_box",
            "minecraft:lime_shulker_box",
            "minecraft:pink_shulker_box",
            "minecraft:gray_shulker_box",
            "minecraft:silver_shulker_box",
            "minecraft:cyan_shulker_box",
            "minecraft:purple_shulker_box",
            "minecraft:blue_shulker_box",
            "minecraft:brown_shulker_box",
            "minecraft:green_shulker_box",
            "minecraft:red_shulker_box",
            "minecraft:black_shulker_box",
            "minecraft:bed",
            "minecraft:dispenser",
            "minecraft:lever",
            "minecraft:stone_pressure_plate",
            "minecraft:wooden_pressure_plate",
            "minecraft:stone_button",
            "minecraft:trapdoor",
            "minecraft:fence_gate",
            "minecraft:wooden_button",
            "minecraft:trapped_chest",
            "minecraft:daylight_detector",
            "minecraft:hopper",
            "minecraft:dropper",
            "minecraft:spruce_fence_gate",
            "minecraft:birch_fence_gate",
            "minecraft:jungle_fence_gate",
            "minecraft:dark_oak_fence_gate",
            "minecraft:acacia_fence_gate",
            "minecraft:wooden_door",
            "minecraft:repeater",
            "minecraft:comparator",
            "minecraft:spruce_door",
            "minecraft:birch_door",
            "minecraft:jungle_door",
            "minecraft:acacia_door",
            "minecraft:dark_oak_door",
            "minecraft:beacon",
            "minecraft:brewing_stand"
        );
    }
}
