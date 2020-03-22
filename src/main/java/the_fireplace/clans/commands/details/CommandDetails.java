package the_fireplace.clans.commands.details;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.ClansHelper;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.FormulaParser;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

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
			Clan targetClan = ClanCache.getClanByName(args[0]);
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
		return args.length == 1 ? Lists.newArrayList(ClanCache.getClanNames().keySet()) : Collections.emptyList();
	}

	@SuppressWarnings("ConstantConditions")
	private void showDetails(MinecraftServer server, ICommandSender sender, Clan clan) throws CommandException {
		sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.name", clan.getName()).setStyle(TextStyles.GREEN));
		sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.desc", clan.getDescription()).setStyle(TextStyles.GREEN));
		sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.claimcount", clan.getClaimCount()).setStyle(TextStyles.GREEN));
		sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.membercount", clan.getMemberCount()).setStyle(TextStyles.GREEN));
		if(ClansHelper.getConfig().isIncreasingRewards() && !clan.isServer())
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.multiplier", clan.getRaidRewardMultiplier()).setStyle(TextStyles.GREEN));
		if(clan.isServer())
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.server").setStyle(TextStyles.GREEN));
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
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.members").setStyle(TextStyles.GREEN));
			for(UUID leader: leaders) {
				GameProfile l = server.getPlayerProfileCache().getProfileByUUID(leader);
				if(l != null)
					sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.leader", l.getName()).setStyle(server.getPlayerList().getPlayerByUUID(leader) != null ? TextStyles.ONLINE_LEADER : TextStyles.OFFLINE_LEADER));
			}
			for(UUID admin: admins) {
				GameProfile a = server.getPlayerProfileCache().getProfileByUUID(admin);
				if(a != null)
					sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.admin", a.getName()).setStyle(server.getPlayerList().getPlayerByUUID(admin) != null ? TextStyles.ONLINE_ADMIN : TextStyles.OFFLINE_ADMIN));
			}
			for(UUID member: members) {
				GameProfile m = server.getPlayerProfileCache().getProfileByUUID(member);
				if(m != null)
					sender.sendMessage(new TextComponentString(m.getName()).setStyle(server.getPlayerList().getPlayerByUUID(member) != null ? TextStyles.GREEN : TextStyles.YELLOW));
			}
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.no_members", clan.getName()).setStyle(TextStyles.RED));
		UUID senderId = sender instanceof EntityPlayerMP ? ((EntityPlayerMP) sender).getUniqueID() : null;
		if((senderId != null && members.contains(senderId) || sender instanceof MinecraftServer) && !clan.isServer()) {
			if(ClansHelper.getConfig().getChargeRentDays() <= 0 && ClansHelper.getConfig().getClanUpkeepDays() <= 0)
				throw new CommandException(TranslationUtil.getRawTranslationString(sender, "commands.clan.details.disabled"));
			long upkeep = 0;
			long rent = 0;
			if(ClansHelper.getConfig().getClanUpkeepDays() > 0) {
				upkeep += FormulaParser.eval(ClansHelper.getConfig().getClanUpkeepCostFormula(), clan, 0);
				String upkeepStr = TranslationUtil.getStringTranslation("commands.clan.details.upkeep_formula", FormulaParser.getFilteredFormula(ClansHelper.getConfig().getClanUpkeepCostFormula(), clan), ClansHelper.getPaymentHandler().getCurrencyString(upkeep));
				if(upkeep > 0) {
					sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.details.upkeep", upkeepStr, ClansHelper.getConfig().getClanUpkeepDays()).setStyle(TextStyles.GREEN));
					sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.details.upkeepdue", (selectedClan.getNextUpkeepTimestamp()-System.currentTimeMillis())/1000/60/60).setStyle(TextStyles.GREEN));
				}
			}
			if(ClansHelper.getConfig().getChargeRentDays() > 0) {
				rent += selectedClan.getRent();
				if(rent > 0) {
					sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.details.rent", ClansHelper.getPaymentHandler().getCurrencyString(rent*selectedClan.getMemberCount()), ClansHelper.getConfig().getChargeRentDays()).setStyle(TextStyles.GREEN));
                    sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.details.rent_individual", ClansHelper.getPaymentHandler().getCurrencyString(rent), ClansHelper.getConfig().getChargeRentDays()).setStyle(TextStyles.GREEN));
					sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.details.rentdue", (selectedClan.getNextRentTimestamp()-System.currentTimeMillis())/1000/60/60).setStyle(TextStyles.GREEN));
				}
			}
			if(upkeep > 0 && rent > 0 && leaders.contains(senderId)) {
				upkeep /= ClansHelper.getConfig().getClanUpkeepDays();
				rent /= ClansHelper.getConfig().getChargeRentDays();
				sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.details.trend", (rent-upkeep) <= 0 ? ClansHelper.getPaymentHandler().getCurrencyString(rent-upkeep) : '+'+ ClansHelper.getPaymentHandler().getCurrencyString(rent-upkeep)).setStyle(rent >= upkeep ? TextStyles.GREEN : TextStyles.YELLOW));
				if(upkeep > rent) {
					long maxRent = (long)FormulaParser.eval(ClansHelper.getConfig().getMaxRentFormula(), clan, 0);
					if(selectedClan.getRent() < maxRent) {
						sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.details.increase_rent", maxRent/ ClansHelper.getConfig().getChargeRentDays() < upkeep ? maxRent : ClansHelper.getConfig().getChargeRentDays() *upkeep/selectedClan.getMemberCount()).setStyle(TextStyles.YELLOW));
					} else
						sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.details.reduce_upkeep").setStyle(TextStyles.YELLOW));
				}
			}
			if(rent <= 0 && upkeep <= 0 && leaders.contains(senderId))
				sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.details.breaking_even", selectedClan.getName()).setStyle(TextStyles.GREEN));
		}
	}
}
