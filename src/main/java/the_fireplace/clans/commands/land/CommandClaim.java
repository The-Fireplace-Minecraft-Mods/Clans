package the_fireplace.clans.commands.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanChunkCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.CapHelper;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
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
					sender.sendMessage(new TextComponentTranslation("%s has already claimed this land.", selectedClan.getClanName()).setStyle(TextStyles.YELLOW));
				else
					sender.sendMessage(new TextComponentTranslation("Another clan (%s) has already claimed this land.", ClanCache.getClanById(claimFaction)).setStyle(TextStyles.RED));
			} else {
				if(!Clans.cfg.forceConnectedClaims || ChunkUtils.hasConnectedClaim(c, selectedClan.getClanId()) || selectedClan.getClaimCount() == 0) {
					if(Clans.cfg.maxClanPlayerClaims <= 0 || selectedClan.getClaimCount() < selectedClan.getMaxClaimCount()) {
						if(selectedClan.getClaimCount() > 0) {
							claimChunk(sender, c, selectedClan);
						} else if(Clans.cfg.minClanHomeDist > 0 && Clans.cfg.initialClaimSeparationMultiplier > 0) {
							boolean inClanHomeRange = false;
							for(Map.Entry<NewClan, BlockPos> pos: ClanCache.getClanHomes().entrySet())
								if(pos.getValue().getDistance(sender.getPosition().getX(), sender.getPosition().getY(), sender.getPosition().getZ()) < Clans.cfg.minClanHomeDist*Clans.cfg.initialClaimSeparationMultiplier)
									inClanHomeRange = true;
							if(inClanHomeRange) {
								if(Clans.cfg.enforceInitialClaimSeparation)
									sender.sendMessage(new TextComponentTranslation("You cannot claim this chunk of land because it is too close to another clan's home. Make sure you are at least %s blocks away from other clans' homes before trying again. Try using /clan fancymap to help determine where other clans are.", Clans.cfg.minClanHomeDist*Clans.cfg.initialClaimSeparationMultiplier).setStyle(TextStyles.RED));
								else if(CapHelper.getPlayerClanCapability(sender).getClaimWarning())
									claimChunk(sender, c, selectedClan);
								else {
									sender.sendMessage(new TextComponentTranslation("It is recommended that you do not claim this chunk of land because it is within %s blocks of another clan's home. Type /clan claim again to claim this land anyways. Try using /clan fancymap to help determine where other clans are.", Clans.cfg.minClanHomeDist*Clans.cfg.initialClaimSeparationMultiplier).setStyle(TextStyles.YELLOW));
									CapHelper.getPlayerClanCapability(sender).setClaimWarning(true);
								}
							}
						} else {
							claimChunk(sender, c, selectedClan);
						}
					} else
						sender.sendMessage(new TextComponentString(selectedClan.getClanName() + " is already at or above its max claim count of "+selectedClan.getMaxClaimCount()).setStyle(TextStyles.RED));
				} else
					sender.sendMessage(new TextComponentString("You cannot claim this chunk of land because it is not next to another of "+selectedClan.getClanName()+"'s claims.").setStyle(TextStyles.RED));
			}
		} else
			sender.sendMessage(new TextComponentString("Internal error: This chunk doesn't appear to be claimable.").setStyle(TextStyles.RED));
	}

	private static void claimChunk(EntityPlayerMP sender, Chunk c, NewClan selectedClan) {
		if (Clans.getPaymentHandler().deductAmount(Clans.cfg.claimChunkCost, selectedClan.getClanId())) {
			ChunkUtils.setChunkOwner(c, selectedClan.getClanId());
			ClanChunkCache.addChunk(selectedClan, c.x, c.z, c.getWorld().provider.getDimension());
			selectedClan.addClaimCount();
			sender.sendMessage(new TextComponentString("Land claimed!").setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(new TextComponentTranslation("Insufficient funds in %s's account to claim chunk. It costs %s %s.", selectedClan.getClanName(), Clans.cfg.claimChunkCost, Clans.getPaymentHandler().getCurrencyName(Clans.cfg.claimChunkCost)).setStyle(TextStyles.RED));
	}
}
