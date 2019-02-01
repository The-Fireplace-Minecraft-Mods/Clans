package the_fireplace.clans.commands;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.MinecraftColors;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandAbandonclaim extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.ADMIN;
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
	public String getUsage(ICommandSender sender) {
		return "/clan abandonclaim";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Clan playerClan = ClanCache.getPlayerClan(sender.getUniqueID());
		assert playerClan != null;
		Chunk c = sender.getEntityWorld().getChunk(sender.getPosition());
		if(c.hasCapability(Clans.CLAIMED_LAND, null)){
			UUID claimFaction = Objects.requireNonNull(c.getCapability(Clans.CLAIMED_LAND, null)).getClan();
			if(claimFaction != null) {
				if(claimFaction.equals(playerClan.getClanId())) {
					//Unset clan home if it is in the chunk
					if(sender.dimension == playerClan.getHomeDim()
							&& playerClan.getHome().getX() >= c.getPos().getXStart()
							&& playerClan.getHome().getX() <= c.getPos().getXEnd()
							&& playerClan.getHome().getZ() >= c.getPos().getZStart()
							&& playerClan.getHome().getZ() <= c.getPos().getZEnd()){
						playerClan.unsetHome();
					}

					Objects.requireNonNull(c.getCapability(Clans.CLAIMED_LAND, null)).setClan(null);
					sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "Claim abandoned!"));
				} else
					sender.sendMessage(new TextComponentString(MinecraftColors.RED + "This land does not belong to you."));
			} else
				sender.sendMessage(new TextComponentString(MinecraftColors.RED + "This land is not claimed."));
		} else {
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Internal error: This chunk doesn't appear to be claimable."));
		}
	}
}
