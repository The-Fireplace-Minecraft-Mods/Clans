package the_fireplace.clans.commands;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import scala.xml.dtd.impl.Base;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class OpClanSubCommand extends ClanSubCommand {
	@Override
	public final String getName() {
		return "opclan";
	}

	@Override
	public final boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender.canUseCommand(server.getOpPermissionLevel(), this.getName());
	}

	@Override
	public final EnumRank getRequiredClanRank(){
		return EnumRank.LEADER;
	}
}
