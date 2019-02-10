package the_fireplace.clans.commands.raiding;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.raid.RaidBlockPlacementDatabase;
import the_fireplace.clans.util.MinecraftColors;

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
		if(RaidBlockPlacementDatabase.hasPlacedBlocks(sender.getUniqueID())){
			List<ItemStack> removeItems = Lists.newArrayList();
			for(ItemStack stack: RaidBlockPlacementDatabase.getPlacedBlocks(sender.getUniqueID()))
				if(sender.addItemStackToInventory(stack))
					removeItems.add(stack);
			RaidBlockPlacementDatabase.getInstance().removePlacedBlocks(sender.getUniqueID(), removeItems);
			if(RaidBlockPlacementDatabase.hasPlacedBlocks(sender.getUniqueID()))
				sender.sendMessage(new TextComponentString(MinecraftColors.YELLOW + "You have run out of room for collection. Make room in your inventory and try again."));
			else
				sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "Collection successful."));
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "You don't have anything to collect."));
	}
}
