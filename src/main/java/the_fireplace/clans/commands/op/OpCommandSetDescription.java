package the_fireplace.clans.commands.op;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.MinecraftColors;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.commands.OpClanSubCommand;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

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
		StringBuilder newTagline = new StringBuilder();
		for(String arg: args)
			newTagline.append(arg).append(' ');
		ClanDatabase.getOpClan().setDescription(newTagline.toString());
		sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "Opclan description set!"));
	}
}
