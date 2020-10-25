package the_fireplace.clans.legacy.commands.op.management;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
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
		Clan c = ClanNameCache.getClanByName(clan);
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
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, ClanNameCache.getClanNames()) : Collections.emptyList();
	}
}
