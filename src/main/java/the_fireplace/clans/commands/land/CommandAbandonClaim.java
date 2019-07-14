package the_fireplace.clans.commands.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.commands.op.land.OpCommandAbandonClaim;
import the_fireplace.clans.forge.ClansForge;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandAbandonClaim extends ClanSubCommand {
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
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.abandonclaim.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		checkAndAttemptAbandon(sender, selectedClan);
	}

	public static void checkAndAttemptAbandon(EntityPlayerMP sender, Clan selectedClan) {
		Chunk c = sender.getEntityWorld().getChunk(sender.getPosition());
		if(c.hasCapability(ClansForge.CLAIMED_LAND, null)){
			UUID claimFaction = ChunkUtils.getChunkOwner(c);
			if(claimFaction != null) {
				if(claimFaction.equals(selectedClan.getClanId())) {
					if(!Clans.getConfig().isForceConnectedClaims() || ChunkUtils.canBeAbandoned(c, selectedClan.getClanId())) {
						OpCommandAbandonClaim.abandonClaim(sender, c, selectedClan);
						ChunkUtils.clearChunkOwner(c);
						sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.abandonclaim.success").setStyle(TextStyles.GREEN));
					} else {//We are forcing connected claims and there is a claim connected
						//Prevent creation of disconnected claims
						OpCommandAbandonClaim.abandonClaimWithAdjacencyCheck(sender, c, selectedClan);
					}
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.abandonclaim.wrongclan", selectedClan.getClanName()).setStyle(TextStyles.RED));
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.abandonclaim.notclaimed").setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "clans.error.nochunkcap").setStyle(TextStyles.RED));
	}
}
