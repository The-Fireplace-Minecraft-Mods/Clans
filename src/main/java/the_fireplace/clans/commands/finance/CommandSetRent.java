package the_fireplace.clans.commands.finance;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSetRent extends ClanSubCommand {
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
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.setrent.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		int newRent = Integer.valueOf(args[0]);
		if(newRent >= 0) {
			long maxRent = Clans.cfg.maxRent;
			if(Clans.cfg.multiplyMaxRentClaims)
				maxRent *= selectedClan.getClaimCount();
			if(maxRent <= 0 || newRent <= maxRent) {
				selectedClan.setRent(newRent);
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setrent.success", selectedClan.getClanName(), selectedClan.getRent()).setStyle(TextStyles.GREEN));
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setrent.overmax", selectedClan.getClanName(), maxRent, Clans.getPaymentHandler().getCurrencyName(maxRent)).setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setrent.negative").setStyle(TextStyles.RED));
	}
}
