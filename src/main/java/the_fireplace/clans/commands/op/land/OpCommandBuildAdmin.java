package the_fireplace.clans.commands.op.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandBuildAdmin extends OpClanSubCommand {

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
		return TranslationUtil.getRawTranslationString(sender, "commands.opclan.buildadmin.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		if(ClanCache.toggleClaimAdmin(sender))
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.opclan.buildadmin.on").setStyle(TextStyles.YELLOW));
		else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.opclan.buildadmin.off").setStyle(TextStyles.GREEN));
	}

	@Override
	protected boolean allowConsoleUsage() {
		return false;
	}
}
