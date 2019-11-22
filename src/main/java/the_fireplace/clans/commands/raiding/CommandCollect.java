package the_fireplace.clans.commands.raiding;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.cache.RaidingParties;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.data.RaidCollectionDatabase;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandCollect extends RaidSubCommand {
	@Override
	public String getName() {
		return "collect";
	}

	@Override
	public int getMinArgs() {
		return 0;
	}

	@Override
	public int getMaxArgs() {
		return 0;
	}

	@Override
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
		if(!RaidingParties.getRaidingPlayers().contains(sender.getUniqueID())) {
			if (RaidCollectionDatabase.hasCollectItems(sender.getUniqueID())) {
				List<String> removeItems = Lists.newArrayList();
				for (String string : RaidCollectionDatabase.getCollectItems(sender.getUniqueID())) {
					ItemStack stack;
					try {
						stack = new ItemStack(JsonToNBT.getTagFromJson(string));
					} catch (NBTException e) {
						stack = null;
					}
					if (stack == null || sender.addItemStackToInventory(stack))
						removeItems.add(string);
				}
				RaidCollectionDatabase.getInstance().removeCollectItems(sender.getUniqueID(), removeItems);
				if (RaidCollectionDatabase.hasCollectItems(sender.getUniqueID()))
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.collect.makespace").setStyle(TextStyles.YELLOW));
				else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.collect.success").setStyle(TextStyles.GREEN));
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.collect.empty").setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.collect.raiding").setStyle(TextStyles.RED));
	}
}
