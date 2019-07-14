package the_fireplace.clans.commands.details;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.forge.legacy.CapHelper;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandPlayerInfo extends ClanSubCommand {
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
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.playerinfo.usage");
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) {
		if(args.length == 0) {
			if(sender instanceof EntityPlayerMP)
				showDetails(server, sender, ((EntityPlayerMP)sender).getGameProfile());
			else
				sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.playerinfo.console", TranslationUtil.getStringTranslation(getUsage(sender))).setStyle(TextStyles.RED));
		} else {
			GameProfile targetPlayer = Clans.getMinecraftHelper().getServer().getPlayerProfileCache().getGameProfileForUsername(args[0]);
			if(targetPlayer == null)
				sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.playerinfo.notfound", args[0]).setStyle(TextStyles.RED));
			else
				showDetails(server, sender, targetPlayer);
		}
	}

	@Override
	protected boolean allowConsoleUsage() {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return args.length == 1 ? Lists.newArrayList(ClanCache.getClanNames().keySet()) : Collections.emptyList();
	}

	private void showDetails(MinecraftServer server, ICommandSender sender, GameProfile target) {
		sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.playerinfo.name", target.getName()).setStyle(TextStyles.GREEN));
		List<Clan> leaders = Lists.newArrayList();
		List<Clan> admins = Lists.newArrayList();
		List<Clan> members = Lists.newArrayList();
		for(Clan clan: ClanCache.getPlayerClans(target.getId())) {
			EnumRank rank = clan.getMembers().get(target.getId());
			switch(rank){
				case LEADER:
					leaders.add(clan);
					break;
				case ADMIN:
					admins.add(clan);
					break;
				case MEMBER:
					members.add(clan);
					break;
			}
		}
		if(!leaders.isEmpty() || !admins.isEmpty() || !members.isEmpty()) {
			Clan defaultClan = null;
			if(ArrayUtils.contains(server.getOnlinePlayerProfiles(), target))
				defaultClan = ClanCache.getClanById(CapHelper.getPlayerClanCapability(server.getPlayerList().getPlayerByUUID(target.getId())).getDefaultClan());
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.playerinfo.clans").setStyle(TextStyles.GREEN));
			for(Clan leader: leaders)
				sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.playerinfo.leader", leader.getClanName()).setStyle(defaultClan != null && leader.getClanId().equals(defaultClan.getClanId()) ? TextStyles.ONLINE_ADMIN : TextStyles.GREEN));
			for(Clan admin: admins)
				sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.playerinfo.admin", admin.getClanName()).setStyle(defaultClan != null && admin.getClanId().equals(defaultClan.getClanId()) ? TextStyles.ONLINE_ADMIN : TextStyles.GREEN));
			for(Clan member: members)
				sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.playerinfo.member", member.getClanName()).setStyle(defaultClan != null && member.getClanId().equals(defaultClan.getClanId()) ? TextStyles.ONLINE_ADMIN : TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.playerinfo.noclans", target.getName()).setStyle(TextStyles.GREEN));
	}
}
