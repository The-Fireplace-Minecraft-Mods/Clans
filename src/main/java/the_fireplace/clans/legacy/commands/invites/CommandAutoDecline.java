package the_fireplace.clans.legacy.commands.invites;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.ClansModContainer;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.clan.ClanNameCache;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.player.InvitedPlayers;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
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
		return ClansModContainer.getConfig().isAllowMultiClanMembership() ? EnumRank.ANY : EnumRank.NOCLAN;
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
			boolean willNowBlockAll = !InvitedPlayers.isBlockingAllInvites(sender.getUniqueID());
			InvitedPlayers.setGlobalInviteBlock(sender.getUniqueID(), willNowBlockAll);
			if(willNowBlockAll)
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autodecline.on").setStyle(TextStyles.GREEN));
			else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autodecline.off").setStyle(TextStyles.GREEN));
		} else {
			Clan c = ClanNameCache.getClanByName(args[0]);
			if(c != null)
				toggleClanInviteBlock(sender, c.getId());
			else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.notfound", args[0]).setStyle(TextStyles.RED));
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, ClanNameCache.getClanNames()) : Collections.emptyList();
	}

	public static void toggleClanInviteBlock(EntityPlayerMP sender, UUID clanId) {
		boolean willNowBlock = !InvitedPlayers.getBlockedClans(sender.getUniqueID()).contains(clanId);
		if(willNowBlock) {
			InvitedPlayers.addInviteBlock(sender.getUniqueID(), clanId);
			InvitedPlayers.removeInvite(sender.getUniqueID(), clanId);
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autodecline.on_clan", Objects.requireNonNull(ClanDatabase.getClanById(clanId)).getName()).setStyle(TextStyles.GREEN));
		} else {
			InvitedPlayers.removeInviteBlock(sender.getUniqueID(), clanId);
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autodecline.off_clan", Objects.requireNonNull(ClanDatabase.getClanById(clanId)).getName()).setStyle(TextStyles.GREEN));
		}
	}
}
