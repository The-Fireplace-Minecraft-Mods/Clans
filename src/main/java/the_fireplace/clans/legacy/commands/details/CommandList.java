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
import the_fireplace.clans.clan.ClanIdRegistry;
import the_fireplace.clans.clan.land.ClanClaimCount;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.metadata.ClanColors;
import the_fireplace.clans.clan.metadata.ClanDescriptions;
import the_fireplace.clans.clan.metadata.ClanNames;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandList extends ClanSubCommand
{
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
        if (!ClanIdRegistry.getIds().isEmpty()) {
            List<UUID> clans = Lists.newArrayList(ClanIdRegistry.getIds());
            List<ITextComponent> listItems = Lists.newArrayList();
            String sort = args.length > 0 ? args[0] : "abc";
            switch (sort) {
                case "alphabetical":
                case "abc":
                default:
                    clans.sort(Comparator.comparing(clan1 -> ClanNames.get(clan1).getName()));
                    for (UUID clan : clans) {
                        listItems.add(TranslationUtil.getTranslation(sender, "commands.clan.list.listitem_alphabetical", ClanNames.get(clan).getName(), ClanDescriptions.get(clan).getDescription()).setStyle(TextStyles.GREEN));
                    }
                    break;
                case "money":
                case "$":
                    clans.sort(Comparator.comparingDouble(Economy::getBalance));
                    for (UUID clan : clans) {
                        listItems.add(TranslationUtil.getTranslation(sender, "commands.clan.list.listitem", Economy.getFormattedCurrency(Economy.getBalance(clan)), ClanNames.get(clan).getName(), ClanDescriptions.get(clan).getDescription()).setStyle(TextStyles.GREEN));
                    }
                    break;
                case "land":
                case "claims":
                    clans.sort(Comparator.comparingLong(clan2 -> ClanClaimCount.get(clan2).getClaimCount()));
                    for (UUID clan : clans) {
                        listItems.add(TranslationUtil.getTranslation(sender, "commands.clan.list.listitem", ClanClaimCount.get(clan).getClaimCount(), ClanNames.get(clan).getName(), ClanDescriptions.get(clan).getDescription()).setStyle(TextStyles.GREEN));
                    }
                    break;
                case "members":
                    clans.sort(Comparator.comparingInt(clan1 -> ClanMembers.get(clan1).getMemberCount()));
                    for (UUID clan : clans) {
                        listItems.add(TranslationUtil.getTranslation(sender, "commands.clan.list.listitem", ClanMembers.get(clan).getMemberCount(), ClanNames.get(clan).getName(), ClanDescriptions.get(clan).getDescription()).setStyle(TextStyles.GREEN));
                    }
                    break;
                case "rewardmult":
                    clans.sort(Comparator.comparingDouble(clan1 -> ClanWeaknessFactor.get(clan1).getWeaknessFactor()));
                    DecimalFormat df = new DecimalFormat("#,###.00");
                    for (UUID clan : clans) {
                        listItems.add(TranslationUtil.getTranslation(sender, "commands.clan.list.listitem", df.format(ClanWeaknessFactor.get(clan).getWeaknessFactor()), ClanNames.get(clan).getName(), ClanDescriptions.get(clan).getDescription()).setStyle(TextStyles.GREEN));
                    }
                    break;
                case "invites":
                case "invite":
                case "i":
                    if (sender instanceof EntityPlayerMP) {
                        listInvites((EntityPlayerMP) sender, args.length == 2 ? parseInt(args[1]) : 1);
                    } else {
                        sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.player").setStyle(TextStyles.RED));
                    }
                    return;
            }
            int page;
            if (args.length > 1) {
                page = parseInt(args[1]);
            } else {
                page = 1;
            }
            ChatUtil.showPaginatedChat(sender, String.format("/clan list %s", args.length > 0 ? args[0] : "abc") + " %s", listItems, page);
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.list.noclans").setStyle(TextStyles.YELLOW));
        }
    }

    @Override
    protected boolean allowConsoleUsage() {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> ret = Lists.newArrayList();
        if (args.length == 1) {
            ret.addAll(Lists.newArrayList("money", "land", "members", "abc"));
        } else if (args.length == 2) {
            for (int i = 1; i < ClanIdRegistry.getIds().size() / ChatUtil.RESULTS_PER_PAGE; i++) {
                ret.add(String.valueOf(i));
            }
        }
        return getListOfStringsMatchingLastWord(args, ret);
    }

    public static void listInvites(EntityPlayerMP sender, int page) {
        if (!InvitedPlayers.getReceivedInvites(sender.getUniqueID()).isEmpty()) {
            boolean shown = false;
            List<ITextComponent> texts = Lists.newArrayList();
            for (UUID inviteClan : InvitedPlayers.getReceivedInvites(sender.getUniqueID())) {
                if (!ClanIdRegistry.isValidClan(inviteClan)) {
                    InvitedPlayers.removeInvite(sender.getUniqueID(), inviteClan);
                    continue;
                }
                shown = true;
                texts.add(new TextComponentString(ClanNames.get(inviteClan).getName()).setStyle(new Style().setColor(ClanColors.get(inviteClan).getColorFormatting())));
            }
            ChatUtil.showPaginatedChat(sender, "/clan list invites %s", texts, page);
            //Deal with the edge case where all inviting clans have been disbanded
            if (!shown) {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.no_invites").setStyle(TextStyles.RED));
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.no_invites").setStyle(TextStyles.RED));
        }
    }
}
