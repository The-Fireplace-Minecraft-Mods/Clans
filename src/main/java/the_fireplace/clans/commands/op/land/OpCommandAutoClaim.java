package the_fireplace.clans.commands.op.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.util.Pair;
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
		return 0;
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
		if(ClanCache.opAutoClaimLands.remove(sender.getUniqueID()) == null) {
			ClanCache.opAutoClaimLands.put(sender.getUniqueID(), new Pair<>(opSelectedClan, args.length == 1 && args[0].equals("force")));
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autoclaim.start", selectedClan.getClanName()).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autoclaim.stop", selectedClan.getClanName()).setStyle(TextStyles.GREEN));
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
