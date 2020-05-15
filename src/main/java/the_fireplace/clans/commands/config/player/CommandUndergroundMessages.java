package the_fireplace.clans.commands.config.player;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.data.PlayerData;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandUndergroundMessages extends ClanSubCommand {
	@Override
	public String getName() {
		return "undergroundmessages";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.ANY;
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
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		boolean showMessages = parseBool(args[0]);
		PlayerData.setShowUndergroundMessages(sender.getUniqueID(), showMessages);
		sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.undergroundmessages.success").setStyle(TextStyles.GREEN));
	}
}
