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
import the_fireplace.clans.util.CapHelper;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandAccept extends ClanSubCommand {
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
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.accept.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Clan acceptClan = ClanCache.getInvite(sender.getUniqueID());
		if(acceptClan != null){
			acceptClan.addMember(sender.getUniqueID());
			if(ClanCache.getPlayerClans(sender.getUniqueID()).size() == 1)
				CapHelper.getPlayerClanCapability(sender).setDefaultClan(acceptClan.getClanId());
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.accept.success", acceptClan.getClanName()).setStyle(TextStyles.GREEN));
			acceptClan.messageAllOnline(sender, TextStyles.GREEN, "commands.clan.accept.accepted", sender.getDisplayNameString(), acceptClan.getClanName());
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.accept.no_invites").setStyle(TextStyles.RED));
	}
}
