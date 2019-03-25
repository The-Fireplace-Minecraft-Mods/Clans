package the_fireplace.clans.commands.raiding;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.raid.NewRaidBlockPlacementDatabase;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandCollect extends RaidSubCommand {
	@Override
	public int getMinArgs() {
		return 0;
	}

	@Override
	public int getMaxArgs() {
		return 0;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/raid collect";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		if(NewRaidBlockPlacementDatabase.hasPlacedBlocks(sender.getUniqueID())){
			List<String> removeItems = Lists.newArrayList();
			for(String string: NewRaidBlockPlacementDatabase.getPlacedBlocks(sender.getUniqueID())) {
				ItemStack stack;
				try {
					stack = new ItemStack(JsonToNBT.getTagFromJson(string));
				} catch (NBTException e) {
					stack = null;
				}
				if (stack == null || sender.addItemStackToInventory(stack))
					removeItems.add(string);
			}
			NewRaidBlockPlacementDatabase.getInstance().removePlacedBlocks(sender.getUniqueID(), removeItems);
			if(NewRaidBlockPlacementDatabase.hasPlacedBlocks(sender.getUniqueID()))
				sender.sendMessage(new TextComponentString("You have run out of room for collection. Make room in your inventory and try again.").setStyle(TextStyles.YELLOW));
			else
				sender.sendMessage(new TextComponentString("Collection successful.").setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(new TextComponentString("You don't have anything to collect.").setStyle(TextStyles.RED));
	}
}
