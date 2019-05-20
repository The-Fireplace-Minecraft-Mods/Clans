package the_fireplace.clans.commands.op;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandSetColor extends OpClanSubCommand {
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
		return "/opclan [target clan] setcolor <color>";
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		int color;
		try {
			color = args[0].startsWith("0x") ? Integer.parseInt(args[0].substring(2), 16) : Integer.parseInt(args[0]);
		} catch(NumberFormatException e) {
			sender.sendMessage(new TextComponentTranslation("Invalid color integer: %s!", args[0]).setStyle(TextStyles.RED));
			return;
		}
		opSelectedClan.setColor(color);
		sender.sendMessage(new TextComponentTranslation("%s color set!", opSelectedClan.getClanName()).setStyle(TextStyles.GREEN));
	}
}
