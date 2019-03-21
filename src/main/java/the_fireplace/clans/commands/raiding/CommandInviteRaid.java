package the_fireplace.clans.commands.raiding;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.raid.Raid;
import the_fireplace.clans.raid.RaidingParties;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandInviteRaid extends RaidSubCommand {
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
		return "/raid invite <player>";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		if(!RaidingParties.getRaidingPlayers().contains(sender.getUniqueID())) {
			Raid raid = RaidingParties.getRaid(sender);
			if (raid != null) {
				assert server != null;
				EntityPlayerMP targetPlayer = getPlayer(server, sender, args[0]);
				HashMap<EntityPlayerMP, EnumRank> clanPlayers = raid.getTarget().getOnlineMembers();
				if(clanPlayers.size() > raid.getMemberCount() - Clans.cfg.maxRaidersOffset) {
					if(!clanPlayers.containsKey(targetPlayer)) {
						targetPlayer.sendMessage(new TextComponentString("You have been invited to a raid against " + raid.getTarget().getClanName() + "! To join, type /raid join " + raid.getTarget().getClanName()).setStyle(TextStyles.GREEN));
						sender.sendMessage(new TextComponentString("You successfully invited " + targetPlayer.getName() + " to the raid!").setStyle(TextStyles.GREEN));
					} else
						sender.sendMessage(new TextComponentString("You cannot invite someone to raid their own clan!").setStyle(TextStyles.RED));
				} else
					sender.sendMessage(new TextComponentString("Your raid cannot hold any more people!").setStyle(TextStyles.RED));
			} else//Internal error because we should not reach this point
				sender.sendMessage(new TextComponentString("Internal error: You are not in a raid!").setStyle(TextStyles.RED));
		} else
			sender.sendMessage(new TextComponentString("You are not in a raid!").setStyle(TextStyles.RED));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		ArrayList<GameProfile> players = Lists.newArrayList(server.getPlayerList().getOnlinePlayerProfiles());
		if(sender instanceof EntityPlayerMP) {
			Raid r = RaidingParties.getRaid((EntityPlayerMP)sender);
			if (r != null)
				players.removeIf(s -> ClanCache.getPlayerClans(s.getId()).contains(r.getTarget()));
		}
		ArrayList<String> playerNames = Lists.newArrayList();
		for(GameProfile profile: players)
			playerNames.add(profile.getName());
		return args.length == 1 ? playerNames : Collections.emptyList();
	}
}
