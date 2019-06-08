package the_fireplace.clans.commands.finance;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandFinances extends ClanSubCommand {
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
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.finances.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		if(Clans.cfg.chargeRentDays <= 0 && Clans.cfg.clanUpkeepDays <= 0)
			throw new CommandException(TranslationUtil.getRawTranslationString(sender, "commands.clan.finances.disabled"));
		long upkeep = 0;
		long rent = 0;
		if(Clans.cfg.clanUpkeepDays > 0) {
			upkeep += Clans.cfg.clanUpkeepCost;
			if(Clans.cfg.multiplyUpkeepClaims)
				upkeep *= selectedClan.getClaimCount();
			if(Clans.cfg.multiplyUpkeepMembers)
				upkeep *= selectedClan.getMemberCount();
			if(upkeep > 0)
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.finances.upkeep", upkeep, Clans.getPaymentHandler().getCurrencyName(upkeep), Clans.cfg.clanUpkeepDays).setStyle(TextStyles.GREEN));
		}
		if(Clans.cfg.chargeRentDays > 0) {
			rent += selectedClan.getRent();
			rent *= selectedClan.getMemberCount();
			if(rent > 0)
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.finances.rent", rent, Clans.getPaymentHandler().getCurrencyName(rent), Clans.cfg.chargeRentDays).setStyle(TextStyles.GREEN));
		}
		if(upkeep > 0 && rent > 0) {
			upkeep /= Clans.cfg.clanUpkeepDays;
			rent /= Clans.cfg.chargeRentDays;
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.finances.trend", (rent-upkeep) <= 0 ? (rent-upkeep) : '+'+(rent-upkeep), Clans.getPaymentHandler().getCurrencyName(rent-upkeep)).setStyle(rent >= upkeep ? TextStyles.GREEN : TextStyles.YELLOW));
			if(upkeep > rent) {
				long maxRent = Clans.cfg.maxRent;
				if(Clans.cfg.multiplyMaxRentClaims)
					maxRent *= selectedClan.getClaimCount();
				if(selectedClan.getRent() < maxRent) {
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.finances.increase_rent", maxRent/Clans.cfg.chargeRentDays < upkeep ? maxRent : Clans.cfg.chargeRentDays*upkeep/selectedClan.getMemberCount()).setStyle(TextStyles.YELLOW));
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.finances.reduce_upkeep").setStyle(TextStyles.YELLOW));
			}
		}
		if(rent <= 0 && upkeep <= 0)
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.finances.breaking_even", selectedClan.getClanName()).setStyle(TextStyles.GREEN));
	}
}
