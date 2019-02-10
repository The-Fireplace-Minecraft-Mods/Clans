package the_fireplace.clans.commands.raiding;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.Clans;
import the_fireplace.clans.util.MinecraftColors;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.raid.Raid;
import the_fireplace.clans.raid.RaidingParties;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandStartRaid extends RaidSubCommand {
	@Override
	public int getMinArgs() {
		return 0;
	}

	@Override
	public int getMaxArgs() {
		return 0;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/raid start";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		if(RaidingParties.getRaidingPlayers().contains(sender)) {
			Raid raid = RaidingParties.getRaid(sender);
			if (raid != null) {
				assert server != null;
				HashMap<EntityPlayerMP, EnumRank> clanPlayers = raid.getTarget().getOnlineMembers(server, sender);
				if(clanPlayers.size() >= raid.getMemberCount() - Clans.cfg.maxRaidersOffset) {
					if(!RaidingParties.hasActiveRaid(raid.getTarget())) {
						RaidingParties.initRaid(raid.getTarget());
						sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "You successfully started the raid!"));
					} else
						sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Another raiding party is raiding this clan right now. Try again in "+(Math.round(100f*(Clans.cfg.defenseShield*60f*60f+raid.getRemainingSeconds())/60f/60f)/100f)+" hours."));
				} else
					sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Your raid has too many people!"));
			} else//Internal error because we should not reach this point
				sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Internal error: You are not in a raid!"));
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "You are not in a raid!"));
	}
}
