package the_fireplace.clans.commands.details;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
			Clan targetClan = ClanCache.getClanByName(args[0]);
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

	private void showDetails(MinecraftServer server, EntityPlayerMP sender, Clan clan) {
		sender.sendMessage(new TextComponentString("Clan name: "+clan.getClanName()).setStyle(TextStyles.GREEN));
		sender.sendMessage(new TextComponentString("Clan description: "+clan.getDescription()).setStyle(TextStyles.GREEN));
		sender.sendMessage(new TextComponentString("Number of claims: "+clan.getClaimCount()).setStyle(TextStyles.GREEN));
		sender.sendMessage(new TextComponentString("Number of members: "+clan.getMemberCount()).setStyle(TextStyles.GREEN));
		List<EntityPlayerMP> leaders = Lists.newArrayList();
		List<EntityPlayerMP> admins = Lists.newArrayList();
		List<EntityPlayerMP> members = Lists.newArrayList();
		for(Map.Entry<EntityPlayerMP, EnumRank> member: clan.getOnlineMembers().entrySet()) {
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
			sender.sendMessage(new TextComponentString("Online members: ").setStyle(TextStyles.GREEN));
			for(EntityPlayerMP leader: leaders)
				sender.sendMessage(new TextComponentString("Leader " + leader.getName()).setStyle(TextStyles.BOLD_ITALIC_GREEN));
			for(EntityPlayerMP admin: admins)
				sender.sendMessage(new TextComponentString("Admin " + admin.getName()).setStyle(TextStyles.BOLD_GREEN));
			for(EntityPlayerMP member: members)
				sender.sendMessage(new TextComponentString(member.getName()).setStyle(TextStyles.GREEN));
		} else {
			sender.sendMessage(new TextComponentString("No online members.").setStyle(TextStyles.GREEN));
		}
	}
}
