package the_fireplace.clans.commands.op.management;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandDemote extends OpClanSubCommand {
	@Override
	public int getMinArgs() {
		return 2;
	}

	@Override
	public int getMaxArgs() {
		return 2;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return TranslationUtil.getRawTranslationString(sender, "commands.opclan.demote.usage");
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String clan = args[0];
		Clan c = ClanCache.getClanByName(clan);
		if(c != null) {
			demoteClanMember(server, sender, args[1], c);
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clan).setStyle(TextStyles.RED));
	}

	public static void demoteClanMember(MinecraftServer server, ICommandSender sender, String playerName, Clan clan) throws CommandException {
		GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(playerName);

		if(target != null) {
			if (!ClanCache.getPlayerClans(target.getId()).isEmpty()) {
				if (ClanCache.getPlayerClans(target.getId()).contains(clan)) {
					if (clan.demoteMember(target.getId())) {
						sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.opclan.demote.success", target.getName(), clan.getMembers().get(target.getId()).toString().toLowerCase(), clan.getClanName()).setStyle(TextStyles.GREEN));
						if(ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target)) {
							EntityPlayerMP targetPlayer = getPlayer(server, sender, target.getName());
							targetPlayer.sendMessage(TranslationUtil.getTranslation(targetPlayer.getUniqueID(), "commands.opclan.demote.demoted", clan.getClanName(), clan.getMembers().get(target.getId()).toString().toLowerCase(), sender.getName()).setStyle(TextStyles.YELLOW));
						}
					} else
						sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.opclan.demote.error", target.getName()).setStyle(TextStyles.RED));
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.not_in_clan", target.getName(), clan.getClanName()).setStyle(TextStyles.RED));
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.not_in_clan", target.getName(), clan.getClanName()).setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.playernotfound", playerName).setStyle(TextStyles.RED));
	}

	@SuppressWarnings("Duplicates")
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		if(args.length == 1)
			return Lists.newArrayList();
		Clan target = ClanCache.getClanByName(args[0]);
		if(target != null && args.length == 2) {
			ArrayList<String> playerNames = Lists.newArrayList();
			for (UUID player : target.getMembers().keySet()) {
				GameProfile playerProf = server.getPlayerProfileCache().getProfileByUUID(player);
				if (playerProf != null && !target.getMembers().get(player).equals(EnumRank.LEADER))
					playerNames.add(playerProf.getName());
			}
			return playerNames;
		}
		return Collections.emptyList();
	}
}
