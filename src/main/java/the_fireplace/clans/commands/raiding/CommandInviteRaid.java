package the_fireplace.clans.commands.raiding;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.cache.RaidingParties;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.model.Raid;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandInviteRaid extends RaidSubCommand {
	@Override
	public String getName() {
		return "invite";
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
		if(!RaidingParties.getRaidingPlayers().contains(sender.getUniqueID())) {
			Raid raid = RaidingParties.getRaid(sender);
			if (raid != null) {

				GameProfile targetProfile = server.getPlayerProfileCache().getGameProfileForUsername(args[0]);
				EntityPlayerMP target = targetProfile != null ? server.getPlayerList().getPlayerByUUID(targetProfile.getId()) : null;
				if(target != null) {
					Map<EntityPlayerMP, EnumRank> clanPlayers = raid.getTarget().getOnlineMembers();
					if (clanPlayers.size() > raid.getAttackerCount() - Clans.getConfig().getMaxRaidersOffset()) {
						if (!clanPlayers.containsKey(target)) {
							target.sendMessage(TranslationUtil.getTranslation(target.getUniqueID(), "commands.raid.invite.invited", raid.getTarget().getName()).setStyle(TextStyles.GREEN));
							sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.invite.success", target.getName()).setStyle(TextStyles.GREEN));
						} else
							sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.invite.inclan").setStyle(TextStyles.RED));
					} else
						sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.invite.limit", raid.getAttackerCount(), clanPlayers.size() + Clans.getConfig().getMaxRaidersOffset()).setStyle(TextStyles.RED));
				} else
					throw new PlayerNotFoundException("commands.generic.player.notFound", args[0]);
			} else {
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.common.notinparty").setStyle(TextStyles.RED));
				Clans.getMinecraftHelper().getLogger().error("Player was in getRaidingPlayers but getRaid was null!");
			}
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.common.notinparty").setStyle(TextStyles.RED));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		ArrayList<GameProfile> players = Lists.newArrayList(server.getPlayerList().getOnlinePlayerProfiles());
		if(sender instanceof EntityPlayerMP) {
			Raid r = RaidingParties.getRaid((EntityPlayerMP)sender);
			if (r != null)
				players.removeIf(s -> ClanCache.getPlayerClans(s.getId()).contains(r.getTarget()));
		}
		ArrayList<String> playerNames = Lists.newArrayList();
		for(GameProfile profile: players)
			playerNames.add(profile.getName());
		return args.length == 1 ? playerNames : Collections.emptyList();
	}
}
