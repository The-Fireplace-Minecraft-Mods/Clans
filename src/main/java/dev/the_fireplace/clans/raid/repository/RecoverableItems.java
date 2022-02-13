package dev.the_fireplace.clans.raid.repository;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.api.raid.injectables.RecoverableItemRepository;
import dev.the_fireplace.lib.api.io.injectables.SaveBasedStorageWriter;
import dev.the_fireplace.lib.api.lazyio.injectables.SaveDataStateManager;
import net.minecraft.item.ItemStack;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Implementation
public final class RecoverableItems implements RecoverableItemRepository
{
    private final Map<UUID, PlayerRecoverableItems> recoverableItemsByPlayer = new ConcurrentHashMap<>();
    private final SaveDataStateManager saveDataStateManager;
    private final SaveBasedStorageWriter saveBasedStorageWriter;

    @Inject
    public RecoverableItems(SaveDataStateManager saveDataStateManager, SaveBasedStorageWriter saveBasedStorageWriter) {
        this.saveDataStateManager = saveDataStateManager;
        this.saveBasedStorageWriter = saveBasedStorageWriter;
    }

    @Override
    public boolean hasItems(UUID playerId) {
        return false;
    }

    @Override
    public Collection<ItemStack> getItems(UUID playerId) {
        return null;
    }

    @Override
    public void storeItem(UUID playerId, ItemStack itemStack) {

    }

    @Override
    public void removeItems(UUID playerId, Collection<ItemStack> itemStacks) {

    }

    private class PlayerRecoverableItems
    {

    }
}
