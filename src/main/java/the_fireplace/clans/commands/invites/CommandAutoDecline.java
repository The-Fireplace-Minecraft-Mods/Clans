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
import java.util.Objects;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandAutoDecline extends ClanSubCommand {
	@Override
	public String getName() {
		return "autodecline";
	}

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
		return 1;
	}

	@Override
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
		if(args.length == 0) {
			boolean willNowBlockAll = !PlayerData.getIsBlockingAllInvites(sender.getUniqueID());
			PlayerData.setGlobalInviteBlock(sender.getUniqueID(), willNowBlockAll);
			if(willNowBlockAll)
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autodecline.on").setStyle(TextStyles.GREEN));
			else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autodecline.off").setStyle(TextStyles.GREEN));
		} else {
			Clan c = ClanCache.getClanByName(args[0]);
			if(c != null)
				toggleClanInviteBlock(sender, c.getId());
			else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.notfound", args[0]).setStyle(TextStyles.RED));
		}
	}

	public static void toggleClanInviteBlock(EntityPlayerMP sender, UUID clanId) {
		boolean willNowBlock = !PlayerData.getBlockedClans(sender.getUniqueID()).contains(clanId);
		if(willNowBlock) {
			PlayerData.addInviteBlock(sender.getUniqueID(), clanId);
			PlayerData.removeInvite(sender.getUniqueID(), clanId);
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autodecline.on_clan", Objects.requireNonNull(ClanCache.getClanById(clanId)).getName()).setStyle(TextStyles.GREEN));
		} else {
			PlayerData.removeInviteBlock(sender.getUniqueID(), clanId);
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autodecline.off_clan", Objects.requireNonNull(ClanCache.getClanById(clanId)).getName()).setStyle(TextStyles.GREEN));
		}
	}
}
