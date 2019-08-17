package the_fireplace.clans.commands.op.management;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandSetColor extends OpClanSubCommand {
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
		return TranslationUtil.getRawTranslationString(sender, "commands.opclan.setcolor.usage");
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) {
		String clan = args[0];
		Clan c = ClanCache.getClanByName(clan);
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
			c.setColor(color);
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.setcolor.success", c.getClanName()).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clan).setStyle(TextStyles.RED));
	}
}
