package the_fireplace.clans.commands.members;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.util.MinecraftColors;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandPromote extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.LEADER;
	}

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
		return "/clan promote <player>";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		Clan playerClan = ClanCache.getPlayerClan(sender.getUniqueID());
		assert playerClan != null;
		EntityPlayerMP target = getPlayer(server, sender, args[0]);
		if(ClanCache.getPlayerClan(target.getUniqueID()) != null) {
			if(Objects.requireNonNull(ClanCache.getPlayerClan(target.getUniqueID())).getClanId().equals(playerClan.getClanId())) {
				if(playerClan.promoteMember(target.getUniqueID())) {
					sender.sendMessage(new TextComponentTranslation(MinecraftColors.GREEN + "You have promoted %s.", target.getName()));
					target.sendMessage(new TextComponentTranslation(MinecraftColors.GREEN + "You have been promoted by %s.", sender.getName()));
				} else
					sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "The player %s could not be promoted.", target.getName()));
			} else
				sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "The player %s is not in your clan.", target.getName()));
		} else
			sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "The player %s is not in your clan.", target.getName()));
	}
}
