package the_fireplace.clans.commands.raiding;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.raid.Raid;
import the_fireplace.clans.raid.RaidingParties;
import the_fireplace.clans.util.MinecraftColors;

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
		Clan target = ClanCache.getClan(args[0]);
		if(target == null)
			sender.sendMessage(new TextComponentString(MinecraftColors.RED+"Target clan not found."));
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
								sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "Raiding party created!"));
							} else
								sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Insufficient funds to form raiding party against " + target.getClanName() + ". It costs " + raidCost + ' ' + Clans.getPaymentHandler().getCurrencyName(raidCost)));
						} else
							sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Target clan already has a party preparing to raid it or currently raiding it!"));
					} else
						sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Target clan has no online members!"));
				} else
					sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Target clan is currently shielded! Try again in "+(Math.round(100f*target.getShield()*60)/100f)+" hours."));
			} else
				sender.sendMessage(new TextComponentString(MinecraftColors.RED + "You are already in a raid!"));
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		HashMap<Clan, Raid> raids = RaidingParties.getRaids();
		Collection<Clan> clans = ClanDatabase.getClans();
		ArrayList<String> targetClanNames = Lists.newArrayList();
		for(Clan c: clans)
			if(sender.getCommandSenderEntity() != null && !c.getOnlineMembers().isEmpty() && !c.getMembers().containsKey(sender.getCommandSenderEntity().getUniqueID()) && !raids.containsKey(c) && !RaidingParties.hasActiveRaid(c) && !c.isShielded() && !c.isOpclan())
				targetClanNames.add(c.getClanName());
		return args.length == 1 ? targetClanNames : Collections.emptyList();
	}
}
