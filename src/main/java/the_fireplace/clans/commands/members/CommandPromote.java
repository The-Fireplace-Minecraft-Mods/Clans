package the_fireplace.clans.commands.members;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.MinecraftColors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandPromote extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.LEADER;
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
		return "/clan promote <player>";
	}

	@Override
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		GameProfile target = server.getPlayerProfileCache().getGameProfileForUsername(args[0]);

		if(target != null) {
			if (!ClanCache.getPlayerClans(target.getId()).isEmpty()) {
				if (ClanCache.getPlayerClans(target.getId()).contains(selectedClan)) {//TODO verify
					if (selectedClan.promoteMember(target.getId())) {
						sender.sendMessage(new TextComponentTranslation(MinecraftColors.GREEN + "You have promoted %s.", target.getName()));
						if(ArrayUtils.contains(server.getPlayerList().getOnlinePlayerProfiles(), target))
							getPlayer(server, sender, target.getName()).sendMessage(new TextComponentTranslation(MinecraftColors.GREEN + "You have been promoted by %s.", sender.getName()));
					} else
						sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "The player %s could not be promoted.", target.getName()));
				} else
					sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "The player %s is not in %s.", target.getName(), selectedClan.getClanName()));
			} else
				sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "The player %s is not in %s.", target.getName(), selectedClan.getClanName()));
		} else
			sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "The player %s was not found.", args[0]));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		ArrayList<String> playerNames = Lists.newArrayList();
		for(UUID player: selectedClan.getMembers().keySet()) {
			GameProfile playerProf = server.getPlayerProfileCache().getProfileByUUID(player);
			if(playerProf != null && !selectedClan.getMembers().get(player).equals(EnumRank.LEADER))
				playerNames.add(playerProf.getName());
		}
		return args.length == 1 ? playerNames : Collections.emptyList();
	}
}
