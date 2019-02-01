package the_fireplace.clans.commands;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ClanSubCommand extends CommandBase {

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		if(sender instanceof Entity) {
			EnumRank playerRank = ClanCache.getPlayerRank(Objects.requireNonNull(sender.getCommandSenderEntity()).getUniqueID());
			switch(playerRank){
				case LEADER:
					return true;
				case ADMIN:
					return getRequiredClanRank() != EnumRank.LEADER;
				case MEMBER:
					return getRequiredClanRank() == EnumRank.MEMBER;
				default:
					return false;
			}
		}
		return false;
	}

	public abstract EnumRank getRequiredClanRank();
}
