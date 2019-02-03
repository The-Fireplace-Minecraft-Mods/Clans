package the_fireplace.clans.commands.raiding;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.MinecraftColors;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.raid.Raid;
import the_fireplace.clans.raid.RaidingParties;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;

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
		return "/raid join <name>";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		if(!RaidingParties.getRaidingPlayers().contains(sender)) {
			String name = args[0];
			Raid raid = RaidingParties.getRaid(name);
			if (raid != null) {
				HashMap<EntityPlayerMP, EnumRank> clanPlayers = raid.getTarget().getOnlineMembers(server, sender);
				if(clanPlayers.size() > raid.getMemberCount()) {//TODO config option to control the offset here
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
}
