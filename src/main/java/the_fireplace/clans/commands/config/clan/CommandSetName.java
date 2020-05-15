package the_fireplace.clans.commands.config.clan;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.ClansHelper;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSetName extends ClanSubCommand {
	@Override
	public String getName() {
		return "setname";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.LEADER;
	}

	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}

	@Override
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
		String newName = args[0];
		if(ClansHelper.getConfig().getMaxNameLength() > 0 && newName.length() > ClansHelper.getConfig().getMaxNameLength())
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setname.toolong", ClansHelper.getConfig().getMaxNameLength()).setStyle(TextStyles.RED));
		else if(!ClanCache.clanNameTaken(newName)) {
			String oldName = selectedClan.getName();
			selectedClan.setName(newName);
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setname.success", oldName, newName).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setname.taken", newName).setStyle(TextStyles.RED));
	}
}
