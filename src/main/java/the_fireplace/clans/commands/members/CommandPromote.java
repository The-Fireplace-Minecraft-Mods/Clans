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
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.MinecraftColors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		EntityPlayerMP target = getPlayer(server, sender, args[0]);//TODO support promoting offline players
		if(!ClanCache.getPlayerClans(target.getUniqueID()).isEmpty()) {
			if(ClanCache.getPlayerClans(target.getUniqueID()).contains(selectedClan)) {//TODO verify
				if(selectedClan.promoteMember(target.getUniqueID())) {
					sender.sendMessage(new TextComponentTranslation(MinecraftColors.GREEN + "You have promoted %s.", target.getName()));
					target.sendMessage(new TextComponentTranslation(MinecraftColors.GREEN + "You have been promoted by %s.", sender.getName()));
				} else
					sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "The player %s could not be promoted.", target.getName()));
			} else
				sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "The player %s is not in your clan.", target.getName()));
		} else
			sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "The player %s is not in your clan.", target.getName()));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		ArrayList<GameProfile> players = Lists.newArrayList(server.getPlayerList().getOnlinePlayerProfiles());
		if(sender instanceof EntityPlayerMP) {//TODO support promoting offline players
			players.removeIf(s -> (!selectedClan.getMembers().containsKey(s.getId()) || selectedClan.getMembers().get(s.getId()).equals(EnumRank.LEADER)));
		}
		ArrayList<String> playerNames = Lists.newArrayList();
		for(GameProfile profile: players)
			playerNames.add(profile.getName());
		return args.length == 1 ? playerNames : Collections.emptyList();
	}
}
