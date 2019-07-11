package the_fireplace.clans.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSetDescription extends ClanSubCommand {
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
		return Integer.MAX_VALUE;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.setdescription.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		StringBuilder newTagline = new StringBuilder();
		for(String arg: args)
			newTagline.append(arg).append(' ');
		selectedClan.setDescription(newTagline.toString());
		sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setdescription.success", selectedClan.getClanName()).setStyle(TextStyles.GREEN));
	}
}
