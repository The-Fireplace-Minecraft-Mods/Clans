package the_fireplace.clans.commands.op;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.NewClanDatabase;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandSetName extends OpClanSubCommand {
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
		return "/opclan [target clan] setname <newname>";
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String newName = args[0];
		if(!ClanCache.clanNameTaken(newName)) {
			String oldName = opSelectedClan.getClanName();
			opSelectedClan.setClanName(newName);
			sender.sendMessage(new TextComponentTranslation("%s %srenamed to %s!", oldName, opSelectedClan.isOpclan() ? "(Opclan) " : "", newName).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(new TextComponentString("The clan name you have specified is already taken.").setStyle(TextStyles.RED));
	}
}
