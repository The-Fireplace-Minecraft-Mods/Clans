package dev.the_fireplace.clans.raid.model;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.the_fireplace.clans.ClansConstants;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageReadBuffer;
import dev.the_fireplace.lib.api.io.interfaces.access.StorageWriteBuffer;
import dev.the_fireplace.lib.api.lazyio.interfaces.SaveData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ItemRecoveryData implements SaveData
{
    private final UUID playerId;
    private final List<ItemStack> recoverableItems = new ArrayList<>();

    public ItemRecoveryData(UUID playerId) {
        this.playerId = playerId;
    }

    public List<ItemStack> getRecoverableItems() {
        return recoverableItems;
    }

    public boolean hasItems() {
        return !recoverableItems.isEmpty();
    }

    public void addStack(ItemStack stack) {
        this.recoverableItems.add(stack);
    }

    public void removeStack(ItemStack removeStack) {
        removeStack = removeStack.copy();
        for (ItemStack recoverableStack : recoverableItems) {
            if (ItemStack.canCombine(removeStack, recoverableStack)) {
                if (recoverableStack.getCount() > removeStack.getCount()) {
                    recoverableStack.decrement(removeStack.getCount());
                    return;
                } else if (recoverableStack.getCount() < removeStack.getCount()) {
                    removeStack.decrement(recoverableStack.getCount());
                    recoverableStack.setCount(0);
                    recoverableItems.remove(recoverableStack);
                } else {
                    recoverableItems.remove(recoverableStack);
                    return;
                }
            }
        }
        ClansConstants.LOGGER.warn("Didn't remove enough items from recoverable item repo! Remaining: {}", removeStack.getCount());
    }

    @Override
    public void readFrom(StorageReadBuffer buffer) {
        List<String> serializedStacks = buffer.readStringList("recoverableStacks", new ArrayList<>());
        for (String serializedStack : serializedStacks) {
            try {
                recoverableItems.add(ItemStack.fromNbt(StringNbtReader.parse(serializedStack)));
            } catch (CommandSyntaxException e) {
                ClansConstants.LOGGER.error("Unable to parse recoverable itemstack! " + serializedStack, e);
            }
        }
    }

    @Override
    public void writeTo(StorageWriteBuffer buffer) {
        List<String> serializedStacks = new ArrayList<>();
        for (ItemStack stack : recoverableItems) {
            serializedStacks.add(stack.writeNbt(new NbtCompound()).asString());
        }
        buffer.writeStringList("recoverableStacks", serializedStacks);
    }

    @Override
    public String getDatabase() {
        return ClansConstants.MODID;
    }

    @Override
    public String getTable() {
        return "recoverable_items";
    }

    @Override
    public String getId() {
        return playerId.toString();
    }
}
