package the_fireplace.clans.legacy.commands.raiding;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.cache.RaidingParties;
import the_fireplace.clans.legacy.commands.RaidSubCommand;
import the_fireplace.clans.legacy.model.Raid;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandLeaveRaid extends RaidSubCommand {
	@Override
	public String getName() {
		return "leave";
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
		if(!RaidingParties.getRaidingPlayers().contains(sender.getUniqueID())) {
			Raid raid = RaidingParties.getRaid(sender);
			if (raid != null) {
				raid.removeAttacker(sender.getUniqueID());
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.leave.success", raid.getTarget().getClanMetadata().getClanName()).setStyle(TextStyles.GREEN));
			} else {
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.common.notinparty").setStyle(TextStyles.RED));
				ClansModContainer.getMinecraftHelper().getLogger().error("Player was in getRaidingPlayers but getRaid was null!");
			}
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.common.notinparty").setStyle(TextStyles.RED));
	}
}
