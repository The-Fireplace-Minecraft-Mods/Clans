package the_fireplace.clans.legacy.commands.details;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.clan.land.ClanClaims;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.raids.ClanWeaknessFactor;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.ChatUtil;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.player.InvitedPlayers;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandList extends ClanSubCommand {
    @Override
	public String getName() {
		return "list";
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
		return 2;
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.list.clans").setStyle(TextStyles.GREEN));
		if(!ClanDatabase.getClans().isEmpty()) {
			ArrayList<Clan> clans = Lists.newArrayList(ClanDatabase.getClans());
			ArrayList<ITextComponent> listItems = Lists.newArrayList();
			String sort = args.length > 0 ? args[0] : "abc";
			switch (sort) {
				case "alphabetical":
				case "abc":
				default:
					clans.sort(Comparator.comparing(clan1 -> clan1.getClanMetadata().getClanName()));
					for (Clan clan : clans)
						listItems.add(TranslationUtil.getTranslation(sender, "commands.clan.list.listitem_alphabetical", clan.getClanMetadata().getClanName(), clan.getClanMetadata().getDescription()).setStyle(TextStyles.GREEN));
					break;
				case "money":
				case "$":
					clans.sort(Comparator.comparingDouble(clan -> Economy.getBalance(clan.getClanMetadata().getClanId())));
					for (Clan clan : clans)
						listItems.add(TranslationUtil.getTranslation(sender, "commands.clan.list.listitem", Economy.getFormattedCurrency(Economy.getBalance(clan.getClanMetadata().getClanId())), clan.getClanMetadata().getClanName(), clan.getClanMetadata().getDescription()).setStyle(TextStyles.GREEN));
					break;
				case "land":
				case "claims":
					clans.sort(Comparator.comparingLong(clan2 -> ClanClaims.get().getClaimCount()));
					for (Clan clan : clans)
                        listItems.add(TranslationUtil.getTranslation(sender, "commands.clan.list.listitem", ClanClaims.get().getClaimCount(), clan.getClanMetadata().getClanName(), clan.getClanMetadata().getDescription()).setStyle(TextStyles.GREEN));
					break;
				case "members":
					clans.sort(Comparator.comparingInt(clan1 -> ClanMembers.get().getMemberCount()));
					for (Clan clan : clans)
                        listItems.add(TranslationUtil.getTranslation(sender, "commands.clan.list.listitem", ClanMembers.get().getMemberCount(), clan.getClanMetadata().getClanName(), clan.getClanMetadata().getDescription()).setStyle(TextStyles.GREEN));
					break;
				case "rewardmult":
					clans.sort(Comparator.comparingDouble(clan1 -> ClanWeaknessFactor.get().getWeaknessFactor()));
					DecimalFormat df = new DecimalFormat("#,###.00");
					for (Clan clan : clans)
                        listItems.add(TranslationUtil.getTranslation(sender, "commands.clan.list.listitem", df.format(ClanWeaknessFactor.get().getWeaknessFactor()), clan.getClanMetadata().getClanName(), clan.getClanMetadata().getDescription()).setStyle(TextStyles.GREEN));
					break;
				case "invites":
				case "invite":
				case "i":
					if(sender instanceof EntityPlayerMP)
						listInvites((EntityPlayerMP)sender, args.length == 2 ? parseInt(args[1]) : 1);
					else
						sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player").setStyle(TextStyles.RED));
					return;
			}
			int page;
			if(args.length > 1)
				page = parseInt(args[1]);
			else
				page = 1;
			ChatUtil.showPaginatedChat(sender, String.format("/clan list %s", args.length > 0 ? args[0] : "abc") + " %s", listItems, page);
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.list.noclans").setStyle(TextStyles.YELLOW));
	}

	@Override
	protected boolean allowConsoleUsage() {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		List<String> ret = Lists.newArrayList();
		if(args.length == 1)
			ret.addAll(Lists.newArrayList("money", "land", "members", "abc"));
		else if(args.length == 2)
			for(int i = 1; i < ClanDatabase.getClans().size()/ ChatUtil.RESULTS_PER_PAGE; i++)
				ret.add(String.valueOf(i));
		return getListOfStringsMatchingLastWord(args, ret);
	}

	public static void listInvites(EntityPlayerMP sender, int page) {
		if(!InvitedPlayers.getReceivedInvites(sender.getUniqueID()).isEmpty()) {
			boolean shown = false;
			List<ITextComponent> texts = Lists.newArrayList();
			for(UUID invite: InvitedPlayers.getReceivedInvites(sender.getUniqueID())) {
				Clan inviteClan = ClanDatabase.getClanById(invite);
				if(inviteClan == null) {
					InvitedPlayers.removeInvite(sender.getUniqueID(), invite);
					continue;
				}
				shown = true;
                texts.add(new TextComponentString(inviteClan.getClanMetadata().getClanName()).setStyle(new Style().setColor(inviteClan.getClanMetadata().getColorFormatting())));
			}
			ChatUtil.showPaginatedChat(sender, "/clan list invites %s", texts, page);
			//Deal with the edge case where all inviting clans have been disbanded
			if(!shown)
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.no_invites").setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.no_invites").setStyle(TextStyles.RED));
	}
}
