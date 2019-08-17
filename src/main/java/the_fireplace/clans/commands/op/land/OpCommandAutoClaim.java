package the_fireplace.clans.commands.op.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.util.ClanManagementUtil;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandAutoClaim extends OpClanSubCommand {
	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return TranslationUtil.getRawTranslationString(sender, "commands.opclan.autoclaim.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		String clan = args[0];
		Clan c = ClanCache.getClanByName(clan);
		if(c != null) {
			if(ClanCache.getOpAutoClaimLands().remove(sender.getUniqueID()) == null) {
				ClanCache.getOpAutoClaimLands().put(sender.getUniqueID(), c);
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autoclaim.start", c.getClanName()).setStyle(TextStyles.GREEN));
				ClanManagementUtil.checkAndAttemptClaim(sender, c, true);
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autoclaim.stop", c.getClanName()).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clan).setStyle(TextStyles.RED));
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
