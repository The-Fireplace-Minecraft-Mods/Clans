package the_fireplace.clans.legacy.commands.members;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.membership.PlayerClans;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.logic.ClanMemberManagement;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandKick extends ClanSubCommand {
	@Override
	public String getName() {
		return "kick";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.ADMIN;
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
		GameProfile target = parsePlayerName(server, args[0]);

		if(target.getId().equals(sender.getUniqueID())) {
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.kick.leave").setStyle(TextStyles.RED));
			return;
		}
		if (!PlayerClans.getClansPlayerIsIn(target.getId()).isEmpty()) {
			if (PlayerClans.getClansPlayerIsIn(target.getId()).contains(selectedClan)) {
                EnumRank senderRank = ClanMembers.get().getMemberRanks().get(sender.getUniqueID());
                EnumRank targetRank = ClanMembers.get().getMemberRanks().get(target.getId());
				if (senderRank == EnumRank.LEADER || targetRank == EnumRank.MEMBER) {
					ClanMemberManagement.kickMember(server, sender, selectedClan, target);
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.kick.authority", target.getName()).setStyle(TextStyles.RED));
			} else
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.kick.not_in_clan", target.getName(), selectedClan.getClanMetadata().getClanName()).setStyle(TextStyles.RED));
		} else
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.player_not_in_clan", target.getName(), selectedClan.getClanMetadata().getClanName()).setStyle(TextStyles.RED));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		ArrayList<String> playerNames = Lists.newArrayList();
        if(selectedClan != null)
			for(UUID player: ClanMembers.get().getMemberRanks().keySet()) {
				GameProfile playerProf = server.getPlayerProfileCache().getProfileByUUID(player);
                if(playerProf != null && (ClanMembers.get().getMemberRanks().get(player).equals(EnumRank.MEMBER) || (sender instanceof EntityPlayerMP && ClanMembers.get().getMemberRanks().get(((EntityPlayerMP) sender).getUniqueID()).equals(EnumRank.LEADER))))
					playerNames.add(playerProf.getName());
			}
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, playerNames) : Collections.emptyList();
	}
}
