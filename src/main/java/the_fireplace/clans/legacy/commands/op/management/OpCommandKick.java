package the_fireplace.clans.legacy.commands.op.management;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanMemberCache;
import the_fireplace.clans.clan.ClanNameCache;
import the_fireplace.clans.legacy.commands.OpClanSubCommand;
import the_fireplace.clans.legacy.logic.ClanMemberManagement;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandKick extends OpClanSubCommand {
	@Override
	public String getName() {
		return "kick";
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
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String clan = args[0];
		String player = args[1];
		Clan c = ClanNameCache.getClanByName(clan);
		if(c != null) {
			GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(player);

			if(target != null) {
				if (!ClanMemberCache.getClansPlayerIsIn(target.getId()).isEmpty()) {
					if (ClanMemberCache.getClansPlayerIsIn(target.getId()).contains(c)) {
						ClanMemberManagement.kickMember(server, sender, c, target);
					} else
						sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), c.getName()).setStyle(TextStyles.RED));
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player_not_in_clan", target.getName(), c.getName()).setStyle(TextStyles.RED));
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.playernotfound", player).setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clan).setStyle(TextStyles.RED));
	}

	@SuppressWarnings("Duplicates")
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		if(args.length == 1)
			return getListOfStringsMatchingLastWord(args, ClanNameCache.getClanNames());
		Clan target = ClanNameCache.getClanByName(args[0]);
		if(target != null && args.length == 2) {
			ArrayList<String> playerNames = Lists.newArrayList();
			for (UUID player : target.getMembers().keySet()) {
				GameProfile playerProf = server.getPlayerProfileCache().getProfileByUUID(player);
				if (playerProf != null && !target.getMembers().get(player).equals(EnumRank.LEADER))
					playerNames.add(playerProf.getName());
			}
			return getListOfStringsMatchingLastWord(args, playerNames);
		}
		return Collections.emptyList();
	}
}
