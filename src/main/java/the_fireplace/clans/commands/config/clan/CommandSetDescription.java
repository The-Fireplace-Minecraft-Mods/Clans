package the_fireplace.clans.commands.config.clan;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.Clans;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSetDescription extends ClanSubCommand {
	@Override
	public String getName() {
		return "setdescription";
	}

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
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
		StringBuilder newTagline = new StringBuilder();
		for(String arg: args)
			newTagline.append(arg).append(' ');
		String descString = newTagline.toString();
		if(Clans.CensorConfig.censorClanDetails)
			descString = Clans.getChatCensorCompat().getCensoredString(descString);
		selectedClan.setDescription(descString);
		sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setdescription.success", selectedClan.getName()).setStyle(TextStyles.GREEN));
	}
}
