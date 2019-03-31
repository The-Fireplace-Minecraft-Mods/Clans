package the_fireplace.clans.commands.op;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.clan.NewClanDatabase;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
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
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		int color;
		try {
			color = args[0].startsWith("0x") ? Integer.parseInt(args[0].substring(2), 16) : Integer.parseInt(args[0]);
		} catch(NumberFormatException e) {
			sender.sendMessage(new TextComponentTranslation("Invalid color integer: %s!", args[0]).setStyle(TextStyles.RED));
			return;
		}
		if(opSelectedClan.isOpclan()) {
			NewClanDatabase.getOpClan().setColor(color);
			sender.sendMessage(new TextComponentTranslation("%s color set!", NewClanDatabase.getOpClan().getClanName()).setStyle(TextStyles.GREEN));
		} else {
			opSelectedClan.setColor(color);
			sender.sendMessage(new TextComponentTranslation("%s color set!", opSelectedClan.getClanName()).setStyle(TextStyles.GREEN));
		}
	}
}
