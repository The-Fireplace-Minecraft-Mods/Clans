package the_fireplace.clans.commands.op.management;

import mcp.MethodsReturnNonnullByDefault;
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
public class OpCommandSetShield extends OpClanSubCommand {
	@Override
	public String getName() {
		return "setshield";
	}

	@Override
	public int getMinArgs() {
		return 2;
	}

	@Override
	public int getMaxArgs() {
		return 2;
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) {
		String clan = args[0];
		Clan c = ClanCache.getClanByName(clan);
		if(c != null) {
			long duration;
			try {
				duration = Long.parseLong(args[1]);
				if(duration < 0)
					duration = 0;
			} catch(NumberFormatException e) {
				sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.opclan.setshield.format").setStyle(TextStyles.RED));
				return;
			}
			c.setShield(duration);
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.opclan.setshield.success", c.getName(), duration).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clan).setStyle(TextStyles.RED));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, ClanCache.getClanNames().keySet()) : Collections.emptyList();
	}
}
