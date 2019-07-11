package the_fireplace.clans.commands.members;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.Clans;
import the_fireplace.clans.abstraction.IConfig;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandDecline extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return Clans.getConfig().isAllowMultiClanMembership() ? EnumRank.ANY : EnumRank.NOCLAN;
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
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.decline.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Clan declineClan = ClanCache.removeInvite(sender.getUniqueID());
		if(declineClan != null) {
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.decline.success", declineClan.getClanName()).setStyle(TextStyles.GREEN));
			declineClan.messageAllOnline(EnumRank.ADMIN, TextStyles.YELLOW, "commands.clan.decline.declined", sender.getDisplayNameString(), declineClan.getClanName());
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.accept.no_invites").setStyle(TextStyles.RED));
	}
}
