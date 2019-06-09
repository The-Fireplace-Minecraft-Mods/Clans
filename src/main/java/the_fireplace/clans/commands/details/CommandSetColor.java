package the_fireplace.clans.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSetColor extends ClanSubCommand {
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
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.setcolor.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		try {
			int color;
			if(TextStyles.colorStrings.containsKey(args[0].toLowerCase()))
				color = TextStyles.colorStrings.get(args[0].toLowerCase());
			else
				color = args[0].startsWith("0x") ? Integer.parseInt(args[0].substring(2), 16) : Integer.parseInt(args[0]);
			selectedClan.setColor(color);
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setcolor.success", selectedClan.getClanName()).setStyle(TextStyles.GREEN));
		} catch(NumberFormatException e) {
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setcolor.invalid", args[0]).setStyle(TextStyles.RED));
		}
	}
}
