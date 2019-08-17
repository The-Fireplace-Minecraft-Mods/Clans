package the_fireplace.clans.commands.op.management;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.ClanManagementUtil;
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
public class OpCommandSetRank extends OpClanSubCommand {
	@Override
	public int getMinArgs() {
		return 3;
	}

	@Override
	public int getMaxArgs() {
		return 3;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return TranslationUtil.getRawTranslationString(sender, "commands.opclan.setrank.usage");
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String clan = args[0];
		Clan c = ClanCache.getClanByName(clan);
		if(c != null) {
			try {
				if(args[1].equalsIgnoreCase("any") || args[1].equalsIgnoreCase("none"))
					throwWrongUsage(sender);
				EnumRank rank = EnumRank.valueOf(args[1].toUpperCase());
				ClanManagementUtil.setRank(server, sender, args[2], c, rank);
			} catch(IllegalArgumentException e) {
				throwWrongUsage(sender);
			}
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clan).setStyle(TextStyles.RED));
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
