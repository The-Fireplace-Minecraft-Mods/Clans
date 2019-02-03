package the_fireplace.clans.commands.raiding;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import the_fireplace.clans.MinecraftColors;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.raid.Raid;
import the_fireplace.clans.raid.RaidingParties;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandCreateRaid extends RaidSubCommand {
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
		return "/raid create <name> <target clan>";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Clan target = ClanCache.getClan(args[1]);
		if(target == null)
			sender.sendMessage(new TextComponentString(MinecraftColors.RED+"Target clan not found."));
		else {
			String name = args[0];
			if(!RaidingParties.getRaidingPlayers().contains(sender)) {
				if (!RaidingParties.getRaids().containsKey(name)) {
					if (target.getOnlineMembers(FMLCommonHandler.instance().getMinecraftServerInstance(), sender).size() > 0) {
						new Raid(name, sender, target);
						sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "Raiding party created!"));
					} else
						sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Target clan has no online members!"));
				} else
					sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Raid name is already taken!"));
			} else
				sender.sendMessage(new TextComponentString(MinecraftColors.RED + "You are already in a raid!"));
		}
	}
}
