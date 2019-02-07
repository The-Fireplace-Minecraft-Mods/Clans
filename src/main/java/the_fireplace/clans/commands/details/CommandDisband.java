package the_fireplace.clans.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.Clans;
import the_fireplace.clans.util.MinecraftColors;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
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
			long distFunds = Clans.getPaymentHandler().getBalance(senderClan.getClanId());
			distFunds += Clans.cfg.claimChunkCost * senderClan.getClaimCount();
			if(Clans.cfg.leaderRecieveDisbandFunds) {
				//TODO if multiple leaders, split among them
				Clans.getPaymentHandler().addAmount(distFunds, sender.getUniqueID());
				distFunds = 0;
			} else {
				//TODO if multiple leaders, split among them
				Clans.getPaymentHandler().addAmount(distFunds % senderClan.getMemberCount(), sender.getUniqueID());
				distFunds /= senderClan.getMemberCount();
			}
			for(UUID member: senderClan.getMembers().keySet()) {
				Clans.getPaymentHandler().ensureAccountExists(member);
				if(!Clans.getPaymentHandler().addAmount(distFunds, member))
					Clans.getPaymentHandler().addAmount(distFunds, sender.getUniqueID());
				EntityPlayerMP player;
				try {
					player = getPlayer(server, sender, member.toString());
				} catch(CommandException e){
					player = null;
				}
				if(player != null && !player.getUniqueID().equals(sender.getUniqueID()))
					player.sendMessage(new TextComponentTranslation(MinecraftColors.GREEN + "Your clan has been disbanded by %s.", sender.getName()));
			}
			Clans.getPaymentHandler().deductAmount(Clans.getPaymentHandler().getBalance(senderClan.getClanId()), senderClan.getClanId());
			sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "You have disbanded your clan."));
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Internal error: Unable to disband clan."));
	}
}
