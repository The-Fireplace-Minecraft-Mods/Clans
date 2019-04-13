package the_fireplace.clans.commands.members;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandInvite extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.ADMIN;//TODO Config option to let normal players or only the leader send invite
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
		return "/clan invite <player>";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		assert server != null;
		EntityPlayerMP target = getPlayer(server, sender, args[0]);
		if(Clans.cfg.allowMultiClanMembership || ClanCache.getPlayerClans(target.getUniqueID()).isEmpty()) {
			if(!ClanCache.getPlayerClans(target.getUniqueID()).contains(selectedClan)) {
				if(ClanCache.inviteToClan(target.getUniqueID(), selectedClan)) {
					sender.sendMessage(new TextComponentTranslation("You have invited %s to join %s.", target.getDisplayNameString(), selectedClan.getClanName()).setStyle(TextStyles.GREEN));
					target.sendMessage(new TextComponentTranslation("You have been invited to join %1$s. To join %1$s, type /clan accept. To decline, type /clan decline.", selectedClan.getClanName()).setStyle(TextStyles.GREEN));
				} else
					sender.sendMessage(new TextComponentTranslation("The player %s has already been invited to join a clan. They must accept or decline that invitation first.", target.getName()).setStyle(TextStyles.RED));
			} else
				sender.sendMessage(new TextComponentTranslation("The player %s is already in %s.", target.getName(), selectedClan.getClanName()).setStyle(TextStyles.RED));
		} else
			sender.sendMessage(new TextComponentTranslation("The player %s is already in a clan.", target.getName()).setStyle(TextStyles.RED));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		ArrayList<GameProfile> players = Lists.newArrayList(server.getPlayerList().getOnlinePlayerProfiles());
		if(!Clans.cfg.allowMultiClanMembership)
			players.removeIf(s -> !ClanCache.getPlayerClans(s.getId()).isEmpty());
		ArrayList<String> playerNames = Lists.newArrayList();
		for(GameProfile profile: players)
			playerNames.add(profile.getName());
		return args.length == 1 ? playerNames :Collections.emptyList();
	}
}
