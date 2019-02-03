package the_fireplace.clans.commands.op;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.MinecraftColors;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.commands.OpClanSubCommand;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandClaim extends OpClanSubCommand {

	@Override
	public int getMinArgs() {
		return 0;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/opclan claim [force]";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Clan opClan = ClanDatabase.getOpClan();
		Chunk c = sender.getEntityWorld().getChunk(sender.getPosition());
		if(c.hasCapability(Clans.CLAIMED_LAND, null)){
			UUID claimFaction = Objects.requireNonNull(c.getCapability(Clans.CLAIMED_LAND, null)).getClan();
			Clan targetClan = ClanCache.getClan(claimFaction);
			if(claimFaction != null && targetClan != null && ((args.length != 1 || !args[0].toLowerCase().equals("force")) || claimFaction.equals(opClan.getClanId()))) {
				if(claimFaction.equals(opClan.getClanId()))
					sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Opclan has already claimed this land."));
				else
					sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Another clan has already claimed this land. To take this land from "+targetClan.getClanName()+", use /opclan claim force"));
			} else {
				if(targetClan != null) {
					targetClan.subClaimCount();
					Clans.getPaymentHandler().addAmount(Clans.cfg.claimChunkCost, targetClan.getClanId());
				}
				Objects.requireNonNull(c.getCapability(Clans.CLAIMED_LAND, null)).setClan(opClan.getClanId());
				opClan.addClaimCount();
				sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "Land claimed!"));
			}
		} else {
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Internal error: This chunk doesn't appear to be claimable."));
		}
	}
}
