package the_fireplace.clans.legacy.commands.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.legacy.cache.PlayerAutoClaimData;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.logic.ClaimManagement;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandAutoAbandon extends ClanSubCommand {
	@Override
	public String getName() {
		return "autoabandon";
	}

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
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
        Clan rm = PlayerAutoClaimData.cancelAutoAbandon(sender.getUniqueID());
		if(rm == null) {
			PlayerAutoClaimData.activateAutoAbandon(sender.getUniqueID(), selectedClan);
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autoabandon.start", selectedClan.getName()).setStyle(TextStyles.GREEN));
			ClaimManagement.checkAndAttemptAbandon(sender, selectedClan);
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autoabandon.stop", rm.getName()).setStyle(TextStyles.GREEN));
	}
}
