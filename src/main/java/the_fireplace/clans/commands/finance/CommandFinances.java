package the_fireplace.clans.commands.finance;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.TextStyles;

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
		return "/clan finances";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		long upkeep = 0;
		long rent = 0;
		if(Clans.cfg.clanUpkeepDays > 0) {
			upkeep += Clans.cfg.clanUpkeepCost;
			if(Clans.cfg.multiplyUpkeepClaims)
				upkeep *= selectedClan.getClaimCount();
			if(Clans.cfg.multiplyUpkeepMembers)
				upkeep *= selectedClan.getMemberCount();
			if(upkeep > 0)
				sender.sendMessage(new TextComponentString("Upkeep (expenses) is "+upkeep+' '+Clans.getPaymentHandler().getCurrencyName(upkeep)+" every "+Clans.cfg.clanUpkeepDays+" days.").setStyle(TextStyles.GREEN));
		}
		if(Clans.cfg.chargeRentDays > 0) {
			rent += selectedClan.getRent();
			rent *= selectedClan.getMemberCount();
			if(rent > 0)
				sender.sendMessage(new TextComponentString("Rent (income) is "+rent+' '+Clans.getPaymentHandler().getCurrencyName(rent)+" every "+Clans.cfg.chargeRentDays+" days.").setStyle(TextStyles.GREEN));
		}
		if(upkeep > 0 && rent > 0) {
			upkeep /= Clans.cfg.clanUpkeepDays;
			rent /= Clans.cfg.chargeRentDays;
			sender.sendMessage(new TextComponentString("Approximate financial balance is "+(rent-upkeep)+' '+Clans.getPaymentHandler().getCurrencyName(rent-upkeep)+" each day.").setStyle(rent >= upkeep ? TextStyles.GREEN : TextStyles.YELLOW));
			if(upkeep > rent) {
				long maxRent = Clans.cfg.maxRent;
				if(Clans.cfg.multiplyMaxRentClaims)
					maxRent *= selectedClan.getClaimCount();
				if(selectedClan.getRent() < maxRent) {
					if(maxRent/Clans.cfg.chargeRentDays < upkeep)
						sender.sendMessage(new TextComponentString("You may want to increase rent to "+maxRent+" and/or find a way to reduce upkeep.").setStyle(TextStyles.YELLOW));
					else
						sender.sendMessage(new TextComponentString("You may want to increase rent to "+Clans.cfg.chargeRentDays*upkeep/selectedClan.getMemberCount()+" and/or find a way to reduce upkeep.").setStyle(TextStyles.YELLOW));
				} else
					sender.sendMessage(new TextComponentString("You may want to find a way to reduce upkeep.").setStyle(TextStyles.YELLOW));
			}
		}
		if(rent <= 0 && upkeep <= 0)
			sender.sendMessage(new TextComponentString("Your clan is not earning or losing money.").setStyle(TextStyles.GREEN));
	}
}
