package the_fireplace.clans.commands.op.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.util.ClanManagementUtil;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
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
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
        if(!ClanCache.opAutoAbandonClaims.remove(sender.getUniqueID())) {
            ClanCache.opAutoAbandonClaims.add(sender.getUniqueID());
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.opclan.autoabandon.start").setStyle(TextStyles.YELLOW));
			ClanManagementUtil.checkAndAttemptAbandon(sender, null);
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.opclan.autoabandon.stop").setStyle(TextStyles.GREEN));
	}

	@Override
	protected boolean allowConsoleUsage() {
		return false;
	}
}
