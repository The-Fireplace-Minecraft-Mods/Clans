package the_fireplace.clans.commands.raiding;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.raid.RaidBlockPlacementDatabase;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.TranslationUtil;

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
		return TranslationUtil.getRawTranslationString(sender, "commands.raid.collect.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		if(RaidBlockPlacementDatabase.hasPlacedBlocks(sender.getUniqueID())){
			List<String> removeItems = Lists.newArrayList();
			for(String string: RaidBlockPlacementDatabase.getPlacedBlocks(sender.getUniqueID())) {
				ItemStack stack;
				try {
					stack = new ItemStack(JsonToNBT.getTagFromJson(string));
				} catch (NBTException e) {
					stack = null;
				}
				if (stack == null || sender.addItemStackToInventory(stack))
					removeItems.add(string);
			}
			RaidBlockPlacementDatabase.getInstance().removePlacedBlocks(sender.getUniqueID(), removeItems);
			if(RaidBlockPlacementDatabase.hasPlacedBlocks(sender.getUniqueID()))
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.collect.makespace").setStyle(TextStyles.YELLOW));
			else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.collect.success").setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.collect.empty").setStyle(TextStyles.RED));
	}
}
