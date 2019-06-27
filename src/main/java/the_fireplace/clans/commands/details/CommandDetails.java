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
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
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
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.details.usage");
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
		sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.name", clan.getClanName()).setStyle(TextStyles.GREEN));
		sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.description", clan.getDescription()).setStyle(TextStyles.GREEN));
		sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.claimcount", clan.getClaimCount()).setStyle(TextStyles.GREEN));
		if(!clan.isOpclan())
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.membercount", clan.getMemberCount()).setStyle(TextStyles.GREEN));
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
		} else if(!clan.isOpclan()) {
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.details.no_members", clan.getClanName()).setStyle(TextStyles.RED));
			Clans.LOGGER.error("Clan {} has no members.", clan.getClanName());
		}
		UUID senderId = sender instanceof EntityPlayerMP ? ((EntityPlayerMP) sender).getUniqueID() : null;
		if(senderId != null && leaders.contains(senderId) || sender instanceof MinecraftServer) {
			if(Clans.cfg.chargeRentDays <= 0 && Clans.cfg.clanUpkeepDays <= 0)
				throw new CommandException(TranslationUtil.getRawTranslationString(sender, "commands.clan.finances.disabled"));
			long upkeep = 0;
			long rent = 0;
			if(Clans.cfg.clanUpkeepDays > 0) {
				upkeep += Clans.cfg.clanUpkeepCost;
				if(Clans.cfg.multiplyUpkeepClaims)
					upkeep *= selectedClan.getClaimCount();
				if(Clans.cfg.multiplyUpkeepMembers)
					upkeep *= selectedClan.getMemberCount();
				if(upkeep > 0)
					sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.finances.upkeep", upkeep, Clans.getPaymentHandler().getCurrencyName(upkeep), Clans.cfg.clanUpkeepDays).setStyle(TextStyles.GREEN));
			}
			if(Clans.cfg.chargeRentDays > 0) {
				rent += selectedClan.getRent();
				rent *= selectedClan.getMemberCount();
				if(rent > 0)
					sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.finances.rent", rent, Clans.getPaymentHandler().getCurrencyName(rent), Clans.cfg.chargeRentDays).setStyle(TextStyles.GREEN));
			}
			if(upkeep > 0 && rent > 0) {
				upkeep /= Clans.cfg.clanUpkeepDays;
				rent /= Clans.cfg.chargeRentDays;
				sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.finances.trend", (rent-upkeep) <= 0 ? (rent-upkeep) : '+'+(rent-upkeep), Clans.getPaymentHandler().getCurrencyName(rent-upkeep)).setStyle(rent >= upkeep ? TextStyles.GREEN : TextStyles.YELLOW));
				if(upkeep > rent) {
					long maxRent = Clans.cfg.maxRent;
					if(Clans.cfg.multiplyMaxRentClaims)
						maxRent *= selectedClan.getClaimCount();
					if(selectedClan.getRent() < maxRent) {
						sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.finances.increase_rent", maxRent/Clans.cfg.chargeRentDays < upkeep ? maxRent : Clans.cfg.chargeRentDays*upkeep/selectedClan.getMemberCount()).setStyle(TextStyles.YELLOW));
					} else
						sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.finances.reduce_upkeep").setStyle(TextStyles.YELLOW));
				}
			}
			if(rent <= 0 && upkeep <= 0)
				sender.sendMessage(TranslationUtil.getTranslation(senderId, "commands.clan.finances.breaking_even", selectedClan.getClanName()).setStyle(TextStyles.GREEN));
		}
	}
}
