package the_fireplace.clans.commands.members;

import mcp.MethodsReturnNonnullByDefault;
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
public class CommandLeave extends ClanSubCommand {
	@Override
	public String getName() {
		return "leave";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.MEMBER;
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
		EnumRank senderRank = selectedClan.getMembers().get(sender.getUniqueID());
		if (unableToLeave(sender, senderRank))
			return;
		if(selectedClan.removeMember(sender.getUniqueID())) {
			PlayerData.updateDefaultClan(sender.getUniqueID(), selectedClan.getId());
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.leave.success", selectedClan.getName()).setStyle(TextStyles.GREEN));
			selectedClan.messageAllOnline(TextStyles.YELLOW, "commands.clan.leave.left", sender.getDisplayNameString(), selectedClan.getName());
		}
	}

	private boolean unableToLeave(EntityPlayerMP sender, EnumRank senderRank) {
		if(senderRank == EnumRank.LEADER && !selectedClan.isServer()) {
			if(selectedClan.getMembers().size() == 1){
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.leave.disband", selectedClan.getName()).setStyle(TextStyles.RED));
				return true;
			}
			if(selectedClan.getLeaderCount() <= 1) {
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.leave.promote", selectedClan.getName()).setStyle(TextStyles.RED));
				return true;
			}
		}
		return false;
	}
}
