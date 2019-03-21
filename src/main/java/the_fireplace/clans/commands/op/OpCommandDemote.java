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
import the_fireplace.clans.util.TextStyles;

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
		return "/opclan demote <clan> <member>";
	}

	@Override
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		String clan = args[0];
		Clan c = ClanCache.getClan(clan);
		if(c != null) {
			demoteClanMember(server, sender, args[1], c);
		} else
			sender.sendMessage(new TextComponentString("Clan not found.").setStyle(TextStyles.RED));
	}

	public static void demoteClanMember(MinecraftServer server, EntityPlayerMP sender, String playerName, Clan clan) throws CommandException {
		GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(playerName);

		if(target != null) {
			if (!ClanCache.getPlayerClans(target.getId()).isEmpty()) {
				if (ClanCache.getPlayerClans(target.getId()).contains(clan)) {
					if (clan.demoteMember(target.getId())) {
						sender.sendMessage(new TextComponentTranslation("You have demoted %s.", target.getName()).setStyle(TextStyles.GREEN));
						if(ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target))
							getPlayer(server, sender, target.getName()).sendMessage(new TextComponentTranslation("You have been demoted in %s by %s.", clan.getClanName(), sender.getName()).setStyle(TextStyles.YELLOW));
					} else
						sender.sendMessage(new TextComponentTranslation("The player %s could not be demoted.", target.getName()).setStyle(TextStyles.RED));
				} else
					sender.sendMessage(new TextComponentTranslation("The player %s is not in %s.", target.getName(), clan.getClanName()).setStyle(TextStyles.RED));
			} else
				sender.sendMessage(new TextComponentTranslation("The player %s is not in %s.", target.getName(), clan.getClanName()).setStyle(TextStyles.RED));
		} else
			sender.sendMessage(new TextComponentTranslation("The player %s was not found.", playerName).setStyle(TextStyles.RED));
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
