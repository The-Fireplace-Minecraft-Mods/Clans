package the_fireplace.clans.commands.members;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandDecline extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return Clans.cfg.allowMultiClanMembership ? EnumRank.ANY : EnumRank.NOCLAN;
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
			for (Map.Entry<EntityPlayerMP, EnumRank> target : selectedClan.getOnlineMembers().entrySet())
				if(target.getValue().greaterOrEquals(EnumRank.ADMIN))
					target.getKey().sendMessage(TranslationUtil.getTranslation(target.getKey().getUniqueID(), "commands.clan.decline.declined", sender.getDisplayName(), selectedClan.getClanName()).setStyle(TextStyles.YELLOW));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.accept.no_invites").setStyle(TextStyles.RED));
	}
}
