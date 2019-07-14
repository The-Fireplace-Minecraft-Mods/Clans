package the_fireplace.clans.commands.op.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.data.ClanChunkData;
import the_fireplace.clans.forge.ClansForge;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
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
		return TranslationUtil.getRawTranslationString(sender, "commands.opclan.claim.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		checkAndAttemptOpClaim(sender, args, opSelectedClan);
	}

	public static void checkAndAttemptOpClaim(EntityPlayerMP sender, String[] args, Clan opSelectedClan) {
		Chunk c = sender.getEntityWorld().getChunk(sender.getPosition());
		if(c.hasCapability(ClansForge.CLAIMED_LAND, null)){
			UUID claimOwner = ChunkUtils.getChunkOwner(c);
			Clan claimOwnerClan = claimOwner != null ? ClanCache.getClanById(claimOwner) : null;
			boolean force = (args.length == 1 && args[0].toLowerCase().equals("force"));
			if(claimOwner != null && claimOwnerClan != null && (!force || claimOwner.equals(opSelectedClan.getClanId()))) {
				if(claimOwner.equals(opSelectedClan.getClanId()))
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.taken", opSelectedClan.getClanName()).setStyle(TextStyles.RED));
				else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.opclan.claim.taken_other", claimOwnerClan.getClanName()).setStyle(TextStyles.RED));
			} else {
				if(claimOwnerClan != null) {
					claimOwnerClan.subClaimCount();
					Clans.getPaymentHandler().addAmount(Clans.getConfig().getClaimChunkCost(), claimOwnerClan.getClanId());
				}
				if(opSelectedClan.isOpclan()) {
					ChunkUtils.setChunkOwner(c, opSelectedClan.getClanId());
					ClanChunkData.addChunk(opSelectedClan, c.x, c.z, c.getWorld().provider.getDimension());
					opSelectedClan.addClaimCount();
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.opclan.claim.success", opSelectedClan.getClanName()).setStyle(TextStyles.GREEN));
				} else {
					if(force || !Clans.getConfig().isForceConnectedClaims() || ChunkUtils.hasConnectedClaim(c, opSelectedClan.getClanId()) || opSelectedClan.getClaimCount() == 0) {
						if(force || Clans.getConfig().getMaxClanPlayerClaims() <= 0 || opSelectedClan.getClaimCount() < opSelectedClan.getMaxClaimCount()) {
							if (force || Clans.getPaymentHandler().deductAmount(Clans.getConfig().getClaimChunkCost(), opSelectedClan.getClanId())) {
								ChunkUtils.setChunkOwner(c, opSelectedClan.getClanId());
								opSelectedClan.addClaimCount();
								sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.opclan.claim.success", opSelectedClan.getClanName()).setStyle(TextStyles.GREEN));
							} else
								sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.insufficient_funds", opSelectedClan.getClanName(), Clans.getConfig().getClaimChunkCost(), Clans.getPaymentHandler().getCurrencyName(Clans.getConfig().getClaimChunkCost())).setStyle(TextStyles.RED));
						} else
							sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.maxed", opSelectedClan.getClanName(), opSelectedClan.getMaxClaimCount()).setStyle(TextStyles.RED));
					} else
						sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.claim.disconnected", opSelectedClan.getClanName()).setStyle(TextStyles.RED));
				}
			}
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "clans.error.nochunkcap").setStyle(TextStyles.RED));
	}

	@Override
	protected boolean allowConsoleUsage() {
		return false;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return args.length == 1 ? Collections.singletonList("force") : Collections.emptyList();
	}
}
