package dev.the_fireplace.clans.api.raid.injectables;

import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.UUID;

/**
 * Tracks all items that were confiscated from or used by players that are able to be retrieved after the raid is completed.
 */
public interface RecoverableItemRepository
{
    boolean hasItems(UUID playerId);

    Collection<ItemStack> getItems(UUID playerId);

    void storeItem(UUID playerId, ItemStack itemStack);

    void removeItems(UUID playerId, Collection<ItemStack> itemStacks);
}
