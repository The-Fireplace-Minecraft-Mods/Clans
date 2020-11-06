package the_fireplace.clans.legacy.commands.op.management;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.legacy.commands.OpClanSubCommand;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandSetColor extends OpClanSubCommand {
	@Override
	public String getName() {
		return "setcolor";
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
		Clan c = ClanNames.getClanByName(clan);
		if(c != null) {
			int color;
			try {
				if(TextStyles.colorStrings.containsKey(args[1].toLowerCase()))
					color = TextStyles.colorStrings.get(args[1].toLowerCase());
				else
					color = args[1].startsWith("0x") ? Integer.parseInt(args[1].substring(2), 16) : Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.setcolor.invalid", args[1]).setStyle(TextStyles.RED));
				return;
			}
            c.getClanMetadata().setColor(color);
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.setcolor.success", c.getClanMetadata().getClanName()).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clan).setStyle(TextStyles.RED));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		if(args.length == 1)
			return getListOfStringsMatchingLastWord(args, ClanNames.getClanNames());
		else if(args.length == 2)
			return getListOfStringsMatchingLastWord(args, TextStyles.colorStrings.keySet());
		return Collections.emptyList();
	}
}
