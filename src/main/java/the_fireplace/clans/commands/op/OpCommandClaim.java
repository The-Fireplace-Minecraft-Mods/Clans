package the_fireplace.clans.commands.op;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanChunkCache;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.TextStyles;

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
		return "/opclan claim [force]";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Chunk c = sender.getEntityWorld().getChunk(sender.getPosition());
		if(c.hasCapability(Clans.CLAIMED_LAND, null)){
			UUID claimOwner = ChunkUtils.getChunkOwner(c);
			Clan claimOwnerClan = claimOwner != null ? ClanCache.getClanById(claimOwner) : null;
			boolean force = (args.length == 1 && args[0].toLowerCase().equals("force"));
			if(claimOwner != null && claimOwnerClan != null && (!force || claimOwner.equals(opSelectedClan.getClanId()))) {
				if(claimOwner.equals(opSelectedClan.getClanId()))
					sender.sendMessage(new TextComponentTranslation("%s has already claimed this land.", opSelectedClan.getClanName()).setStyle(TextStyles.RED));
				else
					sender.sendMessage(new TextComponentTranslation("Another clan (%1$s) has already claimed this land. To take this land from %1$s, use /opclan claim [clan] force.", claimOwnerClan.getClanName()).setStyle(TextStyles.RED));
			} else {
				if(claimOwnerClan != null) {
					claimOwnerClan.subClaimCount();
					Clans.getPaymentHandler().addAmount(Clans.cfg.claimChunkCost, claimOwnerClan.getClanId());
				}
				if(opSelectedClan.isOpclan()) {
					ChunkUtils.setChunkOwner(c, opSelectedClan.getClanId());
					ClanChunkCache.addChunk(opSelectedClan, c.x, c.z, c.getWorld().provider.getDimension());
					opSelectedClan.addClaimCount();
					sender.sendMessage(new TextComponentTranslation("Land claimed for %s!", opSelectedClan.getClanName()).setStyle(TextStyles.GREEN));
				} else {
					if(force || !Clans.cfg.forceConnectedClaims || ChunkUtils.hasConnectedClaim(c, opSelectedClan.getClanId()) || opSelectedClan.getClaimCount() == 0) {
						if(force || Clans.cfg.maxClanPlayerClaims <= 0 || opSelectedClan.getClaimCount() < opSelectedClan.getMaxClaimCount()) {
							if (force || Clans.getPaymentHandler().deductAmount(Clans.cfg.claimChunkCost, opSelectedClan.getClanId())) {
								ChunkUtils.setChunkOwner(c, opSelectedClan.getClanId());
								opSelectedClan.addClaimCount();
								sender.sendMessage(new TextComponentTranslation("Land claimed for %s!", opSelectedClan.getClanName()).setStyle(TextStyles.GREEN));
							} else
								sender.sendMessage(new TextComponentTranslation("Insufficient funds in %s's account to claim chunk. It costs %s %s.", opSelectedClan.getClanName(), Clans.cfg.claimChunkCost, Clans.getPaymentHandler().getCurrencyName(Clans.cfg.claimChunkCost)).setStyle(TextStyles.RED));
						} else
							sender.sendMessage(new TextComponentTranslation("%s is already at or above its max claim count of %s.", opSelectedClan.getClanName(), opSelectedClan.getMaxClaimCount()).setStyle(TextStyles.RED));
					} else
						sender.sendMessage(new TextComponentTranslation("You cannot claim this chunk of land because it is not next to another of %s's claims.", opSelectedClan.getClanName()).setStyle(TextStyles.RED));
				}
			}
		} else
			sender.sendMessage(new TextComponentString("Internal error: This chunk doesn't appear to be claimable.").setStyle(TextStyles.RED));
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
