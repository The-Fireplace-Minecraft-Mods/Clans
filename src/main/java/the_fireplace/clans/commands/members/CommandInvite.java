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
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

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
		return 1;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		assert server != null;
		GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(args[0]);
		if(target != null) {
			if (Clans.getConfig().isAllowMultiClanMembership() || ClanCache.getPlayerClans(target.getId()).isEmpty()) {
				if (!ClanCache.getPlayerClans(target.getId()).contains(selectedClan)) {
					if(PlayerData.getIsBlockingAllInvites(target.getId())) {
						sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.blocking_all", target.getName()).setStyle(TextStyles.RED));
					} else if(PlayerData.getBlockedClans(target.getId()).contains(selectedClan.getId())) {
						sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.blocking", target.getName(), selectedClan.getName()).setStyle(TextStyles.RED));
					} else if(PlayerData.getInvites(target.getId()).contains(selectedClan.getId())) {
						sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.pending", target.getName(), selectedClan.getName()).setStyle(TextStyles.RED));
					} else {
						PlayerData.addInvite(target.getId(), selectedClan.getId());
						sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.success", target.getName(), selectedClan.getName()).setStyle(TextStyles.GREEN));

						if(ArrayUtils.contains(server.getOnlinePlayerProfiles(), target)) {
							EntityPlayerMP targetEntity = server.getPlayerList().getPlayerByUUID(target.getId());
							targetEntity.sendMessage(TranslationUtil.getTranslation(target.getId(), "commands.clan.invite.invited", selectedClan.getName()).setStyle(TextStyles.GREEN));
						}
					}
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.already_in_this", target.getName(), selectedClan.getName()).setStyle(TextStyles.RED));
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.already_in_any", target.getName()).setStyle(TextStyles.RED));
		} else
			throw new PlayerNotFoundException("commands.generic.player.notFound", args[0]);
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
