package the_fireplace.clans.legacy.commands.op.management;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanNameCache;
import the_fireplace.clans.legacy.commands.OpClanSubCommand;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandSetDescription extends OpClanSubCommand {
	@Override
	public String getName() {
		return "setdescription";
	}

	@Override
	public int getMinArgs() {
		return 2;
	}

	@Override
	public int getMaxArgs() {
		return Integer.MAX_VALUE;
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) {
		String clan = args[0];
		Clan c = ClanNameCache.getClanByName(clan);
		if(c != null) {
			StringBuilder newDescription = new StringBuilder();
			for(String arg: ArrayUtils.subarray(args, 1, args.length))
				newDescription.append(arg).append(' ');
			c.setDescription(newDescription.toString());
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.setdescription.success", c.getName()).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clan).setStyle(TextStyles.RED));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, ClanNameCache.getClanNames()) : Collections.emptyList();
	}
}
