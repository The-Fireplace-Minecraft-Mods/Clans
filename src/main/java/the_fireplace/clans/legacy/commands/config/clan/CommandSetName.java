package the_fireplace.clans.legacy.commands.config.clan;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.config.Config;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

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
		if(Config.getInstance().chatCensor.censorClanNames)
			newName = ClansModContainer.getChatCensorCompat().getCensoredString(newName);
		if(ClansModContainer.getConfig().getMaxNameLength() > 0 && newName.length() > ClansModContainer.getConfig().getMaxNameLength())
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setname.toolong", ClansModContainer.getConfig().getMaxNameLength()).setStyle(TextStyles.RED));
		else if(ClanNames.isClanNameAvailable(newName)) {
            String oldName = selectedClanName;
            ClanNames.get(selectedClan).setName(newName);
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setname.success", oldName, newName).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setname.taken", newName).setStyle(TextStyles.RED));
	}
}
