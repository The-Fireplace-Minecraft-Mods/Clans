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
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandSetLimitless extends OpClanSubCommand {
	@Override
	public int getMinArgs() {
		return 2;
	}

	@Override
	public int getMaxArgs() {
		return 2;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return TranslationUtil.getRawTranslationString(sender, "commands.opclan.setshield.usage");
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String clan = args[0];
		Clan c = ClanCache.getClanByName(clan);
		if(c != null) {
			boolean limitless;
			if(args[1].matches("\\d+") && parseInt(args[1]) == 1 || args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("t"))
				limitless = true;
			else if(args[1].matches("\\d+") && parseInt(args[1]) == 0 || args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("f"))
				limitless = false;
			else {
				sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.opclan.setlimitless.format", args[1]).setStyle(TextStyles.RED));
				return;
			}
			c.setLimitless(limitless);
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.opclan.setlimitless.success_"+(limitless ? 't' : 'f'), c.getClanName()).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clan).setStyle(TextStyles.RED));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return args.length == 1 ? Lists.newArrayList(ClanCache.getClanNames().keySet()) : Collections.emptyList();
	}
}
