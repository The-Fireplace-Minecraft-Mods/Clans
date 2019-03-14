package the_fireplace.clans.commands.op;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.MinecraftColors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandAbandomClaim extends OpClanSubCommand {
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
		return "/opclan abandonclaim [force]";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Clan opClan = ClanDatabase.getOpClan();
		Chunk c = sender.getEntityWorld().getChunk(sender.getPosition());
		if(c.hasCapability(Clans.CLAIMED_LAND, null)){
			UUID claimFaction = Objects.requireNonNull(c.getCapability(Clans.CLAIMED_LAND, null)).getClan();
			if(claimFaction != null) {
				Clan targetClan = ClanCache.getClan(claimFaction);
				if(claimFaction.equals(opClan.getClanId()) || (args.length == 1 && args[0].toLowerCase().equals("force")) || targetClan == null) {
					if(targetClan != null) {
						if (targetClan.isOpclan() || !Clans.cfg.forceConnectedClaims || !ChunkUtils.hasConnectedClaim(c, targetClan.getClanId())) {
							//Unset clan home if it is in the chunk
							OpCommandAbandomClaim.abandonClaim(sender, c, targetClan);
							ChunkUtils.clearChunkOwner(c);
							sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "Claim abandoned!"));
						} else {//We are forcing connected claims and there is a claim connected
							//Prevent creation of disconnected claims
							abandonClaimWithAdjacencyCheck(sender, c, targetClan);
						}
					} else {
						ChunkUtils.clearChunkOwner(c);
						sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "Claim abandoned!"));
					}
				} else
					sender.sendMessage(new TextComponentString(MinecraftColors.RED + "This land does not belong to opclan. To force "+targetClan.getClanName()+" to abandon it, use /opclan abandonclaim force"));
			} else
				sender.sendMessage(new TextComponentString(MinecraftColors.RED + "This land is not claimed."));
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Internal error: This chunk doesn't appear to be claimable."));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return args.length == 1 ? Collections.singletonList("force") : Collections.emptyList();
	}

	public static void abandonClaim(EntityPlayerMP sender, Chunk c, Clan targetClan) {
		if (targetClan.hasHome()
				&& sender.dimension == targetClan.getHomeDim()
				&& targetClan.getHome().getX() >= c.getPos().getXStart()
				&& targetClan.getHome().getX() <= c.getPos().getXEnd()
				&& targetClan.getHome().getZ() >= c.getPos().getZStart()
				&& targetClan.getHome().getZ() <= c.getPos().getZEnd()) {
			targetClan.unsetHome();
		}

		targetClan.subClaimCount();
		Clans.getPaymentHandler().addAmount(Clans.cfg.claimChunkCost, targetClan.getClanId());
	}

	public static void abandonClaimWithAdjacencyCheck(EntityPlayerMP sender, Chunk c, Clan targetClan) {
		boolean allowed = true;
		for (Chunk checkChunk : ChunkUtils.getConnectedClaims(c, targetClan.getClanId()))
			if (ChunkUtils.getConnectedClaims(checkChunk, targetClan.getClanId()).equals(Lists.newArrayList(c))) {
				allowed = false;
				break;
			}
		if (allowed) {
			//Unset clan home if it is in the chunk
			OpCommandAbandomClaim.abandonClaim(sender, c, targetClan);
			ChunkUtils.clearChunkOwner(c);
			sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "Claim abandoned!"));
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "You cannot abandon this chunk of land because doing so would create at least one disconnected claim."));
	}
}