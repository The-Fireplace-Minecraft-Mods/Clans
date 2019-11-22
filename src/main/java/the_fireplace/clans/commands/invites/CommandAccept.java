package the_fireplace.clans.commands.invites;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.data.PlayerData;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandAccept extends ClanSubCommand {
	@Override
	public String getName() {
		return "accept";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return Clans.getConfig().isAllowMultiClanMembership() ? EnumRank.ANY : EnumRank.NOCLAN;
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
		Clan acceptClan = ClanCache.getClanByName(args[0]);
		if(acceptClan != null) {
			if(PlayerData.getInvites(sender.getUniqueID()).contains(acceptClan.getId())) {
				acceptClan.addMember(sender.getUniqueID());
				if (ClanCache.getPlayerClans(sender.getUniqueID()).size() == 1)
					PlayerData.setDefaultClan(sender.getUniqueID(), acceptClan.getId());
				PlayerData.removeInvite(sender.getUniqueID(), acceptClan.getId());
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.accept.success", acceptClan.getName()).setStyle(TextStyles.GREEN));
				acceptClan.messageAllOnline(sender, TextStyles.GREEN, "commands.clan.accept.accepted", sender.getDisplayNameString(), acceptClan.getName());
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.not_invited", args[0]).setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.notfound", args[0]).setStyle(TextStyles.RED));
	}
}
