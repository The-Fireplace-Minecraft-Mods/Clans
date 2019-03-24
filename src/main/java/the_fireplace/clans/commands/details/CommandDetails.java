package the_fireplace.clans.commands.details;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandDetails extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.ANY;
	}

	@Override
	public int getMinArgs() {
		return 0;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/clan details [clan]";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		assert server != null;
		if(args.length == 0) {
			if(selectedClan == null) {
				sender.sendMessage(new TextComponentString("You are not in a clan. Use /clan details [clan] to get the details of another clan.").setStyle(TextStyles.RED));
			} else {
				showDetails(server, sender, selectedClan);
			}
		} else {
			NewClan targetClan = ClanCache.getClanByName(args[0]);
			if(targetClan == null) {
				sender.sendMessage(new TextComponentString("Target clan not found.").setStyle(TextStyles.RED));
			} else {
				showDetails(server, sender, targetClan);
			}
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return args.length == 1 ? Lists.newArrayList(ClanCache.getClanNames().keySet()) : Collections.emptyList();
	}

	@SuppressWarnings("ConstantConditions")
	private void showDetails(MinecraftServer server, EntityPlayerMP sender, NewClan clan) {
		sender.sendMessage(new TextComponentString("Clan name: "+clan.getClanName()).setStyle(TextStyles.GREEN));
		sender.sendMessage(new TextComponentString("Clan description: "+clan.getDescription()).setStyle(TextStyles.GREEN));
		sender.sendMessage(new TextComponentString("Number of claims: "+clan.getClaimCount()).setStyle(TextStyles.GREEN));
		if(!clan.isOpclan())
			sender.sendMessage(new TextComponentString("Number of members: "+clan.getMemberCount()).setStyle(TextStyles.GREEN));
		List<UUID> leaders = Lists.newArrayList();
		List<UUID> admins = Lists.newArrayList();
		List<UUID> members = Lists.newArrayList();
		for(Map.Entry<UUID, EnumRank> member: clan.getMembers().entrySet()) {
			switch(member.getValue()){
				case LEADER:
					leaders.add(member.getKey());
					break;
				case ADMIN:
					admins.add(member.getKey());
					break;
				case MEMBER:
					members.add(member.getKey());
					break;
			}
		}
		if(!leaders.isEmpty() || !admins.isEmpty() || !members.isEmpty()) {
			sender.sendMessage(new TextComponentString("Members: ").setStyle(TextStyles.GREEN));
			for(UUID leader: leaders) {
				GameProfile l = server.getPlayerProfileCache().getProfileByUUID(leader);
				if(l != null)
					sender.sendMessage(new TextComponentString("Leader " + l.getName()).setStyle(server.getPlayerList().getPlayerByUUID(leader) != null ? TextStyles.ONLINE_LEADER : TextStyles.OFFLINE_LEADER));
			}
			for(UUID admin: admins) {
				GameProfile a = server.getPlayerProfileCache().getProfileByUUID(admin);
				if(a != null)
					sender.sendMessage(new TextComponentString("Admin " + a.getName()).setStyle(server.getPlayerList().getPlayerByUUID(admin) != null ? TextStyles.ONLINE_ADMIN : TextStyles.OFFLINE_ADMIN));
			}
			for(UUID member: members) {
				GameProfile m = server.getPlayerProfileCache().getProfileByUUID(member);
				if(m != null)
					sender.sendMessage(new TextComponentString(m.getName()).setStyle(server.getPlayerList().getPlayerByUUID(member) != null ? TextStyles.GREEN : TextStyles.YELLOW));
			}
		} else if(!clan.isOpclan()) {
			sender.sendMessage(new TextComponentTranslation("Error: %s has no members.", clan.getClanName()).setStyle(TextStyles.RED));
			Clans.LOGGER.error("Clan %s has no members.", clan.getClanName());
		}
	}
}
