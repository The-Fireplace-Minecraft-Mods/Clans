package the_fireplace.clans.commands.raiding;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.raid.Raid;
import the_fireplace.clans.raid.RaidingParties;
import the_fireplace.clans.util.TextStyles;

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
		if(RaidingParties.getRaidingPlayers().contains(sender.getUniqueID())) {
			Raid raid = RaidingParties.getRaid(sender);
			if (raid != null) {
				assert server != null;
				HashMap<EntityPlayerMP, EnumRank> clanPlayers = raid.getTarget().getOnlineMembers();
				if(clanPlayers.size() >= raid.getMemberCount() - Clans.cfg.maxRaidersOffset) {
					if(!RaidingParties.hasActiveRaid(raid.getTarget())) {
						if(!RaidingParties.isPreparingRaid(raid.getTarget())) {
							long raidCost = Clans.cfg.startRaidCost;
							if (Clans.cfg.startRaidMultiplier)
								raidCost *= raid.getTarget().getClaimCount();
							raid.setCost(raidCost);
							if (Clans.getPaymentHandler().deductAmount(raidCost, sender.getUniqueID())) {
								RaidingParties.initRaid(raid.getTarget());
								sender.sendMessage(new TextComponentTranslation("You successfully started the raid against %s!", raid.getTarget().getClanName()).setStyle(TextStyles.GREEN));
							} else
								sender.sendMessage(new TextComponentTranslation("You have insufficient funds to start the raid against %s. It costs %s %s.", raid.getTarget().getClanName(), raidCost, Clans.getPaymentHandler().getCurrencyName(raidCost)).setStyle(TextStyles.RED));
						} else
							sender.sendMessage(new TextComponentString("The raid has already been started!").setStyle(TextStyles.RED));
					} else //This should not be possible
						sender.sendMessage(new TextComponentTranslation("Internal error: Another raiding party is raiding this clan right now. Try again in %s hours.", Math.round(100f*(Clans.cfg.defenseShield*60f*60f+raid.getRemainingSeconds())/60f/60f)/100f).setStyle(TextStyles.RED));
				} else
					sender.sendMessage(new TextComponentTranslation("Your raiding party has too many people! It has %s raiders and the limit is currently %s.", raid.getMemberCount(), clanPlayers.size() + Clans.cfg.maxRaidersOffset).setStyle(TextStyles.RED));
			} else//Internal error because we should not reach this point
				sender.sendMessage(new TextComponentString("Internal error: You are not in a raiding party!").setStyle(TextStyles.RED));
		} else
			sender.sendMessage(new TextComponentString("You are not in a raiding party!").setStyle(TextStyles.RED));
	}
}
