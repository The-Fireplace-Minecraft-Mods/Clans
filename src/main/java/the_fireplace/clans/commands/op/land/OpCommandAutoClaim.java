package the_fireplace.clans.commands.op.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.cache.PlayerAutoClaimData;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.logic.ClanManagementLogic;
import the_fireplace.clans.model.Clan;
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
	public String getName() {
		return "autoclaim";
	}

	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}

	@Override
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
		String attemptClanName = args[0];
		Clan clan = ClanCache.getClanByName(attemptClanName);
		if(clan != null) {
            Clan rm = PlayerAutoClaimData.cancelOpAutoClaim(sender.getUniqueID());
			if(rm == null) {
                PlayerAutoClaimData.activateOpAutoClaim(sender.getUniqueID(), clan);
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autoclaim.start", clan.getName()).setStyle(TextStyles.GREEN));
				ClanManagementLogic.checkAndAttemptClaim(sender, clan, true);
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autoclaim.stop", rm.getName()).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", attemptClanName).setStyle(TextStyles.RED));
	}

	@Override
	protected boolean allowConsoleUsage() {
		return false;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, ClanCache.getClanNames().keySet()) : Collections.emptyList();
	}
}
