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
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ClanSubCommand extends CommandBase {
	@Override
	public final String getName() {
		return "clan";
	}

	@Override
	public final boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		if(sender instanceof Entity) {
			EnumRank playerRank = ClanCache.getPlayerRank(Objects.requireNonNull(sender.getCommandSenderEntity()).getUniqueID());
			if(getRequiredClanRank() == EnumRank.ANY)
				return true;
			switch(playerRank){
				case LEADER:
					return getRequiredClanRank() != EnumRank.NOCLAN;
				case ADMIN:
					return getRequiredClanRank() != EnumRank.LEADER && getRequiredClanRank() != EnumRank.NOCLAN;
				case MEMBER:
					return getRequiredClanRank() == EnumRank.MEMBER;
				case NOCLAN:
					return getRequiredClanRank() == EnumRank.NOCLAN;
				default:
					return false;
			}
		}
		return false;
	}

	public abstract EnumRank getRequiredClanRank();
	public abstract int getMinArgs();
	public abstract int getMaxArgs();

	protected final void throwWrongUsage(ICommandSender sender) throws WrongUsageException{
		throw new WrongUsageException(getUsage(sender));
	}

	public final void execute(@Nullable MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(sender instanceof EntityPlayerMP){
			if(args.length >= getMinArgs() && args.length <= getMaxArgs()) {
				if(checkPermission(server, sender))
					run(server, (EntityPlayerMP) sender, args);
				else
					sender.sendMessage(new TextComponentTranslation("commands.generic.permission").setStyle(new Style().setColor(TextFormatting.RED)));
			} else
				throwWrongUsage(sender);
		} else
			throw new WrongUsageException("You must be a player to do this");
	}

	protected abstract void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException;
}
