package the_fireplace.clans.commands.raiding;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.raid.Raid;
import the_fireplace.clans.raid.RaidingParties;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandJoinRaid extends RaidSubCommand {
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
		return "/raid join <clan name>";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		NewClan target = ClanCache.getClanByName(args[0]);
		if(target == null)
			sender.sendMessage(new TextComponentString("Target clan not found.").setStyle(TextStyles.RED));
		else {
			if(!RaidingParties.getRaidingPlayers().contains(sender.getUniqueID())) {
				if(!target.getMembers().containsKey(sender.getUniqueID())) {
					if (!RaidingParties.getRaidedClans().contains(target)) {
						if(!target.isShielded()) {
							if (target.getOnlineMembers().size() > 0 && target.getOnlineMembers().size() + Clans.cfg.maxRaidersOffset > 0) {
								new Raid(sender, target);
								sender.sendMessage(new TextComponentTranslation("You successfully created the raiding party against %s!", target.getClanName()).setStyle(TextStyles.GREEN));
							} else
								sender.sendMessage(new TextComponentTranslation("%s does not have enough online members to get raided!", target.getClanName()).setStyle(TextStyles.RED));
						} else
							sender.sendMessage(new TextComponentTranslation("%s is currently shielded! Try again in %s hours.", target.getClanName(), Math.round(100f*target.getShield()*60)/100f).setStyle(TextStyles.RED));
					} else { //Join an existing raid
						Raid raid = RaidingParties.getRaid(target);
						if(target.getOnlineMembers().size() + Clans.cfg.maxRaidersOffset > raid.getMemberCount()) {
							raid.addMember(sender);
							sender.sendMessage(new TextComponentTranslation("You successfully joined the raiding party against %s!", target.getClanName()).setStyle(TextStyles.GREEN));
						} else
							sender.sendMessage(new TextComponentTranslation("The raiding party against %s cannot hold any more people! It has %s raiders and the limit is currently %s.", target.getClanName(), raid.getMemberCount(), target.getOnlineMembers().size() + Clans.cfg.maxRaidersOffset).setStyle(TextStyles.RED));
					}
				} else
					sender.sendMessage(new TextComponentString("You cannot raid a clan you are in!").setStyle(TextStyles.RED));
			} else
				sender.sendMessage(new TextComponentString("You are already in a raiding party, and cannot join another unless you leave the one you are currently in.").setStyle(TextStyles.RED));
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		HashMap<NewClan, Raid> raids = RaidingParties.getRaids();
		ArrayList<String> targetClanNames = Lists.newArrayList();
		for(Map.Entry<NewClan, Raid> entry: raids.entrySet())
			if(sender.getCommandSenderEntity() != null && !entry.getKey().getMembers().containsKey(sender.getCommandSenderEntity().getUniqueID()))
				targetClanNames.add(entry.getKey().getClanName());
		return args.length == 1 ? targetClanNames : Collections.emptyList();
	}
}
