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
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandInvite extends ClanSubCommand {
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
	public String getUsage(ICommandSender sender) {
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.invite.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		assert server != null;
		GameProfile targetProfile = server.getPlayerProfileCache().getGameProfileForUsername(args[0]);
		EntityPlayerMP target = targetProfile != null ? server.getPlayerList().getPlayerByUUID(targetProfile.getId()) : null;
		if(target != null) {
			if (Clans.getConfig().isAllowMultiClanMembership() || ClanCache.getPlayerClans(target.getUniqueID()).isEmpty()) {
				if (!ClanCache.getPlayerClans(target.getUniqueID()).contains(selectedClan)) {
					if (ClanCache.inviteToClan(target.getUniqueID(), selectedClan)) {
						sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.success", target.getDisplayNameString(), selectedClan.getClanName()).setStyle(TextStyles.GREEN));
						target.sendMessage(TranslationUtil.getTranslation(target.getUniqueID(), "commands.clan.invite.invited", selectedClan.getClanName()).setStyle(TextStyles.GREEN));
					} else {
						sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.pending", target.getName()).setStyle(TextStyles.RED));
						target.sendMessage(TranslationUtil.getTranslation(target.getUniqueID(), "commands.clan.invite.failedinvite", target.getName(), Objects.requireNonNull(ClanCache.getInvite(target.getUniqueID())).getClanName()).setStyle(TextStyles.YELLOW));
					}
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.already_in_this", target.getName(), selectedClan.getClanName()).setStyle(TextStyles.RED));
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
		ArrayList<String> playerNames = Lists.newArrayList();
		for(GameProfile profile: players)
			playerNames.add(profile.getName());
		return playerNames;
	}
}
