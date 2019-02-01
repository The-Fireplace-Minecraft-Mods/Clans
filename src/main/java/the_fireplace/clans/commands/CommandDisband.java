package the_fireplace.clans.commands;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.MinecraftColors;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.clan.EnumRank;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandDisband extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.LEADER;
	}

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
		return "/clan disband";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Clan senderClan = ClanCache.getClan(sender.getUniqueID());
		assert senderClan != null;
		if(ClanDatabase.removeClan(senderClan.getClanId())) {
			for(UUID member: senderClan.getMembers().keySet()) {
				EntityPlayerMP player;
				try {
					player = getPlayer(server, sender, member.toString());
				} catch(CommandException e){
					player = null;
				}
				if(player != null && !player.getUniqueID().equals(sender.getUniqueID()))
					player.sendMessage(new TextComponentTranslation(MinecraftColors.GREEN + "Your clan has been disbanded by %s.", sender.getName()));
			}
			sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "You have disbanded your clan."));
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Clan home can only be set in clan territory!"));
	}
}
