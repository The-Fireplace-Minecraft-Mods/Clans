package the_fireplace.clans.commands.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClanChunkCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
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
		Chunk c = sender.getEntityWorld().getChunk(sender.getPosition());
		if(c.hasCapability(Clans.CLAIMED_LAND, null)){
			UUID claimFaction = ChunkUtils.getChunkOwner(c);
			if(claimFaction != null) {
				if(claimFaction.equals(selectedClan.getClanId()))
					sender.sendMessage(new TextComponentString("Your clan has already claimed this land.").setStyle(TextStyles.YELLOW));
				else
					sender.sendMessage(new TextComponentString("Another clan has already claimed this land.").setStyle(TextStyles.RED));
			} else {
				if(!Clans.cfg.forceConnectedClaims || ChunkUtils.hasConnectedClaim(c, selectedClan.getClanId()) || selectedClan.getClaimCount() == 0) {
					if(Clans.cfg.maxClanPlayerClaims <= 0 || selectedClan.getClaimCount() < selectedClan.getMaxClaimCount()) {
						if (Clans.getPaymentHandler().deductAmount(Clans.cfg.claimChunkCost, selectedClan.getClanId())) {
							ChunkUtils.setChunkOwner(c, selectedClan.getClanId());
							ClanChunkCache.addChunk(selectedClan, c.x, c.z, c.getWorld().provider.getDimension());
							selectedClan.addClaimCount();
							sender.sendMessage(new TextComponentString("Land claimed!").setStyle(TextStyles.GREEN));
						} else
							sender.sendMessage(new TextComponentString("Insufficient funds in clan account to claim chunk. It costs " + Clans.cfg.claimChunkCost + ' ' + Clans.getPaymentHandler().getCurrencyName(Clans.cfg.claimChunkCost)).setStyle(TextStyles.RED));
					} else
						sender.sendMessage(new TextComponentString(selectedClan.getClanName() + " is already at or above its max claim count of "+selectedClan.getMaxClaimCount()).setStyle(TextStyles.RED));
				} else
					sender.sendMessage(new TextComponentString("You cannot claim this chunk of land because it is not next to another of "+selectedClan.getClanName()+"'s claims.").setStyle(TextStyles.RED));
			}
		} else
			sender.sendMessage(new TextComponentString("Internal error: This chunk doesn't appear to be claimable.").setStyle(TextStyles.RED));
	}
}
