package the_fireplace.clans.commands.raiding;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.raid.Raid;
import the_fireplace.clans.raid.RaidingParties;
import the_fireplace.clans.util.MinecraftColors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandJoinRaid extends RaidSubCommand {
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
		return "/raid join <clan name>";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		if(!RaidingParties.getRaidingPlayers().contains(sender.getUniqueID())) {
			String targetName = args[0];
			Raid raid = RaidingParties.getRaid(targetName);
			if (raid != null) {
				HashMap<EntityPlayerMP, EnumRank> clanPlayers = raid.getTarget().getOnlineMembers();
				if(clanPlayers.size() > raid.getMemberCount() - Clans.cfg.maxRaidersOffset) {
					if(!clanPlayers.containsKey(sender)) {
						raid.addMember(sender);
						sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "You successfully joined the raid!"));
					} else
						sender.sendMessage(new TextComponentString(MinecraftColors.RED + "You cannot raid your own clan!"));
				} else
					sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Target raid cannot hold any more people!"));
			} else
				sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Target raid does not exist!"));
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "You are already in a raid!"));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		HashMap<Clan, Raid> raids = RaidingParties.getRaids();
		ArrayList<String> targetClanNames = Lists.newArrayList();
		for(Map.Entry<Clan, Raid> entry: raids.entrySet())
			if(sender.getCommandSenderEntity() != null && !entry.getKey().getMembers().containsKey(sender.getCommandSenderEntity().getUniqueID()))
				targetClanNames.add(entry.getKey().getClanName());
		return args.length == 1 ? targetClanNames : Collections.emptyList();
	}
}
