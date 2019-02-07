package the_fireplace.clans.commands.finance;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.Clans;
import the_fireplace.clans.util.MinecraftColors;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandTakeFunds extends ClanSubCommand {
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
		return "/clan takefunds <amount>";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		if(!Clans.cfg.leaderWithdrawFunds)
			throw new CommandException("/clan takefunds is disabled on this server.");
		Clan playerClan = ClanCache.getPlayerClan(sender.getUniqueID());
		assert playerClan != null;
		long amount = Long.valueOf(args[0]);
		if(Clans.getPaymentHandler().deductAmount(amount, playerClan.getClanId())) {
			if(Clans.getPaymentHandler().addAmount(amount, sender.getUniqueID()))
				sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "Successfully took " + amount + ' ' + Clans.getPaymentHandler().getCurrencyName(amount) + " from your clan's balance."));
			else {
				Clans.getPaymentHandler().addAmount(amount, playerClan.getClanId());
				sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Internal error: Your account not found."));
			}
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Your clan does not have enough funds to do this."));
	}
}
