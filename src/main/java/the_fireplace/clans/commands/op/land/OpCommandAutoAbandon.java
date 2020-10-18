package the_fireplace.clans.commands.op.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.cache.PlayerAutoClaimData;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.logic.ClaimManagement;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandAutoAbandon extends OpClanSubCommand {
	@Override
	public String getName() {
		return "autoabandon";
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
        if(!PlayerAutoClaimData.cancelOpAutoAbandon(sender.getUniqueID())) {
            PlayerAutoClaimData.activateOpAutoAbandon(sender.getUniqueID());
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.opclan.autoabandon.start").setStyle(TextStyles.YELLOW));
			ClaimManagement.checkAndAttemptAbandon(sender, null);
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.opclan.autoabandon.stop").setStyle(TextStyles.GREEN));
	}

	@Override
	protected boolean allowConsoleUsage() {
		return false;
	}
}
