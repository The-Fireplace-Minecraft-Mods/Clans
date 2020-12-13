package the_fireplace.clans.legacy.commands.details;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.clan.economics.ClanRent;
import the_fireplace.clans.clan.economics.ClanUpkeep;
import the_fireplace.clans.clan.land.ClanClaimCount;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.metadata.ClanDescriptions;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.clan.raids.ClanWeaknessFactor;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.FormulaParser;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.TimeUtils;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.player.PlayerLastSeenData;
import the_fireplace.clans.player.PlayerRaidStats;

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
	public String getName() {
		return "details";
	}

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
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0) {
			if(selectedClan == null) {
				if(sender instanceof EntityPlayerMP)
					sender.sendMessage(TranslationUtil.getTranslation(((EntityPlayerMP) sender).getUniqueID(), "commands.clan.details.noclan", TranslationUtil.getTranslation(((EntityPlayerMP) sender).getUniqueID(), getUsage(sender)).getFormattedText()).setStyle(TextStyles.RED));
				else
					sender.sendMessage(TranslationUtil.getTranslation("commands.clan.details.console", TranslationUtil.getStringTranslation(getUsage(sender))).setStyle(TextStyles.RED));
			} else
				showDetails(server, sender, selectedClan);
		} else {
			UUID targetClan = ClanNames.getClanByName(args[0]);
			if(targetClan == null)
				sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", args[0]).setStyle(TextStyles.RED));
			else
				showDetails(server, sender, targetClan);
		}
	}

	@Override
	protected boolean allowConsoleUsage() {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return args.length == 1 ? Lists.newArrayList(ClanNames.getClanNames()) : Collections.emptyList();
	}

	@SuppressWarnings("ConstantConditions")
	private void showDetails(MinecraftServer server, ICommandSender sender, UUID clan) throws CommandException {
        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.name", ClanNames.get(clan).getName()).setStyle(TextStyles.GREEN));
		sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.desc", ClanDescriptions.get(clan).getDescription()).setStyle(TextStyles.GREEN));
        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.claimcount", ClanClaimCount.get(clan).getClaimCount()).setStyle(TextStyles.GREEN));
        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.membercount", ClanMembers.get(clan).getMemberCount()).setStyle(TextStyles.GREEN));
        if(ClansModContainer.getConfig().isIncreasingRewards() && !AdminControlledClanSettings.get(clan).isServerOwned())
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.multiplier", ClanWeaknessFactor.get(clan).getWeaknessFactor()).setStyle(TextStyles.GREEN));
        if(AdminControlledClanSettings.get(clan).isServerOwned())
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.server").setStyle(TextStyles.GREEN));
		List<UUID> leaders = Lists.newArrayList();
		List<UUID> admins = Lists.newArrayList();
		List<UUID> members = Lists.newArrayList();
        for(Map.Entry<UUID, EnumRank> member: ClanMembers.get(clan).getMemberRanks().entrySet()) {
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
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.members").setStyle(TextStyles.GREEN));
			for(UUID leader: leaders) {
				GameProfile l = server.getPlayerProfileCache().getProfileByUUID(leader);
				if(l != null)
					sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.leader", l.getName(), PlayerRaidStats.getRaidWLR(l.getId()), TimeUtils.getFormattedTime(PlayerLastSeenData.getLastSeen(l.getId()))).setStyle(server.getPlayerList().getPlayerByUUID(leader) != null ? TextStyles.ONLINE_LEADER : TextStyles.OFFLINE_LEADER));
			}
			for(UUID admin: admins) {
				GameProfile a = server.getPlayerProfileCache().getProfileByUUID(admin);
				if(a != null)
					sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.admin", a.getName(), PlayerRaidStats.getRaidWLR(a.getId()), TimeUtils.getFormattedTime(PlayerLastSeenData.getLastSeen(a.getId()))).setStyle(server.getPlayerList().getPlayerByUUID(admin) != null ? TextStyles.ONLINE_ADMIN : TextStyles.OFFLINE_ADMIN));
			}
			for(UUID member: members) {
				GameProfile m = server.getPlayerProfileCache().getProfileByUUID(member);
				if(m != null)
					sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.member", m.getName(), PlayerRaidStats.getRaidWLR(m.getId()), TimeUtils.getFormattedTime(PlayerLastSeenData.getLastSeen(m.getId()))).setStyle(server.getPlayerList().getPlayerByUUID(member) != null ? TextStyles.GREEN : TextStyles.YELLOW));
			}
		} else
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.no_members", ClanNames.get(clan).getName()).setStyle(TextStyles.RED));
		UUID senderId = sender instanceof EntityPlayerMP ? ((EntityPlayerMP) sender).getUniqueID() : null;
        if((senderId != null && (members.contains(senderId) || admins.contains(senderId) || leaders.contains(senderId)) || sender instanceof MinecraftServer) && !AdminControlledClanSettings.get(clan).isServerOwned()) {
			double upkeep = 0;
			double rent = 0;
			if(ClansModContainer.getConfig().getClanUpkeepDays() > 0) {
				upkeep += FormulaParser.eval(ClansModContainer.getConfig().getClanUpkeepCostFormula(), clan, 0);
				if(upkeep > 0) {
					sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.details.upkeep", Economy.getFormattedCurrency(upkeep), ClansModContainer.getConfig().getClanUpkeepDays()).setStyle(TextStyles.GREEN));
                    sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.details.upkeepdue", (ClanUpkeep.get(clan).getNextUpkeepTimestamp() -System.currentTimeMillis())/1000/60/60).setStyle(TextStyles.GREEN));
				}
			}
			if(ClansModContainer.getConfig().getChargeRentDays() > 0) {
                rent += ClanRent.get(clan).getRent();
				if(rent > 0) {
                    sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.details.rent", Economy.getFormattedCurrency(rent* ClanMembers.get(clan).getMemberCount()), ClansModContainer.getConfig().getChargeRentDays()).setStyle(TextStyles.GREEN));
                    sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.details.rent_individual", Economy.getFormattedCurrency(rent), ClansModContainer.getConfig().getChargeRentDays()).setStyle(TextStyles.GREEN));
                    sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.details.rentdue", (ClanRent.get(clan).getNextRentTimestamp() -System.currentTimeMillis())/1000/60/60).setStyle(TextStyles.GREEN));
				}
			}
			if(upkeep > 0 && rent > 0 && leaders.contains(senderId)) {
				upkeep /= ClansModContainer.getConfig().getClanUpkeepDays();
				rent /= ClansModContainer.getConfig().getChargeRentDays();
				sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.details.trend", (rent-upkeep) <= 0 ? Economy.getFormattedCurrency(rent-upkeep) : '+'+ Economy.getFormattedCurrency(rent-upkeep)).setStyle(rent >= upkeep ? TextStyles.GREEN : TextStyles.YELLOW));
				if(upkeep > rent) {
					double maxRent = FormulaParser.eval(ClansModContainer.getConfig().getMaxRentFormula(), clan, 0);
                    if(ClanRent.get(clan).getRent() < maxRent) {
                        sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.details.increase_rent", maxRent/ ClansModContainer.getConfig().getChargeRentDays() < upkeep ? maxRent : ClansModContainer.getConfig().getChargeRentDays() *upkeep/ ClanMembers.get(clan).getMemberCount()).setStyle(TextStyles.YELLOW));
					} else
						sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.details.reduce_upkeep").setStyle(TextStyles.YELLOW));
				}
			}
			if(rent <= 0 && upkeep <= 0 && leaders.contains(senderId))
                sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.details.breaking_even", selectedClanName).setStyle(TextStyles.GREEN));
		}
	}
}
