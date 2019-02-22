package the_fireplace.clans.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.commands.members.CommandLeave;
import the_fireplace.clans.util.MinecraftColors;

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
		if (ClanDatabase.removeClan(selectedClan.getClanId())) {
			long distFunds = Clans.getPaymentHandler().getBalance(selectedClan.getClanId());
			distFunds += Clans.cfg.claimChunkCost * selectedClan.getClaimCount();
			if (Clans.cfg.leaderRecieveDisbandFunds) {
				selectedClan.payLeaders(distFunds);
				distFunds = 0;
			} else {
				selectedClan.payLeaders(distFunds % selectedClan.getMemberCount());
				distFunds /= selectedClan.getMemberCount();
			}
			for (UUID member : selectedClan.getMembers().keySet()) {
				Clans.getPaymentHandler().ensureAccountExists(member);
				if (!Clans.getPaymentHandler().addAmount(distFunds, member))
					selectedClan.payLeaders(distFunds);
				EntityPlayerMP player;
				try {
					player = getPlayer(server, sender, member.toString());
				} catch (CommandException e) {
					player = null;
				}
				if(player != null) {
                    CommandLeave.updateDefaultClan(player, selectedClan);
                    if (!player.getUniqueID().equals(sender.getUniqueID()))
                        player.sendMessage(new TextComponentTranslation(MinecraftColors.GREEN + "Your clan has been disbanded by %s.", sender.getName()));
                }
			}
			Clans.getPaymentHandler().deductAmount(Clans.getPaymentHandler().getBalance(selectedClan.getClanId()), selectedClan.getClanId());
			sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "You have disbanded your clan."));
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Internal error: Unable to disband clan."));
	}
}
