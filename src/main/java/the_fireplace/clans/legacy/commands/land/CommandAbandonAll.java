package the_fireplace.clans.legacy.commands.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.legacy.api.ClaimAccessor;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.logic.ClaimManagement;
import the_fireplace.clans.legacy.model.ChunkPositionWithData;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

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
		for(ChunkPositionWithData claim: ClaimAccessor.getInstance().getClaimedChunks(selectedClan))
			ClaimManagement.abandonClaim(claim.getPosX(), claim.getPosZ(), claim.getDim(), selectedClan);
		sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.abandonall.success", ClanNames.get(selectedClan).getName()).setStyle(TextStyles.GREEN));
	}
}
