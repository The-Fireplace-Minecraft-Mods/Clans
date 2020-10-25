package the_fireplace.clans.legacy.commands.op.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.legacy.commands.OpClanSubCommand;
import the_fireplace.clans.legacy.logic.ClaimManagement;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandAbandonClaim extends OpClanSubCommand {
	@Override
	public String getName() {
		return "abandonclaim";
	}

	@Override
	public int getMinArgs() {
		return 0;
	}

	@Override
	public int getMaxArgs() {
		return 0;
	}

	@Override
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
		ClaimManagement.checkAndAttemptAbandon(sender, null);
	}

	@Override
	protected boolean allowConsoleUsage() {
		return false;
	}
}
