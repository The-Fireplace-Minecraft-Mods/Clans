package the_fireplace.clans.commands.raiding;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.clan.NewClanDatabase;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.raid.Raid;
import the_fireplace.clans.raid.RaidingParties;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandCreateRaid extends RaidSubCommand {
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
		return "/raid form <target clan>";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		NewClan target = ClanCache.getClanByName(args[0]);
		if(target == null)
			sender.sendMessage(new TextComponentString("Target clan not found.").setStyle(TextStyles.RED));
		else {
			if(!RaidingParties.getRaidingPlayers().contains(sender.getUniqueID())) {
				if(!target.isShielded()) {
					if (target.getOnlineMembers().size() > 0) {
						if (!RaidingParties.getRaidedClans().contains(target)) {
							long raidCost = Clans.cfg.startRaidCost;
							if (Clans.cfg.startRaidMultiplier)
								raidCost *= target.getClaimCount();
							if (Clans.getPaymentHandler().deductAmount(raidCost, sender.getUniqueID())) {
								new Raid(sender, target, raidCost);
								sender.sendMessage(new TextComponentString("Raiding party created!").setStyle(TextStyles.GREEN));
							} else
								sender.sendMessage(new TextComponentString("Insufficient funds to form raiding party against " + target.getClanName() + ". It costs " + raidCost + ' ' + Clans.getPaymentHandler().getCurrencyName(raidCost)).setStyle(TextStyles.RED));
						} else
							sender.sendMessage(new TextComponentString("Target clan already has a party preparing to raid it or currently raiding it!").setStyle(TextStyles.RED));
					} else
						sender.sendMessage(new TextComponentString("Target clan has no online members!").setStyle(TextStyles.RED));
				} else
					sender.sendMessage(new TextComponentString("Target clan is currently shielded! Try again in "+(Math.round(100f*target.getShield()/60)/100f)+" hours.").setStyle(TextStyles.RED));
			} else
				sender.sendMessage(new TextComponentString("You are already in a raid!").setStyle(TextStyles.RED));
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		HashMap<NewClan, Raid> raids = RaidingParties.getRaids();
		Collection<NewClan> clans = NewClanDatabase.getClans();
		ArrayList<String> targetClanNames = Lists.newArrayList();
		for(NewClan c: clans)
			if(sender.getCommandSenderEntity() != null && !c.getOnlineMembers().isEmpty() && !c.getMembers().containsKey(sender.getCommandSenderEntity().getUniqueID()) && !raids.containsKey(c) && !RaidingParties.hasActiveRaid(c) && !c.isShielded() && !c.isOpclan())
				targetClanNames.add(c.getClanName());
		return args.length == 1 ? targetClanNames : Collections.emptyList();
	}
}
