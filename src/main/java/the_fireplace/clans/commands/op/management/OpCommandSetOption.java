package the_fireplace.clans.commands.op.management;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandSetOption extends OpClanSubCommand {
	@Override
	public String getName() {
		return "setoption";
	}

	@Override
	public int getMinArgs() {
		return 3;
	}

	@Override
	public int getMaxArgs() {
		return 3;
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String clan = args[0];
		Clan c = ClanCache.getClanByName(clan);
		if(c != null) {
			String option = args[1].toLowerCase();
			if(Clan.DEFAULT_OPTIONS.containsKey(option)) {
				switch(option) {
					//Value is an int
					case "maxclaims":
					case "claimcost": {
						int value = parseInt(args[2]);
						c.setOption(option, value);
						sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.opclan.setoption.success", option, c.getName(), value < 0 ? "default" : value).setStyle(TextStyles.GREEN));
						break;
					}
					//Value is a boolean
					default: {
						Boolean value = parseBool(args[2], true);
						c.setOption(option, value == null ? -1 : value ? 1 : 0);
						sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.opclan.setoption.success", option, c.getName(), value == null ? "default" : value.toString()).setStyle(TextStyles.GREEN));
					}
				}
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.opclan.setoption.failed", option).setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clan).setStyle(TextStyles.RED));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		List<String> ret = Lists.newArrayList();
		if(args.length == 1)
			return getListOfStringsMatchingLastWord(args, ClanCache.getClanNames().keySet());
		else if(args.length == 2)
			return getListOfStringsMatchingLastWord(args, Clan.DEFAULT_OPTIONS.keySet());
		return ret;
	}
}
