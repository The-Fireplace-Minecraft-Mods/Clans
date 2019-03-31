package the_fireplace.clans.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.CapHelper;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSetDefault extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.MEMBER;
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
		return "/clan setdefault <clan>";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		NewClan def = ClanCache.getClanByName(args[0]);
		if(def != null) {
			if(def.getMembers().containsKey(sender.getUniqueID())) {
				//noinspection ConstantConditions
				if (sender.hasCapability(Clans.CLAN_DATA_CAP, null)) {
					CapHelper.getPlayerClanCapability(sender).setDefaultClan(def.getClanId());
					sender.sendMessage(new TextComponentTranslation("Your default clan has been set to %s.", def.getClanName()).setStyle(TextStyles.GREEN));
				} else
					sender.sendMessage(new TextComponentString("Internal error: Player cannot set default clan.").setStyle(TextStyles.RED));
			} else
				sender.sendMessage(new TextComponentTranslation("You are not in %s.", def.getClanName()).setStyle(TextStyles.RED));
		} else
			sender.sendMessage(new TextComponentString("The clan you have specified does not exist.").setStyle(TextStyles.RED));
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) {

	}
}
