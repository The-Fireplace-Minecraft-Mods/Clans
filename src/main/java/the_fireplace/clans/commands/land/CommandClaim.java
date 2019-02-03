package the_fireplace.clans.commands.land;

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
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandClaim extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.ADMIN;
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
		return "/clan claim";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Clan playerClan = ClanCache.getPlayerClan(sender.getUniqueID());
		assert playerClan != null;
		Chunk c = sender.getEntityWorld().getChunk(sender.getPosition());
		if(c.hasCapability(Clans.CLAIMED_LAND, null)){
			UUID claimFaction = Objects.requireNonNull(c.getCapability(Clans.CLAIMED_LAND, null)).getClan();
			if(claimFaction != null) {
				if(claimFaction.equals(playerClan.getClanId()))
					sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Your clan has already claimed this land."));
				else
					sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Another clan has already claimed this land."));
			} else {
				if(Clans.getPaymentHandler().deductAmount(Clans.cfg.claimChunkCost, playerClan.getClanId())) {
					Objects.requireNonNull(c.getCapability(Clans.CLAIMED_LAND, null)).setClan(playerClan.getClanId());
					playerClan.addClaimCount();
					sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "Land claimed!"));
				} else
					sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Insufficient funds in clan account to claim chunk. It costs "+ Clans.cfg.claimChunkCost+' '+Clans.getPaymentHandler().getCurrencyName(Clans.cfg.claimChunkCost)));
			}
		} else {
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Internal error: This chunk doesn't appear to be claimable."));
		}
	}
}
