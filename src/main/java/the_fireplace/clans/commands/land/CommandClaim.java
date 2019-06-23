package the_fireplace.clans.commands.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanChunkData;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.CapHelper;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

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
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.claim.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		checkAndAttemptClaim(sender, selectedClan);
	}

	public static boolean checkAndAttemptClaim(EntityPlayerMP sender, Clan selectedClan) {
		Chunk c = sender.getEntityWorld().getChunk(sender.getPosition());
		if(c.hasCapability(Clans.CLAIMED_LAND, null)) {
			UUID claimFaction = ChunkUtils.getChunkOwner(c);
			Clan claimClan = ClanCache.getClanById(claimFaction);
			if(claimFaction != null && claimClan != null) {
				if(claimFaction.equals(selectedClan.getClanId()))
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.taken", selectedClan.getClanName()).setStyle(TextStyles.YELLOW));
				else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.taken_other", claimClan.getClanName()).setStyle(TextStyles.RED));
			} else {
				if(!Clans.cfg.forceConnectedClaims || ChunkUtils.hasConnectedClaim(c, selectedClan.getClanId()) || selectedClan.getClaimCount() == 0) {
					if(Clans.cfg.maxClanPlayerClaims <= 0 || selectedClan.getClaimCount() < selectedClan.getMaxClaimCount()) {
						if(selectedClan.getClaimCount() > 0)
							claimChunk(sender, c, selectedClan);
						else if(Clans.cfg.minClanHomeDist > 0 && Clans.cfg.initialClaimSeparationMultiplier > 0) {
							boolean inClanHomeRange = false;
							for(Map.Entry<Clan, BlockPos> pos: ClanCache.getClanHomes().entrySet())
								if(!pos.getKey().getClanId().equals(selectedClan.getClanId()) && pos.getKey().hasHome() && pos.getValue() != null && pos.getValue().getDistance(sender.getPosition().getX(), sender.getPosition().getY(), sender.getPosition().getZ()) < Clans.cfg.minClanHomeDist*Clans.cfg.initialClaimSeparationMultiplier)
									inClanHomeRange = true;
							if(inClanHomeRange) {
								if(Clans.cfg.enforceInitialClaimSeparation)
									sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.proximity_error", Clans.cfg.minClanHomeDist*Clans.cfg.initialClaimSeparationMultiplier).setStyle(TextStyles.RED));
								else if(CapHelper.getPlayerClanCapability(sender).getClaimWarning())
									return claimChunk(sender, c, selectedClan);
								else {
									sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.proximity_warning", Clans.cfg.minClanHomeDist*Clans.cfg.initialClaimSeparationMultiplier).setStyle(TextStyles.YELLOW));
									CapHelper.getPlayerClanCapability(sender).setClaimWarning(true);
								}
							} else
								return claimChunk(sender, c, selectedClan);
						} else
							return claimChunk(sender, c, selectedClan);
					} else
						sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.maxed", selectedClan.getClanName(), selectedClan.getMaxClaimCount()).setStyle(TextStyles.RED));
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.disconnected", selectedClan.getClanName()).setStyle(TextStyles.RED));
			}
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "clans.error.nochunkcap").setStyle(TextStyles.RED));
		return false;
	}

	private static boolean claimChunk(EntityPlayerMP sender, Chunk c, Clan selectedClan) {
		if (Clans.getPaymentHandler().deductAmount(Clans.cfg.claimChunkCost, selectedClan.getClanId())) {
			ChunkUtils.setChunkOwner(c, selectedClan.getClanId());
			ClanChunkData.addChunk(selectedClan, c.x, c.z, c.getWorld().provider.getDimension());
			selectedClan.addClaimCount();
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.success").setStyle(TextStyles.GREEN));
			return true;
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.insufficient_funds", selectedClan.getClanName(), Clans.cfg.claimChunkCost, Clans.getPaymentHandler().getCurrencyName(Clans.cfg.claimChunkCost)).setStyle(TextStyles.RED));
		return false;
	}
}
