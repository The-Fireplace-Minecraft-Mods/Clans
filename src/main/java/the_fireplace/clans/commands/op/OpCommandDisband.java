package the_fireplace.clans.commands.op;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.commands.members.CommandLeave;
import the_fireplace.clans.util.MinecraftColors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandDisband extends OpClanSubCommand {
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
		return "/opclan disband <clan>";
	}

	@Override
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
		String clan = args[0];
		Clan c = ClanCache.getClan(clan);
		if(c != null) {
			disbandClan(server, sender, c);
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Clan not found."));
	}

	public static void disbandClan(MinecraftServer server, EntityPlayerMP sender, Clan c) {
		if(!c.isOpclan()) {
			if (ClanDatabase.removeClan(c.getClanId())) {
				long distFunds = Clans.getPaymentHandler().getBalance(c.getClanId());
				distFunds += Clans.cfg.claimChunkCost * c.getClaimCount();
				if (Clans.cfg.leaderRecieveDisbandFunds) {
					c.payLeaders(distFunds);
					distFunds = 0;
				} else {
					c.payLeaders(distFunds % c.getMemberCount());
					distFunds /= c.getMemberCount();
				}
				for (UUID member : c.getMembers().keySet()) {
					Clans.getPaymentHandler().ensureAccountExists(member);
					if (!Clans.getPaymentHandler().addAmount(distFunds, member))
						c.payLeaders(distFunds);
					EntityPlayerMP player;
					try {
						player = getPlayer(server, sender, member.toString());
					} catch (CommandException e) {
						player = null;
					}
					if (player != null) {
						CommandLeave.updateDefaultClan(player, c);
						if (!player.getUniqueID().equals(sender.getUniqueID()))
							player.sendMessage(new TextComponentTranslation(MinecraftColors.GREEN + "Your clan has been disbanded by %s.", sender.getName()));
					}
				}
				Clans.getPaymentHandler().deductAmount(Clans.getPaymentHandler().getBalance(c.getClanId()), c.getClanId());
				sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "You have disbanded " + c.getClanName()));
			} else
				sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Internal error: Unable to disband clan."));
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "You cannot disband the Opclan."));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		ArrayList<String> removable = Lists.newArrayList(ClanCache.getClanNames().keySet());
		removable.remove(Objects.requireNonNull(ClanCache.getClan(UUID.fromString("00000000-0000-0000-0000-000000000000"))).getClanName());
		return args.length == 1 ? removable : Collections.emptyList();
	}
}
