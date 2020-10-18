package the_fireplace.clans.commands.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.cache.PlayerAutoClaimData;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.logic.ClanManagementLogic;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandAutoClaim extends ClanSubCommand {
	@Override
	public String getName() {
		return "autoclaim";
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
        Clan rm = PlayerAutoClaimData.cancelAutoClaim(sender.getUniqueID());
		if(rm == null) {
            PlayerAutoClaimData.activateAutoClaim(sender.getUniqueID(), selectedClan);
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autoclaim.start", selectedClan.getName()).setStyle(TextStyles.GREEN));
			ClanManagementLogic.checkAndAttemptClaim(sender, selectedClan, false);
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autoclaim.stop", rm.getName()).setStyle(TextStyles.GREEN));
	}
}
