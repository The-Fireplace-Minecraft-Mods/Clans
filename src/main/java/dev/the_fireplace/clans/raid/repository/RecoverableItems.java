package dev.the_fireplace.clans.raid.repository;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.clans.api.raid.injectables.RecoverableItemRepository;
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
    private final Map<UUID, ItemRecoveryData> recoverableItemsByPlayer = new ConcurrentHashMap<>();
    private final SaveDataStateManager saveDataStateManager;

    @Inject
    public RecoverableItems(SaveDataStateManager saveDataStateManager) {
        this.saveDataStateManager = saveDataStateManager;
    }

    @Override
    public boolean hasItems(UUID playerId) {
        return getRecoveryData(playerId).hasItems();
    }

    @Override
    public Collection<ItemStack> getItems(UUID playerId) {
        return getRecoveryData(playerId).getRecoverableItems();
    }

    @Override
    public void storeItem(UUID playerId, ItemStack itemStack) {
        getRecoveryData(playerId).addStack(itemStack);
    }

    @Override
    public void removeItems(UUID playerId, Collection<ItemStack> itemStacks) {
        ItemRecoveryData recoveryData = getRecoveryData(playerId);
        for (ItemStack stack : itemStacks) {
            recoveryData.removeStack(stack);
        }
    }

    private ItemRecoveryData getRecoveryData(UUID playerId) {
        if (recoverableItemsByPlayer.containsKey(playerId)) {
            return recoverableItemsByPlayer.get(playerId);
        }

        return initializeRecoveryData(playerId);
    }

    private ItemRecoveryData initializeRecoveryData(UUID playerId) {
        ItemRecoveryData recoverableItemData = new ItemRecoveryData(playerId);
        saveDataStateManager.initializeWithAutosave(recoverableItemData, (short) 5);
        recoverableItemsByPlayer.put(playerId, recoverableItemData);
        return recoverableItemData;
    }

    //TODO when player disconnects
    public void tearDown(UUID playerId) {
        ItemRecoveryData recoverableItemData = recoverableItemsByPlayer.remove(playerId);
        if (recoverableItemData != null) {
            this.saveDataStateManager.tearDown(recoverableItemData);
        }
    }
}
