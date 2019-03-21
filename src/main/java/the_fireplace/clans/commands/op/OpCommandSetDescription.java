package the_fireplace.clans.commands.op;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandSetDescription extends OpClanSubCommand {
	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public int getMaxArgs() {
		return Integer.MAX_VALUE;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/opclan setdescription <new description>";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		StringBuilder newDescription = new StringBuilder();
		for(String arg: args)
			newDescription.append(arg).append(' ');
		if(opSelectedClan == null) {
			ClanDatabase.getOpClan().setDescription(newDescription.toString());
			sender.sendMessage(new TextComponentString("Opclan description set!").setStyle(TextStyles.GREEN));
		} else {
			opSelectedClan.setDescription(newDescription.toString());
			sender.sendMessage(new TextComponentTranslation("%s description set!", opSelectedClan.getClanName()).setStyle(TextStyles.GREEN));
		}
	}
}
