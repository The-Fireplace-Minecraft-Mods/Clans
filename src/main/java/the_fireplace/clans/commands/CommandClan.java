package the_fireplace.clans.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.event.Timer;
import the_fireplace.clans.raid.RaidingParties;
import the_fireplace.clans.util.CapHelper;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.Pair;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandClan {

    public static final SuggestionProvider<CommandSource> playerClanSuggestion = (context, builder) -> {
        for(Clan c: ClanCache.getPlayerClans(context.getSource().asPlayer().getUniqueID()))
            builder.suggest(c.getClanName());
        return builder.buildFuture();
    };

    public static final SuggestionProvider<CommandSource> clanSuggestion = (context, builder) -> {
        for(Clan c: ClanDatabase.getClans())
            builder.suggest(c.getClanName());
        return builder.buildFuture();
    };

    public static final SuggestionProvider<CommandSource> invitablePlayerSuggestion = (context, builder) -> {
        for(EntityPlayerMP p: Clans.minecraftServer.getPlayerList().getPlayers())
            if(Clans.cfg.allowMultiClanMembership || ClanCache.getPlayerClans(p.getUniqueID()).isEmpty())
                builder.suggest(p.getName().getFormattedText());
        return builder.buildFuture();
    };

    @SuppressWarnings("Duplicates")
    public static void register(CommandDispatcher<CommandSource> commandDispatcher) {
        LiteralArgumentBuilder<CommandSource> clanCommand = Commands.literal("clan").requires((iCommandSender) -> iCommandSender.getEntity() instanceof EntityPlayerMP);
        LiteralArgumentBuilder<CommandSource> clanCommandWithClan = clanCommand.then(Commands.argument("clan", StringArgumentType.word()).suggests(playerClanSuggestion));

        clanCommand.then(Commands.literal("banner")
                .executes(context -> runBannerCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommand.then(Commands.literal("b")
                .executes(context -> runBannerCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommandWithClan.then(Commands.literal("banner")
                .executes(context -> runBannerCommand(context, ClanCache.getClan(context.getArgument("clan", String.class)))));
        clanCommandWithClan.then(Commands.literal("b")
                .executes(context -> runBannerCommand(context, ClanCache.getClan(context.getArgument("clan", String.class)))));

        clanCommand.then(Commands.literal("details")
                .executes(context -> runDetailsCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))
                .then(Commands.argument("clan", StringArgumentType.word()).suggests(clanSuggestion)
                .executes(context -> runDetailsCommand(context, ClanCache.getClan(context.getArgument("clan", String.class))))));
        clanCommand.then(Commands.literal("info")
                .executes(context -> runDetailsCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))
                .then(Commands.argument("clan", StringArgumentType.word()).suggests(clanSuggestion)
                .executes(context -> runDetailsCommand(context, ClanCache.getClan(context.getArgument("clan", String.class))))));
        clanCommand.then(Commands.literal("d")
                .executes(context -> runDetailsCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))
                .then(Commands.argument("clan", StringArgumentType.word()).suggests(clanSuggestion)
                .executes(context -> runDetailsCommand(context, ClanCache.getClan(context.getArgument("clan", String.class))))));
        clanCommandWithClan.then(Commands.literal("details")
                .executes(context -> runDetailsCommand(context, ClanCache.getClan(context.getArgument("clan", String.class)))));
        clanCommandWithClan.then(Commands.literal("info")
                .executes(context -> runDetailsCommand(context, ClanCache.getClan(context.getArgument("clan", String.class)))));
        clanCommandWithClan.then(Commands.literal("d")
                .executes(context -> runDetailsCommand(context, ClanCache.getClan(context.getArgument("clan", String.class)))));

        clanCommand.then(Commands.literal("disband")
                .executes(context -> runDisbandCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()), false)));
        clanCommandWithClan.then(Commands.literal("disband")
                .executes(context -> runDisbandCommand(context, ClanCache.getClan(context.getArgument("clan", String.class)), false)));

        clanCommand.then(Commands.literal("form")
                .then(Commands.argument("name", StringArgumentType.word())
                .executes(context -> runFormCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()), null))
                .then(Commands.argument("banner", StringArgumentType.greedyString())
                .executes(context -> runFormCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()), context.getArgument("banner", String.class))))));
        clanCommand.then(Commands.literal("create")
                .then(Commands.argument("name", StringArgumentType.word())
                .executes(context -> runFormCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()), null))
                .then(Commands.argument("banner", StringArgumentType.greedyString())
                .executes(context -> runFormCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()), context.getArgument("banner", String.class))))));

        clanCommand.then(Commands.literal("map")
                .executes(CommandClan::runMapCommand));
        clanCommand.then(Commands.literal("m")
                .executes(CommandClan::runMapCommand));

        clanCommand.then(Commands.literal("setbanner")
                .executes(context -> runSetBannerCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()), null))
                .then(Commands.argument("banner", StringArgumentType.greedyString())
                .executes(context -> runSetBannerCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()), context.getArgument("banner", String.class)))));
        clanCommandWithClan.then(Commands.literal("setbanner")
                .executes(context -> runSetBannerCommand(context, ClanCache.getClan(context.getArgument("clan", String.class)), null))
                .then(Commands.argument("banner", StringArgumentType.greedyString())
                .executes(context -> runSetBannerCommand(context, ClanCache.getClan(context.getArgument("clan", String.class)), context.getArgument("banner", String.class)))));

        clanCommand.then(Commands.literal("setdefault")
                .then(Commands.argument("clan", StringArgumentType.word())
                .executes(context -> runSetDefaultCommand(context, ClanCache.getClan(context.getArgument("clan", String.class))))));

        clanCommand.then(Commands.literal("setname")
                .then(Commands.argument("name", StringArgumentType.word())
                .executes(context -> runSetNameCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommandWithClan.then(Commands.literal("setname")
                .then(Commands.argument("name", StringArgumentType.word())
                .executes(context -> runSetNameCommand(context, ClanCache.getClan(context.getArgument("clan", String.class))))));

        clanCommand.then(Commands.literal("setdescription")
                .then(Commands.argument("description", StringArgumentType.greedyString())
                .executes(context -> runSetDescriptionCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommand.then(Commands.literal("setdesc")
                .then(Commands.argument("description", StringArgumentType.greedyString())
                .executes(context -> runSetDescriptionCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommandWithClan.then(Commands.literal("setdescription")
                .then(Commands.argument("description", StringArgumentType.greedyString())
                .executes(context -> runSetDescriptionCommand(context, ClanCache.getClan(context.getArgument("clan", String.class))))));
        clanCommandWithClan.then(Commands.literal("setdesc")
                .then(Commands.argument("description", StringArgumentType.greedyString())
                .executes(context -> runSetDescriptionCommand(context, ClanCache.getClan(context.getArgument("clan", String.class))))));

        clanCommand.then(Commands.literal("addfunds")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .executes(context -> runAddFundsCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommand.then(Commands.literal("deposit")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .executes(context -> runAddFundsCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommand.then(Commands.literal("af")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .executes(context -> runAddFundsCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommandWithClan.then(Commands.literal("addfunds")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .executes(context -> runAddFundsCommand(context, ClanCache.getClan(context.getArgument("clan", String.class))))));
        clanCommandWithClan.then(Commands.literal("deposit")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .executes(context -> runAddFundsCommand(context, ClanCache.getClan(context.getArgument("clan", String.class))))));
        clanCommandWithClan.then(Commands.literal("af")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .executes(context -> runAddFundsCommand(context, ClanCache.getClan(context.getArgument("clan", String.class))))));

        clanCommand.then(Commands.literal("balance")
                .executes(context -> runBalanceCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommandWithClan.then(Commands.literal("balance")
                .executes(context -> runBalanceCommand(context, ClanCache.getClan(context.getArgument("clan", String.class)))));

        clanCommand.then(Commands.literal("claim")
                .executes(context -> runClaimCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommand.then(Commands.literal("c")
                .executes(context -> runClaimCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommandWithClan.then(Commands.literal("claim")
                .executes(context -> runClaimCommand(context, ClanCache.getClan(context.getArgument("clan", String.class)))));
        clanCommandWithClan.then(Commands.literal("c")
                .executes(context -> runClaimCommand(context, ClanCache.getClan(context.getArgument("clan", String.class)))));

        clanCommand.then(Commands.literal("abandonclaim")
                .executes(context -> runAbandonClaimCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommand.then(Commands.literal("ac")
                .executes(context -> runAbandonClaimCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommandWithClan.then(Commands.literal("abandonclaim")
                .executes(context -> runAbandonClaimCommand(context, ClanCache.getClan(context.getArgument("clan", String.class)))));
        clanCommandWithClan.then(Commands.literal("ac")
                .executes(context -> runAbandonClaimCommand(context, ClanCache.getClan(context.getArgument("clan", String.class)))));

        clanCommand.then(Commands.literal("accept")
                .executes(context -> runAcceptCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));

        clanCommand.then(Commands.literal("decline")
                .executes(context -> runDeclineCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));

        clanCommand.then(Commands.literal("promote")
                .then(Commands.argument("target", StringArgumentType.word())
                .executes(context -> runPromoteCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommandWithClan.then(Commands.literal("promote")
                .then(Commands.argument("target", StringArgumentType.word())
                .executes(context -> runPromoteCommand(context, ClanCache.getClan(context.getArgument("clan", String.class))))));

        clanCommand.then(Commands.literal("demote")
                .then(Commands.argument("target", StringArgumentType.word())
                .executes(context -> runDemoteCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommandWithClan.then(Commands.literal("demote")
                .then(Commands.argument("target", StringArgumentType.word())
                .executes(context -> runDemoteCommand(context, ClanCache.getClan(context.getArgument("clan", String.class))))));

        clanCommand.then(Commands.literal("invite")
                .then(Commands.argument("target", StringArgumentType.word()).suggests(invitablePlayerSuggestion)
                .executes(context -> runInviteCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommand.then(Commands.literal("i")
                .then(Commands.argument("target", StringArgumentType.word()).suggests(invitablePlayerSuggestion)
                .executes(context -> runInviteCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommandWithClan.then(Commands.literal("invite")
                .then(Commands.argument("target", StringArgumentType.word()).suggests(invitablePlayerSuggestion)
                .executes(context -> runInviteCommand(context, ClanCache.getClan(context.getArgument("clan", String.class))))));
        clanCommandWithClan.then(Commands.literal("i")
                .then(Commands.argument("target", StringArgumentType.word()).suggests(invitablePlayerSuggestion)
                .executes(context -> runInviteCommand(context, ClanCache.getClan(context.getArgument("clan", String.class))))));

        clanCommand.then(Commands.literal("kick")
                .then(Commands.argument("target", StringArgumentType.word())
                .executes(context -> runKickCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommandWithClan.then(Commands.literal("kick")
                .then(Commands.argument("target", StringArgumentType.word())
                .executes(context -> runKickCommand(context, ClanCache.getClan(context.getArgument("clan", String.class))))));

        clanCommand.then(Commands.literal("leave")
                .executes(context -> runLeaveCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommandWithClan.then(Commands.literal("leave")
                .executes(context -> runLeaveCommand(context, ClanCache.getClan(context.getArgument("clan", String.class)))));

        clanCommand.then(Commands.literal("home")
                .executes(context -> runHomeCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommand.then(Commands.literal("h")
                .executes(context -> runHomeCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommandWithClan.then(Commands.literal("home")
                .executes(context -> runHomeCommand(context, ClanCache.getClan(context.getArgument("clan", String.class)))));
        clanCommandWithClan.then(Commands.literal("h")
                .executes(context -> runHomeCommand(context, ClanCache.getClan(context.getArgument("clan", String.class)))));

        clanCommand.then(Commands.literal("sethome")
                .executes(context -> runSetHomeCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommandWithClan.then(Commands.literal("sethome")
                .executes(context -> runSetHomeCommand(context, ClanCache.getClan(context.getArgument("clan", String.class)))));

        clanCommand.then(Commands.literal("trapped")
                .executes(context -> runTrappedCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommand.then(Commands.literal("t")
                .executes(context -> runTrappedCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));

        if(Clans.cfg.leaderWithdrawFunds) {
            clanCommand.then(Commands.literal("takefunds")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .executes(context -> runTakeFundsCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
            clanCommand.then(Commands.literal("withdraw")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .executes(context -> runTakeFundsCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
            clanCommandWithClan.then(Commands.literal("takefunds")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .executes(context -> runTakeFundsCommand(context, ClanCache.getClan(context.getArgument("clan", String.class))))));
            clanCommandWithClan.then(Commands.literal("withdraw")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .executes(context -> runTakeFundsCommand(context, ClanCache.getClan(context.getArgument("clan", String.class))))));
        }

        if(Clans.cfg.chargeRentDays > 0) {
            clanCommand.then(Commands.literal("setrent")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .executes(context -> runSetRentCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
            clanCommandWithClan.then(Commands.literal("setrent")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .executes(context -> runSetRentCommand(context, ClanCache.getClan(context.getArgument("clan", String.class))))));
        }

        if(Clans.cfg.chargeRentDays > 0 || Clans.cfg.clanUpkeepDays > 0) {
            clanCommand.then(Commands.literal("finances")
                    .executes(context -> runFinancesCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
            clanCommandWithClan.then(Commands.literal("finances")
                    .executes(context -> runFinancesCommand(context, ClanCache.getClan(context.getArgument("clan", String.class)))));
        }

        LiteralCommandNode<CommandSource> clanNode = commandDispatcher.register(clanCommand);
        commandDispatcher.register(Commands.literal("c").redirect(clanNode));
    }

    private static int runBannerCommand(CommandContext<CommandSource> context, @Nullable Clan selectedClan) throws CommandSyntaxException {
        if(!validateClanRank(context, selectedClan, EnumRank.MEMBER))
            return 0;
        assert selectedClan != null;
        NBTTagCompound banner;
        try{
            banner = JsonToNBT.getTagFromJson(selectedClan.getClanBanner());
        } catch(CommandSyntaxException e){
            throwCommandFailure("%s does not have a banner.", selectedClan.getClanName());
            return 1;
        }
        if(context.getSource().asPlayer().getHeldItemMainhand().getItem() instanceof ItemBanner) {
            int count = context.getSource().asPlayer().getHeldItemMainhand().getCount();
            ItemStack bannerStack = ItemStack.read(banner);
            bannerStack.setCount(count);
            context.getSource().asPlayer().setHeldItem(EnumHand.MAIN_HAND, bannerStack);
        } else if(context.getSource().asPlayer().getHeldItemOffhand().getItem() instanceof ItemBanner) {
            int count = context.getSource().asPlayer().getHeldItemOffhand().getCount();
            ItemStack bannerStack = ItemStack.read(banner);
            bannerStack.setCount(count);
            context.getSource().asPlayer().setHeldItem(EnumHand.OFF_HAND, bannerStack);
        } else
            throwCommandFailure("You are not holding a banner!");
        return 1;
    }

    private static int runDetailsCommand(CommandContext<CommandSource> context, @Nullable Clan clan) {
        if(!validateClanRank(context, clan, EnumRank.ANY))
            return 0;
        assert clan != null;
        sendFeedback(context, TextStyles.GREEN, "Clan name: %s", clan.getClanName());
        sendFeedback(context, TextStyles.GREEN, "Clan description: %s", clan.getDescription());
        sendFeedback(context, TextStyles.GREEN, "Number of claims: %s", clan.getClaimCount());
        sendFeedback(context, TextStyles.GREEN, "Number of members: %s", clan.getMemberCount());
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
            sendFeedback(context, TextStyles.GREEN, "Online members:");
            for(EntityPlayerMP leader: leaders)
                sendFeedback(context, TextStyles.BOLD_ITALIC.setColor(TextFormatting.GREEN), "Leader %s", leader.getName().getFormattedText());
            for(EntityPlayerMP admin: admins)
                sendFeedback(context, TextStyles.BOLD.setColor(TextFormatting.GREEN), "Admin %s", admin.getName().getFormattedText());
            for(EntityPlayerMP member: members)
                sendFeedback(context, TextStyles.GREEN, "Member %s", member.getName().getFormattedText());
        } else
            sendFeedback(context, TextStyles.GREEN, "No online members.");
        return 1;
    }

    static int runDisbandCommand(CommandContext<CommandSource> context, @Nullable Clan clan, boolean isOpclanCommand) throws CommandSyntaxException {
        if (!isOpclanCommand && !validateClanRank(context, clan, EnumRank.LEADER))
            return 0;
        assert clan != null;
        if(!clan.isOpclan()) {
            if (ClanDatabase.removeClan(clan.getClanId())) {
                long distFunds = Clans.getPaymentHandler().getBalance(clan.getClanId());
                distFunds += Clans.cfg.claimChunkCost * clan.getClaimCount();
                if (Clans.cfg.leaderRecieveDisbandFunds) {
                    clan.payLeaders(distFunds);
                    distFunds = 0;
                } else {
                    clan.payLeaders(distFunds % clan.getMemberCount());
                    distFunds /= clan.getMemberCount();
                }
                for (UUID member : clan.getMembers().keySet()) {
                    Clans.getPaymentHandler().ensureAccountExists(member);
                    if (!Clans.getPaymentHandler().addAmount(distFunds, member))
                        clan.payLeaders(distFunds);
                    EntityPlayerMP player;
                    try {
                        player = Clans.minecraftServer.getPlayerList().getPlayerByUUID(member);
                    } catch (CommandException e) {
                        player = null;
                    }
                    if (player != null) {
                        updateDefaultClan(player, clan);
                        if (!player.getUniqueID().equals(context.getSource().asPlayer().getUniqueID()))
                            sendFeedback(context, TextStyles.GREEN, "Your clan has been disbanded by %s.", context.getSource().getName());
                    }
                }
                Clans.getPaymentHandler().deductAmount(Clans.getPaymentHandler().getBalance(clan.getClanId()), clan.getClanId());
                sendFeedback(context, TextStyles.GREEN, "You have disbanded %s.", clan.getClanName());
            } else
                throwCommandFailure("Internal error: Unable to disband %s.", clan.getClanName());
        } else
            throwCommandFailure("You cannot disband %s because it is the opclan.", clan.getClanName());
        return 1;
    }

    private static int runFormCommand(CommandContext<CommandSource> context, @Nullable Clan selectedClan, @Nullable String banner) throws CommandSyntaxException {
        if(!validateClanRank(context, selectedClan, Clans.cfg.allowMultiClanMembership ? EnumRank.ANY : EnumRank.NOCLAN))
            return 0;
        String newClanName = context.getArgument("name", String.class);
        if (Clans.cfg.maxNameLength > 0 && newClanName.length() > Clans.cfg.maxNameLength)
            throwCommandFailure("The clan name you have specified is too long. This server's maximum name length is %s characters.", Clans.cfg.maxNameLength);
        else if (ClanCache.clanNameTaken(newClanName))
            throwCommandFailure("The clan name \"%s\" is already taken or invalid.", newClanName);
        else {
            if (banner != null) {
                try {
                    if(ItemStack.read(JsonToNBT.getTagFromJson(banner)).isEmpty()) {
                        throwCommandFailure("The clan banner you have specified is invalid.");
                        return 0;
                    }
                    if (ClanCache.clanBannerTaken(banner)) {
                        throwCommandFailure("The clan banner you have specified is already taken.");
                        return 0;
                    }
                } catch (CommandSyntaxException e) {
                    throwCommandFailure("The clan banner you have specified is invalid.");
                    return 0;
                }
            }
            if (Clans.getPaymentHandler().deductAmount(Clans.cfg.formClanCost, context.getSource().asPlayer().getUniqueID())) {
                Clan c = new Clan(newClanName, context.getSource().asPlayer().getUniqueID(), banner);
                if(ClanCache.getPlayerClans(context.getSource().asPlayer().getUniqueID()).size() == 1)
                    CapHelper.getPlayerClanCapability(context.getSource().asPlayer()).setDefaultClan(c.getClanId());
                sendFeedback(context, TextStyles.GREEN, "Clan formed!");
            } else
                throwCommandFailure("Insufficient funds to form clan. It costs %s %s.", Clans.cfg.formClanCost, Clans.getPaymentHandler().getCurrencyName(Clans.cfg.formClanCost));
        }
        return 1;
    }

    private static final char[] mapchars = {'%', '&', '@', '*', '+', '<', '>', '~', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7', '8', '9', 'w', 'm'};
    private static int runMapCommand(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if(!validateClanRank(context, null, EnumRank.ANY))
            return 0;
        World w = context.getSource().asPlayer().getEntityWorld();
        Chunk center = w.getChunk(context.getSource().asPlayer().getPosition());

        Map<UUID, Character> symbolMap = Maps.newHashMap();
        sendFeedback(context, TextStyles.GREEN, "=====================================================");
        for(int z=center.z-5; z <= center.z + 5; z++) {
            StringBuilder row = new StringBuilder();
            for (int x = center.x - 26; x <= center.x + 26; x++) {
                Chunk c = w.getChunk(x, z);
                UUID chunkOwner = ChunkUtils.getChunkOwner(c);
                if(chunkOwner == null)
                    row.append('#');
                else {
                    if(ClanCache.getClan(chunkOwner) == null) {
                        ChunkUtils.clearChunkOwner(c);
                        row.append('#');
                    } else {
                        if (!symbolMap.containsKey(chunkOwner))
                            symbolMap.put(chunkOwner, mapchars[symbolMap.size() % mapchars.length]);
                        row.append(symbolMap.get(chunkOwner));
                    }
                }
            }
            sendFeedback(context, TextStyles.GREEN, row.toString());
        }
        sendFeedback(context, TextStyles.GREEN, "=====================================================");
        for(Map.Entry<UUID, Character> symbol: symbolMap.entrySet()) {
            Clan c = ClanCache.getClan(symbol.getKey());
            sendFeedback(context, TextStyles.GREEN, "%s: %s", symbol.getValue(), c != null ? c.getClanName() : "Wilderness");
        }
        return 1;
    }

    private static int runSetBannerCommand(CommandContext<CommandSource> context, @Nullable Clan clan, @Nullable String banner) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, EnumRank.LEADER))
            return 0;
        assert clan != null;
        if(banner != null){
            try {
                if(ItemStack.read(JsonToNBT.getTagFromJson(banner)).isEmpty()) {
                    throwCommandFailure("The clan banner you have specified is invalid.");
                    return 0;
                }
                if(ClanCache.clanBannerTaken(banner)) {
                    throwCommandFailure("The clan banner you have specified is already taken.");
                    return 0;
                } else {
                    clan.setClanBanner(banner);
                    sendFeedback(context, TextStyles.GREEN, "Clan banner set!");
                }
            } catch(CommandSyntaxException e){
                throwCommandFailure("The clan banner you have specified is invalid.");
                return 0;
            }
        } else if(context.getSource().asPlayer().getHeldItemMainhand().getItem() instanceof ItemBanner) {
            NBTTagCompound tags = context.getSource().asPlayer().getHeldItemMainhand().serializeNBT();
            setClanBannerFromItem(context, clan, tags);
        } else if(context.getSource().asPlayer().getHeldItemOffhand().getItem() instanceof ItemBanner) {
            NBTTagCompound tags = context.getSource().asPlayer().getHeldItemOffhand().serializeNBT();
            setClanBannerFromItem(context, clan, tags);
        } else
            throwCommandFailure("You are not holding a banner!");
        return 1;
    }

    private static void setClanBannerFromItem(CommandContext<CommandSource> context, Clan playerClan, @Nullable NBTTagCompound tags) {
        String banner = tags != null ? tags.toString() : "";
        if(ClanCache.clanBannerTaken(banner))
            throwCommandFailure("The clan banner you have specified is already taken.");
        else {
            playerClan.setClanBanner(banner);
            sendFeedback(context, TextStyles.GREEN, "Clan banner set!");
        }
    }

    private static int runSetDefaultCommand(CommandContext<CommandSource> context, @Nullable Clan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, EnumRank.MEMBER))
            return 0;
        assert clan != null;
        CapHelper.getPlayerClanCapability(context.getSource().asPlayer()).setDefaultClan(clan.getClanId());
        return 1;
    }

    private static int runSetDescriptionCommand(CommandContext<CommandSource> context, @Nullable Clan clan) {
        if(!validateClanRank(context, clan, EnumRank.LEADER))
            return 0;
        assert clan != null;
        clan.setDescription(context.getArgument("description", String.class));
        sendFeedback(context, TextStyles.GREEN, "Clan description for %s set!", clan.getClanName());
        return 1;
    }

    private static int runSetNameCommand(CommandContext<CommandSource> context, @Nullable Clan clan) {
        if(!validateClanRank(context, clan, EnumRank.LEADER))
            return 0;
        assert clan != null;
        String newName = context.getArgument("name", String.class);
        if(Clans.cfg.maxNameLength > 0 && newName.length() > Clans.cfg.maxNameLength)
            throwCommandFailure("The clan name you have specified is too long. This server's maximum clan name length is %s.", Clans.cfg.maxNameLength);
        else if(!ClanCache.clanNameTaken(newName)) {
            String oldName = clan.getClanName();
            clan.setClanName(newName);
            sendFeedback(context, TextStyles.GREEN, "You have renamed %s to %s!", oldName, newName);
        } else
            throwCommandFailure("The clan name \"%s\" is already taken or invalid.", newName);
        return 1;
    }

    private static int runAddFundsCommand(CommandContext<CommandSource> context, @Nullable Clan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, EnumRank.MEMBER))
            return 0;
        assert clan != null;
        long amount = context.getArgument("amount", Integer.class);
        if(Clans.getPaymentHandler().deductAmount(amount, context.getSource().asPlayer().getUniqueID())) {
            if(Clans.getPaymentHandler().addAmount(amount, clan.getClanId()))
                sendFeedback(context, TextStyles.GREEN, "Successfully added %s %s to %s's balance.", amount, Clans.getPaymentHandler().getCurrencyName(amount), clan.getClanName());
            else {
                Clans.getPaymentHandler().addAmount(amount, context.getSource().asPlayer().getUniqueID());
                throwCommandFailure("Internal error: Clan account not found for %s.", clan.getClanName());
            }
        } else
            throwCommandFailure("You do not have enough funds to do this.");
        return 1;
    }

    private static int runBalanceCommand(CommandContext<CommandSource> context, @Nullable Clan clan) {
        if(!validateClanRank(context, clan, EnumRank.MEMBER))
            return 0;
        assert clan != null;
        long balance = Clans.getPaymentHandler().getBalance(clan.getClanId());
        sendFeedback(context, TextStyles.GREEN, "Clan balance: %s %s", balance, Clans.getPaymentHandler().getCurrencyName(balance));
        return 1;
    }

    private static int runFinancesCommand(CommandContext<CommandSource> context, @Nullable Clan clan) {
        if(!validateClanRank(context, clan, EnumRank.LEADER))
            return 0;
        assert clan != null;
        long upkeep = 0;
        long rent = 0;
        if(Clans.cfg.clanUpkeepDays > 0) {
            upkeep += Clans.cfg.clanUpkeepCost;
            if(Clans.cfg.multiplyUpkeepClaims)
                upkeep *= clan.getClaimCount();
            if(Clans.cfg.multiplyUpkeepMembers)
                upkeep *= clan.getMemberCount();
            if(upkeep > 0)
                sendFeedback(context, TextStyles.GREEN, "Upkeep (expenses) is %s %s every %s days.", upkeep, Clans.getPaymentHandler().getCurrencyName(upkeep), Clans.cfg.clanUpkeepDays);
        }
        if(Clans.cfg.chargeRentDays > 0) {
            rent += clan.getRent();
            rent *= clan.getMemberCount();
            if(rent > 0)
                sendFeedback(context, TextStyles.GREEN, "Rent (income) is %s %s every %s days.", rent, Clans.getPaymentHandler().getCurrencyName(rent), Clans.cfg.chargeRentDays);
        }
        if(upkeep > 0 && rent > 0) {
            upkeep /= Clans.cfg.clanUpkeepDays;
            rent /= Clans.cfg.chargeRentDays;
            sendFeedback(context, rent >= upkeep ? TextStyles.GREEN : TextStyles.YELLOW, "Approximate financial balance is %s %s each day.", rent-upkeep, Clans.getPaymentHandler().getCurrencyName(rent-upkeep));
            if(upkeep > rent) {
                long maxRent = Clans.cfg.maxRent;
                if(Clans.cfg.multiplyMaxRentClaims)
                    maxRent *= clan.getClaimCount();
                if(clan.getRent() < maxRent) {
                    if(maxRent/Clans.cfg.chargeRentDays < upkeep)
                        sendFeedback(context, TextStyles.YELLOW, "You may want to increase rent to %s and/or find a way to reduce upkeep.", maxRent);
                    else
                        sendFeedback(context, TextStyles.YELLOW, "You may want to increase rent to %s and/or find a way to reduce upkeep.", Clans.cfg.chargeRentDays*upkeep/clan.getMemberCount());
                } else
                    sendFeedback(context, TextStyles.YELLOW, "You may want to find a way to reduce upkeep.");
            }
        }
        return 1;
    }

    private static int runSetRentCommand(CommandContext<CommandSource> context, @Nullable Clan clan) {
        if(!validateClanRank(context, clan, EnumRank.LEADER))
            return 0;
        assert clan != null;
        long newRent = context.getArgument("amount", Integer.class);
        if(newRent >= 0) {
            long maxRent = Clans.cfg.maxRent;
            if(Clans.cfg.multiplyMaxRentClaims)
                maxRent *= clan.getClaimCount();
            if(maxRent <= 0 || newRent <= maxRent) {
                clan.setRent(newRent);
                sendFeedback(context, TextStyles.GREEN, "Clan rent for %s set to %s!", clan.getClanName(), clan.getRent());
            } else
                throwCommandFailure("Cannot set rent above your maximum(%s %s)!", maxRent, Clans.getPaymentHandler().getCurrencyName(maxRent));
        } else
            throwCommandFailure("Cannot set negative rent!");
        return 1;
    }

    private static int runTakeFundsCommand(CommandContext<CommandSource> context, @Nullable Clan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, EnumRank.LEADER))
            return 0;
        assert clan != null;
        if(!Clans.cfg.leaderWithdrawFunds)
            throwCommandFailure("/clan takefunds is disabled on this server.");
        long amount = context.getArgument("amount", Integer.class);
        if(Clans.getPaymentHandler().deductAmount(amount, clan.getClanId())) {
            if(Clans.getPaymentHandler().addAmount(amount, context.getSource().asPlayer().getUniqueID()))
                sendFeedback(context, TextStyles.GREEN, "Successfully took %s %s from your clan's balance.", amount, Clans.getPaymentHandler().getCurrencyName(amount));
            else {
                Clans.getPaymentHandler().addAmount(amount, clan.getClanId());
                throwCommandFailure("Internal error: Your currency account not found.");
            }
        } else
            throwCommandFailure("%s does not have enough funds to do this.", clan.getClanName());
        return 1;
    }

    @SuppressWarnings("Duplicates")
    private static int runAbandonClaimCommand(CommandContext<CommandSource> context, @Nullable Clan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, EnumRank.ADMIN))
            return 0;
        assert clan != null;
        Chunk c = context.getSource().asPlayer().getEntityWorld().getChunk(context.getSource().asPlayer().getPosition());
        UUID claimFaction = ChunkUtils.getChunkOwner(c);
        if(claimFaction != null) {
            if(claimFaction.equals(clan.getClanId())) {
                if(!Clans.cfg.forceConnectedClaims || !ChunkUtils.hasConnectedClaim(c, clan.getClanId())) {
                    abandonClaim(context.getSource().asPlayer(), c, clan);
                    ChunkUtils.clearChunkOwner(c);
                    sendFeedback(context, TextStyles.GREEN, "Claim abandoned!");
                } else {//We are forcing connected claims and there is a claim connected
                    //Prevent creation of disconnected claims
                    abandonClaimWithAdjacencyCheck(context, c, clan);
                }
            } else
                throwCommandFailure("This land does not belong to %s.", clan.getClanName());
        } else
            throwCommandFailure("This land is not claimed.");
        return 1;
    }

    static void abandonClaim(EntityPlayerMP sender, Chunk c, Clan targetClan) {
        if (targetClan.hasHome()
                && sender.dimension.getId() == targetClan.getHomeDim()
                && targetClan.getHome().getX() >= c.getPos().getXStart()
                && targetClan.getHome().getX() <= c.getPos().getXEnd()
                && targetClan.getHome().getZ() >= c.getPos().getZStart()
                && targetClan.getHome().getZ() <= c.getPos().getZEnd()) {
            targetClan.unsetHome();
        }

        targetClan.subClaimCount();
        Clans.getPaymentHandler().addAmount(Clans.cfg.claimChunkCost, targetClan.getClanId());
    }

    static void abandonClaimWithAdjacencyCheck(CommandContext<CommandSource> context, Chunk c, Clan targetClan) throws CommandSyntaxException {
        boolean allowed = true;
        for (IChunk checkChunk : ChunkUtils.getConnectedClaims(c, targetClan.getClanId()))
            if (ChunkUtils.getConnectedClaims(checkChunk, targetClan.getClanId()).equals(Lists.newArrayList(c))) {
                allowed = false;
                break;
            }
        if (allowed) {
            abandonClaim(context.getSource().asPlayer(), c, targetClan);
            ChunkUtils.clearChunkOwner(c);
            sendFeedback(context, TextStyles.GREEN, "Claim abandoned!");
        } else
            throwCommandFailure("You cannot abandon this chunk of land because doing so would create at least one disconnected claim.");
    }

    @SuppressWarnings("Duplicates")
    private static int runClaimCommand(CommandContext<CommandSource> context, @Nullable Clan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, EnumRank.ADMIN))
            return 0;
        assert clan != null;
        Chunk c = context.getSource().asPlayer().getEntityWorld().getChunk(context.getSource().asPlayer().getPosition());
        UUID claimFaction = ChunkUtils.getChunkOwner(c);
        if(claimFaction != null) {
            if(claimFaction.equals(clan.getClanId()))
                throwCommandFailure("%s has already claimed this land.", clan.getClanName());
            else
                throwCommandFailure("Another clan (%s) has already claimed this land.", Objects.requireNonNull(ClanCache.getClan(claimFaction)).getClanName());
        } else {
            if(!Clans.cfg.forceConnectedClaims || ChunkUtils.hasConnectedClaim(c, clan.getClanId()) || clan.getClaimCount() == 0) {
                if(Clans.cfg.maxClanPlayerClaims <= 0 || clan.getClaimCount() < clan.getMaxClaimCount()) {
                    if (Clans.getPaymentHandler().deductAmount(Clans.cfg.claimChunkCost, clan.getClanId())) {
                        ChunkUtils.setChunkOwner(c, clan.getClanId());
                        clan.addClaimCount();
                        sendFeedback(context, TextStyles.GREEN, "Land claimed!");
                    } else
                        throwCommandFailure("Insufficient funds in %s's account to claim chunk. It costs %s %s.", clan.getClanName(), Clans.cfg.claimChunkCost, Clans.getPaymentHandler().getCurrencyName(Clans.cfg.claimChunkCost));
                } else
                    throwCommandFailure("%s is already at or above its max claim count of %s.", clan.getClanName(), clan.getMaxClaimCount());
            } else
                throwCommandFailure("You cannot claim this chunk of land because it is not next to another of %s's claims.", clan.getClanName());
        }
        return 1;
    }

    @SuppressWarnings("Duplicates")
    private static int runAcceptCommand(CommandContext<CommandSource> context, @Nullable Clan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, Clans.cfg.allowMultiClanMembership ? EnumRank.ANY : EnumRank.NOCLAN))
            return 0;
        Clan acceptClan = ClanCache.getInvite(context.getSource().asPlayer().getUniqueID());
        if(acceptClan != null){
            acceptClan.addMember(context.getSource().asPlayer().getUniqueID());
            sendFeedback(context, TextStyles.GREEN, "You joined %s.", acceptClan.getClanName());
        } else
            throwCommandFailure("You don't have any pending invites.");
        return 1;
    }

    @SuppressWarnings("Duplicates")
    private static int runDeclineCommand(CommandContext<CommandSource> context, @Nullable Clan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, Clans.cfg.allowMultiClanMembership ? EnumRank.ANY : EnumRank.NOCLAN))
            return 0;
        Clan declineClan = ClanCache.getInvite(context.getSource().asPlayer().getUniqueID());
        if(declineClan != null){
            ClanCache.removeInvite(context.getSource().asPlayer().getUniqueID());
            sendFeedback(context, TextStyles.GREEN, "You declined the invitation to join %s.", declineClan.getClanName());
        } else
            throwCommandFailure("You don't have any pending invites.");
        return 1;
    }

    @SuppressWarnings("Duplicates")
    private static int runDemoteCommand(CommandContext<CommandSource> context, @Nullable Clan clan) {
        if(!validateClanRank(context, clan, EnumRank.LEADER))
            return 0;
        assert clan != null;
        demotePlayer(context, clan);
        return 1;
    }

    static void demotePlayer(CommandContext<CommandSource> context, @Nonnull Clan clan) {
        String playerName = context.getArgument("target", String.class);
        GameProfile target = Clans.minecraftServer.getPlayerProfileCache().getGameProfileForUsername(playerName);

        if(target != null) {
            if (!ClanCache.getPlayerClans(target.getId()).isEmpty()) {
                if (ClanCache.getPlayerClans(target.getId()).contains(clan)) {
                    if (clan.demoteMember(target.getId())) {
                        sendFeedback(context, TextStyles.GREEN, "You have demoted %s.", target.getName());
                        if(ArrayUtils.contains(Clans.minecraftServer.getOnlinePlayerNames(), target))
                            Objects.requireNonNull(Clans.minecraftServer.getPlayerList().getPlayerByUsername(playerName)).sendMessage(new TextComponentTranslation("You have been demoted in %s by %s.", clan.getClanName(), context.getSource().getName()).setStyle(TextStyles.YELLOW));
                    } else
                        throwCommandFailure("The player %s could not be demoted in %s.", target.getName(), clan.getClanName());
                } else
                    throwCommandFailure("The player %s is not in %s.", target.getName(), clan.getClanName());
            } else
                throwCommandFailure("The player %s is not in %s.", target.getName(), clan.getClanName());
        } else
            throwCommandFailure("The player %s was not found.", playerName);
    }

    @SuppressWarnings("Duplicates")
    private static int runPromoteCommand(CommandContext<CommandSource> context, @Nullable Clan clan) {
        if(!validateClanRank(context, clan, EnumRank.LEADER))
            return 0;
        assert clan != null;
        promotePlayer(context, clan);
        return 1;
    }

    static void promotePlayer(CommandContext<CommandSource> context, @Nonnull Clan clan) {
        String playerName = context.getArgument("target", String.class);
        GameProfile target = Clans.minecraftServer.getPlayerProfileCache().getGameProfileForUsername(playerName);

        if(target != null) {
            if (!ClanCache.getPlayerClans(target.getId()).isEmpty()) {
                if (ClanCache.getPlayerClans(target.getId()).contains(clan)) {
                    if (clan.promoteMember(target.getId())) {
                        sendFeedback(context, TextStyles.GREEN, "You have promoted %s in %s.", target.getName(), clan.getClanName());
                        if(ArrayUtils.contains(Clans.minecraftServer.getOnlinePlayerNames(), target))
                            Objects.requireNonNull(Clans.minecraftServer.getPlayerList().getPlayerByUsername(playerName)).sendMessage(new TextComponentTranslation("You have been promoted in %s by %s.", clan.getClanName(), context.getSource().getName()).setStyle(TextStyles.GREEN));
                    } else
                        throwCommandFailure("The player %s could not be promoted.", target.getName());
                } else
                    throwCommandFailure("The player %s is not in %s.", target.getName(), clan.getClanName());
            } else
                throwCommandFailure("The player %s is not in %s.", target.getName(), clan.getClanName());
        } else
            throwCommandFailure("The player %s was not found.", playerName);
    }

    private static int runInviteCommand(CommandContext<CommandSource> context, @Nullable Clan clan) {
        if(!validateClanRank(context, clan, EnumRank.ADMIN))
            return 0;
        assert clan != null;
        String playerName = context.getArgument("target", String.class);
        EntityPlayerMP target = Clans.minecraftServer.getPlayerList().getPlayerByUsername(playerName);
        if(target != null) {
            if (Clans.cfg.allowMultiClanMembership || ClanCache.getPlayerClans(target.getUniqueID()).isEmpty()) {
                if (ClanCache.inviteToClan(target.getUniqueID(), clan))
                    sendFeedback(context, TextStyles.GREEN, "You have been invited to join %1$s. To join %1$s, type /clan accept. To decline, type /clan decline.", clan.getClanName());
                else
                    throwCommandFailure("The player %s has already been invited to join a clan. They must accept or decline that invitation first.", target.getName());
            } else
                throwCommandFailure("The player %s is already in a clan.", target.getName());
        } else
            throwCommandFailure("The target player %s is not online.", playerName);
        return 1;
    }

    private static int runKickCommand(CommandContext<CommandSource> context, @Nullable Clan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, EnumRank.ADMIN))
            return 0;
        assert clan != null;
        String playerName = context.getArgument("target", String.class);
        GameProfile target = Clans.minecraftServer.getPlayerProfileCache().getGameProfileForUsername(playerName);

        if(target != null) {
            if(target.getId().equals(context.getSource().asPlayer().getUniqueID())) {
                sendFeedback(context, TextStyles.YELLOW, "To leave a clan, use /clan leave.");
                return 0;
            }
            if (!ClanCache.getPlayerClans(target.getId()).isEmpty()) {
                if (ClanCache.getPlayerClans(target.getId()).contains(clan)) {
                    EnumRank senderRank = clan.getMembers().get(context.getSource().asPlayer().getUniqueID());
                    EnumRank targetRank = clan.getMembers().get(target.getId());
                    if (senderRank == EnumRank.LEADER) {
                        removeMember(context, clan, target);
                    } else if (targetRank == EnumRank.MEMBER) {
                        removeMember(context, clan, target);
                    } else
                        throwCommandFailure("You do not have the authority to kick out %s.", target.getName());
                } else
                    throwCommandFailure("The player %s is not in %s.", target.getName(), clan.getClanName());
            } else
                throwCommandFailure("The player %s is not in %s.", target.getName(), clan.getClanName());
        } else
            throwCommandFailure("The player %s was not found.", playerName);
        return 1;
    }

    public static void removeMember(CommandContext<CommandSource> context, Clan playerClan, GameProfile target) throws CommandException {
        if(playerClan.removeMember(target.getId())) {
            sendFeedback(context, TextStyles.GREEN, "You have kicked %s out of %s.", target.getName(), playerClan.getClanName());
            if(ArrayUtils.contains(Clans.minecraftServer.getOnlinePlayerNames(), target.getName()))
                Objects.requireNonNull(Clans.minecraftServer.getPlayerList().getPlayerByUsername(target.getName())).sendMessage(new TextComponentTranslation("You have been kicked out of %s by %s.", playerClan.getClanName(), context.getSource().getName()).setStyle(TextStyles.GREEN));
        } else
            throwCommandFailure("The player %s could not be kicked from %s. If %1$s is the only leader of %2$s, another leader should be promoted to leader before attempting to kick %1$s.", target.getName(), playerClan.getClanName());
    }

    private static int runLeaveCommand(CommandContext<CommandSource> context, @Nullable Clan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, EnumRank.MEMBER))
            return 0;
        assert clan != null;
        EnumRank senderRank = clan.getMembers().get(context.getSource().asPlayer().getUniqueID());
        if(senderRank == EnumRank.LEADER) {
            if(clan.getMembers().size() == 1){
                throwCommandFailure("You are the last member of %s. To disband it, use /clan disband.", clan.getClanName());
                return 0;
            }
            List<UUID> leaders = Lists.newArrayList();
            for(UUID member: clan.getMembers().keySet())
                if(clan.getMembers().get(member).equals(EnumRank.LEADER))
                    leaders.add(member);
            if(leaders.size() <= 1) {
                throwCommandFailure("You cannot leave %s without a leader. Promote someone else to be a leader before leaving.", clan.getClanName());
                return 0;
            }
        }
        if(clan.removeMember(context.getSource().asPlayer().getUniqueID())) {
            updateDefaultClan(context.getSource().asPlayer(), clan);
            sendFeedback(context, TextStyles.GREEN, "You have left %s.", clan.getClanName());
        } else //Internal error because this should be unreachable
            throwCommandFailure("Internal Error: You were unable to be removed from %s.", clan.getClanName());
        return 1;
    }

    /**
     * Check if a clan is the player's default clan, and if it is, update the player's default clan to something else.
     * @param player
     * The player to check and update (if needed)
     * @param removeClan
     * The clan the player is being removed from. Use null to forcibly change the player's default clan, regardless of what it currently is.
     */
    static void updateDefaultClan(EntityPlayerMP player, @Nullable Clan removeClan) {
        UUID oldDef = CapHelper.getPlayerClanCapability(player).getDefaultClan();
        if(removeClan == null || removeClan.getClanId().equals(oldDef))
            if(ClanCache.getPlayerClans(player.getUniqueID()).isEmpty())
                CapHelper.getPlayerClanCapability(player).setDefaultClan(null);
            else
                CapHelper.getPlayerClanCapability(player).setDefaultClan(ClanCache.getPlayerClans(player.getUniqueID()).get(0).getClanId());
    }

    private static int runHomeCommand(CommandContext<CommandSource> context, @Nullable Clan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, EnumRank.MEMBER))
            return 0;
        assert clan != null;
        BlockPos home = clan.getHome();
        int playerDim = context.getSource().asPlayer().dimension.getId();

        int cooldown = CapHelper.getPlayerClanCapability(context.getSource().asPlayer()).getCooldown();
        if(cooldown <= 0) {
            if (!clan.hasHome())
                throwCommandFailure("Error: %s does not have a set home. The clan leader should use /clan sethome to set one.", clan.getClanName());
            else {
                if(Clans.cfg.clanHomeWarmupTime > 0)
                    Timer.clanHomeWarmups.put(context.getSource().asPlayer(), new Pair<>(Clans.cfg.clanHomeWarmupTime, ClanCache.getPlayerClans(context.getSource().asPlayer().getUniqueID()).indexOf(clan)));
                else
                    teleportHome(context.getSource().asPlayer(), clan, home, playerDim);
            }
        } else
            throwCommandFailure("You cannot use this command until your cooldown runs out in %s seconds.", cooldown);
        return 1;
    }

    static void teleportHome(EntityPlayerMP player, Clan playerClan, BlockPos home, int playerDim) {
        if (playerDim == playerClan.getHomeDim() || player.changeDimension(Objects.requireNonNull(DimensionType.getById(playerClan.getHomeDim())), player.getServerWorld().getDefaultTeleporter()) != null) {
            if (!player.attemptTeleport(home.getX(), home.getY(), home.getZ())) {
                throwCommandFailure("Error teleporting to clan home. Ensure that it is not blocked.");
                if (playerDim != player.dimension.getId() && player.changeDimension(Objects.requireNonNull(DimensionType.getById(playerDim)), player.getServerWorld().getDefaultTeleporter()) == null)
                    throwCommandFailure("Error teleporting you back to the dimension you were in.");
            } else
                CapHelper.getPlayerClanCapability(player).setCooldown(Clans.cfg.clanHomeCooldownTime);
        } else
            throwCommandFailure("Error teleporting to clan home dimension.");
    }

    private static int runSetHomeCommand(CommandContext<CommandSource> context, @Nullable Clan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, EnumRank.LEADER))
            return 0;
        assert clan != null;
        Chunk c = context.getSource().asPlayer().getEntityWorld().getChunk(context.getSource().asPlayer().getPosition());
        if(clan.getClanId().equals(CapHelper.getClaimedLandCapability(c).getClan())) {
            for(Map.Entry<Clan, BlockPos> pos: ClanCache.getClanHomes().entrySet())
                if(pos.getValue() != null && pos.getKey() != clan && pos.getValue().getDistance(context.getSource().asPlayer().getPosition().getX(), context.getSource().asPlayer().getPosition().getY(), context.getSource().asPlayer().getPosition().getZ()) < Clans.cfg.minClanHomeDist) {
                    throwCommandFailure("You are too close to another clan's home! You must be at least %s blocks away from other clans' homes to set your clan home.", Clans.cfg.minClanHomeDist);
                    return 0;
                }
            clan.setHome(context.getSource().asPlayer().getPosition(), context.getSource().asPlayer().dimension.getId());
            sendFeedback(context, TextStyles.GREEN, "Clan home set!");
        } else
            throwCommandFailure("Clan home can only be set in clan territory!");
        return 1;
    }

    private static int runTrappedCommand(CommandContext<CommandSource> context, @Nullable Clan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, EnumRank.ANY))
            return 0;
        assert clan != null;
        Chunk origin = context.getSource().asPlayer().world.getChunk(context.getSource().asPlayer().getPosition());
        Clan chunkOwner = ClanCache.getClan(ChunkUtils.getChunkOwner(origin));
        if(chunkOwner == null && Clans.cfg.protectWilderness && context.getSource().asPlayer().getPosition().getY() >= Clans.cfg.minWildernessY) {
            BlockPos spawn = context.getSource().asPlayer().world.getSpawnPoint();
            context.getSource().asPlayer().attemptTeleport(spawn.getX(), spawn.getY(), spawn.getZ());
        } else if(chunkOwner != null && !chunkOwner.getMembers().containsKey(context.getSource().asPlayer().getUniqueID()) && (!RaidingParties.hasActiveRaid(chunkOwner) || !RaidingParties.getActiveRaid(chunkOwner).getMembers().contains(context.getSource().asPlayer()))) {
            int x = 0, z = 0, tmp, dx = 0, dz = -1;
            while(true) {//Spiral out until a player friendly chunk is found
                Chunk test = context.getSource().asPlayer().world.getChunk(origin.x + x, origin.z + z);
                Clan testChunkOwner = ClanCache.getClan(ChunkUtils.getChunkOwner(test));
                if(testChunkOwner == null || testChunkOwner.getMembers().containsKey(context.getSource().asPlayer().getUniqueID())) {
                    context.getSource().asPlayer().attemptTeleport((test.getPos().getXStart() + test.getPos().getXEnd())/2f, test.getWorld().getHeight(Heightmap.Type.MOTION_BLOCKING, (test.getPos().getXStart() + test.getPos().getXEnd())/2, (test.getPos().getZStart() + test.getPos().getZEnd())/2), (test.getPos().getZStart() + test.getPos().getZEnd())/2f);
                    break;
                }
                if(x == z || (x < 0 && x == -z) || (x > 0 && x == 1-z)) {
                    tmp = dx;
                    dx = -dz;
                    dz = tmp;
                }
                x += dx;
                z += dz;
            }
        }
        return 1;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean validateClanRank(CommandContext<CommandSource> context, @Nullable Clan selectedClan, EnumRank requiredRank) {
        if(!(context.getSource().getEntity() instanceof EntityPlayerMP)) {
            throwCommandFailure("You must be a player to do this!");
            return false;
        }

        if(selectedClan == null && !requiredRank.equals(EnumRank.ANY) && !requiredRank.equals(EnumRank.NOCLAN)) {
            throwCommandFailure("Invalid clan! Either you are not in a clan or the clan you typed does not exist!");
            return false;
        }

        if(selectedClan != null && requiredRank.equals(EnumRank.NOCLAN)) {
            throwCommandFailure("You cannot do this because you are in a clan!");
            return false;
        }

        if(selectedClan != null && !selectedClan.getMembers().containsKey(context.getSource().getEntity().getUniqueID())) {
            throwCommandFailure("You are not in %s!", selectedClan.getClanName());
            return false;
        }

        if(selectedClan != null && !selectedClan.getMembers().get(context.getSource().getEntity().getUniqueID()).greaterOrEquals(requiredRank)) {
            throwCommandFailure("You do not have permission to do this in %s!", selectedClan.getClanName());
            return false;
        }

        return true;
    }

    private static void throwCommandFailure(String message, Object... args) throws CommandException {
        throw new CommandException(new TextComponentTranslation(message, args).setStyle(TextStyles.RED));
    }

    private static void sendFeedback(CommandContext<CommandSource> context, Style color, String message, Object... args) throws CommandException {
        context.getSource().sendFeedback(new TextComponentTranslation(message, args).setStyle(color), false);
    }
}
