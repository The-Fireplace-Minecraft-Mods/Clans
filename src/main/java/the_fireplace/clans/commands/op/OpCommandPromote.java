package the_fireplace.clans.commands.op;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.util.MinecraftColors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandPromote extends OpClanSubCommand {
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
		return "/opclan promote <clan> <member>";
	}

	@Override
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		String clan = args[0];
		Clan c = ClanCache.getClan(clan);
		if(c != null) {
			promoteClanMember(server, sender, args[1], c);
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Clan not found."));
	}

	public static void promoteClanMember(MinecraftServer server, EntityPlayerMP sender, String playerName, Clan clan) throws CommandException {
		GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(playerName);

		if(target != null) {

		} else
			sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "The player %s was not found.", playerName));
	}

	@SuppressWarnings("Duplicates")
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		if(args.length == 1)
			return Lists.newArrayList();
		Clan target = ClanCache.getClan(args[0]);
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
