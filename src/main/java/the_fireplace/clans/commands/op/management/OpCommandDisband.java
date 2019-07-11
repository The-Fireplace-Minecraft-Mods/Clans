package the_fireplace.clans.commands.op.management;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.data.ClanDatabase;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandDisband extends OpClanSubCommand {
	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return TranslationUtil.getRawTranslationString(sender, "commands.opclan.disband.usage");
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) {
		String clan = args[0];
		Clan c = ClanCache.getClanByName(clan);
		if(c != null)
			disbandClan(server, sender, c);
		else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clan).setStyle(TextStyles.RED));
	}

	public static void disbandClan(MinecraftServer server, ICommandSender sender, Clan c) {
		if(!c.isOpclan()) {
			c.disband(server, sender, "commands.opclan.disband.disbanded", c.getClanName(), sender.getName());
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.opclan.disband.success", c.getClanName()).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.opclan.disband.opclan", c.getClanName()).setStyle(TextStyles.RED));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		ArrayList<String> removable = Lists.newArrayList(ClanCache.getClanNames().keySet());
		removable.remove(ClanDatabase.getOpClan().getClanName());
		return args.length == 1 ? removable : Collections.emptyList();
	}
}
