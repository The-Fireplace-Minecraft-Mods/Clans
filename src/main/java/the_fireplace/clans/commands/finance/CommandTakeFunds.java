package the_fireplace.clans.commands.finance;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.TextStyles;

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
		long amount = Long.valueOf(args[0]);
		if(Clans.getPaymentHandler().deductAmount(amount, selectedClan.getClanId())) {
			if(Clans.getPaymentHandler().addAmount(amount, sender.getUniqueID()))
				sender.sendMessage(new TextComponentTranslation("Successfully took %s %s from %s's balance.", amount, Clans.getPaymentHandler().getCurrencyName(amount), selectedClan.getClanName()).setStyle(TextStyles.GREEN));
			else {
				Clans.getPaymentHandler().addAmount(amount, selectedClan.getClanId());
				sender.sendMessage(new TextComponentString("Internal error: Your currency account not found.").setStyle(TextStyles.RED));
			}
		} else
			sender.sendMessage(new TextComponentTranslation("%s does not have enough funds to do this.", selectedClan.getClanName()).setStyle(TextStyles.RED));
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) {

	}
}
