package the_fireplace.clans.legacy.commands.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.data.ClaimData;
import the_fireplace.clans.legacy.logic.ClaimManagement;
import the_fireplace.clans.legacy.model.ChunkPositionWithData;
import the_fireplace.clans.legacy.model.EnumRank;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandAbandonAll extends ClanSubCommand {
	@Override
	public String getName() {
		return "abandonall";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.LEADER;
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
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
		for(ChunkPositionWithData claim: ClaimData.getClaimedChunks(selectedClan.getId()))
			ClaimManagement.abandonClaim(claim.getPosX(), claim.getPosZ(), claim.getDim(), selectedClan);
	}
}
