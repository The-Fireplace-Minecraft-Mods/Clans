package the_fireplace.clans.commands;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.clan.EnumRank;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class RaidSubCommand extends ClanSubCommand {
	@Override
	public final String getName() {
		return "raid";
	}

	@Override
	public final EnumRank getRequiredClanRank(){
		return EnumRank.ANY;
	}
}
