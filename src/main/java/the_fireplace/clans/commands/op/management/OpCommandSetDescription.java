package the_fireplace.clans.commands.op.management;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandSetDescription extends OpClanSubCommand {
	@Override
	public int getMinArgs() {
		return 2;
	}

	@Override
	public int getMaxArgs() {
		return Integer.MAX_VALUE;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return TranslationUtil.getRawTranslationString(sender, "commands.opclan.setdescription.usage");
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) {
		String clan = args[0];
		Clan c = ClanCache.getClanByName(clan);
		if(c != null) {
			StringBuilder newDescription = new StringBuilder();
			for(String arg: ArrayUtils.subarray(args, 1, args.length))
				newDescription.append(arg).append(' ');
			c.setDescription(newDescription.toString());
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.setdescription.success", c.getClanName()).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clan).setStyle(TextStyles.RED));
	}
}
