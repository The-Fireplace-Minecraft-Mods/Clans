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
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.MinecraftColors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandKick extends ClanSubCommand {
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
	public String getUsage(ICommandSender sender) {
		return "/clan kick <player>";
	}

	@SuppressWarnings("Duplicates")
	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		Clan playerClan = ClanCache.getPlayerClan(sender.getUniqueID());
		assert playerClan != null;
		assert server != null;
		EntityPlayerMP target = getPlayer(server, sender, args[0]);//TODO support kicking offline players
		if(ClanCache.getPlayerClan(target.getUniqueID()) != null) {
			if(Objects.requireNonNull(ClanCache.getPlayerClan(target.getUniqueID())).getClanId().equals(playerClan.getClanId())) {
				EnumRank senderRank = playerClan.getMembers().get(sender.getUniqueID());
				EnumRank targetRank = playerClan.getMembers().get(target.getUniqueID());
				if(senderRank == EnumRank.LEADER) {
					removeMember(sender, playerClan, target);
				} else if(targetRank == EnumRank.MEMBER) {
					removeMember(sender, playerClan, target);
				} else
					sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "You do not have the authority to kick out %s.", target.getName()));
			} else
				sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "The player %s is not in your clan.", target.getName()));
		} else
			sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "The player %s is not in your clan.", target.getName()));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		ArrayList<GameProfile> players = Lists.newArrayList(server.getPlayerList().getOnlinePlayerProfiles());
		if(sender instanceof EntityPlayerMP) {//TODO support kicking offline players
			players.removeIf(s -> !Objects.equals(ClanCache.getPlayerClan(((EntityPlayerMP) sender).getUniqueID()), ClanCache.getPlayerClan(s.getId())));
		}
		ArrayList<String> playerNames = Lists.newArrayList();
		for(GameProfile profile: players)
			playerNames.add(profile.getName());
		return args.length == 1 ? playerNames : Collections.emptyList();
	}

	private void removeMember(EntityPlayerMP sender, Clan playerClan, EntityPlayerMP target) {
		if(playerClan.removeMember(target.getUniqueID())) {
			sender.sendMessage(new TextComponentTranslation(MinecraftColors.GREEN + "You have kicked %s out of the clan.", target.getName()));
			target.sendMessage(new TextComponentTranslation(MinecraftColors.GREEN + "You have been kicked out of %s by %s.", playerClan.getClanName(), sender.getName()));
		} else //Internal error because this should be unreachable
			sender.sendMessage(new TextComponentTranslation(MinecraftColors.RED + "Internal Error: The player %s is not in your clan.", target.getName()));
	}
}
