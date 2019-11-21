package the_fireplace.clans.commands.members;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.data.PlayerData;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandInvite extends ClanSubCommand {
	@Override
	public String getName() {
		return "invite";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.ADMIN;
	}

	@Override
	public int getMinArgs() {
		return 2;
	}

	@Override
	public int getMaxArgs() {
		return 2;
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		assert server != null;
		switch(args[0].toLowerCase()) {
			case "revoke":
			case "r":
				revokeInvite(server, sender, args[1], selectedClan);
				break;
			case "send":
			case "s":
			default:
				invitePlayer(server, sender, args[1], selectedClan);
		}
	}

	public static void invitePlayer(@Nonnull MinecraftServer server, EntityPlayerMP sender, String inviteTarget, Clan invitingClan) throws PlayerNotFoundException {
		GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(inviteTarget);
		if(target != null) {
			if (Clans.getConfig().isAllowMultiClanMembership() || ClanCache.getPlayerClans(target.getId()).isEmpty()) {
				if (!ClanCache.getPlayerClans(target.getId()).contains(invitingClan)) {
					if(PlayerData.getIsBlockingAllInvites(target.getId())) {
						sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.blocking_all", target.getName()).setStyle(TextStyles.RED));
					} else if(PlayerData.getBlockedClans(target.getId()).contains(invitingClan.getId())) {
						sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.blocking", target.getName(), invitingClan.getName()).setStyle(TextStyles.RED));
					} else if(PlayerData.getInvites(target.getId()).contains(invitingClan.getId())) {
						sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.pending", target.getName(), invitingClan.getName()).setStyle(TextStyles.RED));
					} else {
						PlayerData.addInvite(target.getId(), invitingClan.getId());
						sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.success", target.getName(), invitingClan.getName()).setStyle(TextStyles.GREEN));

						if(ArrayUtils.contains(server.getOnlinePlayerProfiles(), target)) {
							EntityPlayerMP targetEntity = server.getPlayerList().getPlayerByUUID(target.getId());
							targetEntity.sendMessage(TranslationUtil.getTranslation(target.getId(), "commands.clan.invite.invited", invitingClan.getName()).setStyle(TextStyles.GREEN));
						}
					}
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.already_in_this", target.getName(), invitingClan.getName()).setStyle(TextStyles.RED));
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.already_in_any", target.getName()).setStyle(TextStyles.RED));
		} else
			throw new PlayerNotFoundException("commands.generic.player.notFound", inviteTarget);
	}

	public static void revokeInvite(@Nonnull MinecraftServer server, EntityPlayerMP sender, String inviteTarget, Clan revokingClan) throws PlayerNotFoundException {
		GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(inviteTarget);
		if(target != null) {
			if(!PlayerData.getInvites(target.getId()).contains(revokingClan.getId())) {
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.not_pending", target.getName(), revokingClan.getName()).setStyle(TextStyles.RED));
			} else {
				PlayerData.removeInvite(target.getId(), revokingClan.getId());
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.revoke_success", target.getName(), revokingClan.getName()).setStyle(TextStyles.GREEN));

				if(ArrayUtils.contains(server.getOnlinePlayerProfiles(), target)) {
					EntityPlayerMP targetEntity = server.getPlayerList().getPlayerByUUID(target.getId());
					targetEntity.sendMessage(TranslationUtil.getTranslation(target.getId(), "commands.clan.invite.revoked", revokingClan.getName()).setStyle(TextStyles.GREEN));
				}
			}
		} else
			throw new PlayerNotFoundException("commands.generic.player.notFound", inviteTarget);
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		if(args.length != 1)
			return Collections.emptyList();
		ArrayList<GameProfile> players = Lists.newArrayList(server.getPlayerList().getOnlinePlayerProfiles());
		if(!Clans.getConfig().isAllowMultiClanMembership())
			players.removeIf(s -> !ClanCache.getPlayerClans(s.getId()).isEmpty());
		players.removeIf(s -> ClanCache.getPlayerClans(s.getId()).contains(selectedClan));
		ArrayList<String> playerNames = Lists.newArrayList();
		for(GameProfile profile: players)
			playerNames.add(profile.getName());
		return playerNames;
	}
}
