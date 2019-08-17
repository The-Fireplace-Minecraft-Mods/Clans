package the_fireplace.clans.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandDisband extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.LEADER;
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
	public String getUsage(ICommandSender sender) {
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.disband.usage");
	}

	@Override
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
		if(selectedClan.getMembers().get(sender.getUniqueID()).equals(EnumRank.LEADER)) {
			if(!selectedClan.isLimitless()) {
				selectedClan.disband(server, sender, "commands.clan.disband.disbanded", selectedClan.getClanName(), sender.getName());
				sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.disband.success", selectedClan.getClanName()).setStyle(TextStyles.GREEN));
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.disband.limitless", selectedClan.getClanName()).setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.not_leader", selectedClan.getClanName()).setStyle(TextStyles.RED));
	}
}
