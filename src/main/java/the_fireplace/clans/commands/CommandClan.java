package the_fireplace.clans.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.*;
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
        for(NewClan c: ClanCache.getClansByPlayer(context.getSource().asPlayer().getUniqueID()))
            builder.suggest(c.getClanName());
        return builder.buildFuture();
    };

    public static final SuggestionProvider<CommandSource> clanSuggestion = (context, builder) -> {
        for(NewClan c: NewClanDatabase.getClans())
            builder.suggest(c.getClanName());
        return builder.buildFuture();
    };

    public static final SuggestionProvider<CommandSource> invitablePlayerSuggestion = (context, builder) -> {
        for(EntityPlayerMP p: ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
            if(Clans.cfg.allowMultiClanMembership || ClanCache.getClansByPlayer(p.getUniqueID()).isEmpty())
                builder.suggest(p.getName().getFormattedText());
        return builder.buildFuture();
    };

    @SuppressWarnings("Duplicates")
    public static void register(CommandDispatcher<CommandSource> commandDispatcher) {
        LiteralArgumentBuilder<CommandSource> clanCommand = Commands.literal("clan").requires((iCommandSender) -> iCommandSender.getEntity() instanceof EntityPlayerMP);
        RequiredArgumentBuilder<CommandSource, String> clanCommandWithClan = Commands.argument("clan", StringArgumentType.word()).suggests(playerClanSuggestion);

        clanCommand.then(Commands.literal("banner")
                .executes(context -> runBannerCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommand.then(Commands.literal("b")
                .executes(context -> runBannerCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommandWithClan.then(Commands.literal("banner")
                .executes(context -> runBannerCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))));
        clanCommandWithClan.then(Commands.literal("b")
                .executes(context -> runBannerCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))));

        clanCommand.then(Commands.literal("details")
                .executes(context -> runDetailsCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))
                .then(Commands.argument("clan", StringArgumentType.word()).suggests(clanSuggestion)
                .executes(context -> runDetailsCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));
        clanCommand.then(Commands.literal("info")
                .executes(context -> runDetailsCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))
                .then(Commands.argument("clan", StringArgumentType.word()).suggests(clanSuggestion)
                .executes(context -> runDetailsCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));
        clanCommand.then(Commands.literal("d")
                .executes(context -> runDetailsCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))
                .then(Commands.argument("clan", StringArgumentType.word()).suggests(clanSuggestion)
                .executes(context -> runDetailsCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));
        clanCommandWithClan.then(Commands.literal("details")
                .executes(context -> runDetailsCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))));
        clanCommandWithClan.then(Commands.literal("info")
                .executes(context -> runDetailsCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))));
        clanCommandWithClan.then(Commands.literal("d")
                .executes(context -> runDetailsCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))));

        clanCommand.then(Commands.literal("list")
                        .executes(CommandClan::runListCommand));

        clanCommand.then(Commands.literal("playerinfo")
                .executes(context -> runPlayerInfoCommand(context, context.getSource().asPlayer().getGameProfile()))
                .then(Commands.argument("target", EntityArgument.player())
                .executes(context -> runPlayerInfoCommand(context, EntityArgument.getPlayer(context, "target").getGameProfile()))));
        clanCommand.then(Commands.literal("pi")
                .executes(context -> runPlayerInfoCommand(context, context.getSource().asPlayer().getGameProfile()))
                .then(Commands.argument("target", EntityArgument.player())
                .executes(context -> runPlayerInfoCommand(context, EntityArgument.getPlayer(context, "target").getGameProfile()))));

        clanCommand.then(Commands.literal("disband")
                .executes(context -> runDisbandCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()), false)));
        clanCommandWithClan.then(Commands.literal("disband")
                .executes(context -> runDisbandCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)), false)));

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

        clanCommand.then(Commands.literal("fancymap")
                .executes(CommandClan::runFancyMapCommand));
        clanCommand.then(Commands.literal("fm")
                .executes(CommandClan::runFancyMapCommand));

        clanCommand.then(Commands.literal("setbanner")
                .executes(context -> runSetBannerCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()), null))
                .then(Commands.argument("banner", StringArgumentType.greedyString())
                .executes(context -> runSetBannerCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()), context.getArgument("banner", String.class)))));
        clanCommandWithClan.then(Commands.literal("setbanner")
                .executes(context -> runSetBannerCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)), null))
                .then(Commands.argument("banner", StringArgumentType.greedyString())
                .executes(context -> runSetBannerCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)), context.getArgument("banner", String.class)))));

        clanCommand.then(Commands.literal("setdefault")
                .then(Commands.argument("clan", StringArgumentType.word())
                .executes(context -> runSetDefaultCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));

        clanCommand.then(Commands.literal("setname")
                .then(Commands.argument("name", StringArgumentType.word())
                .executes(context -> runSetNameCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommandWithClan.then(Commands.literal("setname")
                .then(Commands.argument("name", StringArgumentType.word())
                .executes(context -> runSetNameCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));

        clanCommand.then(Commands.literal("setcolor")
                .then(Commands.argument("color", StringArgumentType.word())
                .executes(context -> runSetColorCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommand.then(Commands.literal("setcolour")
                .then(Commands.argument("color", StringArgumentType.word())
                .executes(context -> runSetColorCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommandWithClan.then(Commands.literal("setcolor")
                .then(Commands.argument("color", StringArgumentType.word())
                .executes(context -> runSetColorCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));
        clanCommandWithClan.then(Commands.literal("setcolour")
                .then(Commands.argument("color", StringArgumentType.word())
                .executes(context -> runSetColorCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));

        clanCommand.then(Commands.literal("setdescription")
                .then(Commands.argument("description", StringArgumentType.greedyString())
                .executes(context -> runSetDescriptionCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommand.then(Commands.literal("setdesc")
                .then(Commands.argument("description", StringArgumentType.greedyString())
                .executes(context -> runSetDescriptionCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommandWithClan.then(Commands.literal("setdescription")
                .then(Commands.argument("description", StringArgumentType.greedyString())
                .executes(context -> runSetDescriptionCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));
        clanCommandWithClan.then(Commands.literal("setdesc")
                .then(Commands.argument("description", StringArgumentType.greedyString())
                .executes(context -> runSetDescriptionCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));

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
                .executes(context -> runAddFundsCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));
        clanCommandWithClan.then(Commands.literal("deposit")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .executes(context -> runAddFundsCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));
        clanCommandWithClan.then(Commands.literal("af")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .executes(context -> runAddFundsCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));

        clanCommand.then(Commands.literal("balance")
                .executes(context -> runBalanceCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommandWithClan.then(Commands.literal("balance")
                .executes(context -> runBalanceCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))));

        clanCommand.then(Commands.literal("claim")
                .executes(context -> runClaimCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommand.then(Commands.literal("c")
                .executes(context -> runClaimCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommandWithClan.then(Commands.literal("claim")
                .executes(context -> runClaimCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))));
        clanCommandWithClan.then(Commands.literal("c")
                .executes(context -> runClaimCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))));

        clanCommand.then(Commands.literal("abandonclaim")
                .executes(context -> runAbandonClaimCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommand.then(Commands.literal("ac")
                .executes(context -> runAbandonClaimCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommandWithClan.then(Commands.literal("abandonclaim")
                .executes(context -> runAbandonClaimCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))));
        clanCommandWithClan.then(Commands.literal("ac")
                .executes(context -> runAbandonClaimCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))));

        clanCommand.then(Commands.literal("accept")
                .executes(context -> runAcceptCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));

        clanCommand.then(Commands.literal("decline")
                .executes(context -> runDeclineCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));

        clanCommand.then(Commands.literal("promote")
                .then(Commands.argument("target", StringArgumentType.word())
                .executes(context -> runPromoteCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommandWithClan.then(Commands.literal("promote")
                .then(Commands.argument("target", StringArgumentType.word())
                .executes(context -> runPromoteCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));

        clanCommand.then(Commands.literal("demote")
                .then(Commands.argument("target", StringArgumentType.word())
                .executes(context -> runDemoteCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommandWithClan.then(Commands.literal("demote")
                .then(Commands.argument("target", StringArgumentType.word())
                .executes(context -> runDemoteCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));

        clanCommand.then(Commands.literal("invite")
                .then(Commands.argument("target", StringArgumentType.word()).suggests(invitablePlayerSuggestion)
                .executes(context -> runInviteCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommand.then(Commands.literal("i")
                .then(Commands.argument("target", StringArgumentType.word()).suggests(invitablePlayerSuggestion)
                .executes(context -> runInviteCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommandWithClan.then(Commands.literal("invite")
                .then(Commands.argument("target", StringArgumentType.word()).suggests(invitablePlayerSuggestion)
                .executes(context -> runInviteCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));
        clanCommandWithClan.then(Commands.literal("i")
                .then(Commands.argument("target", StringArgumentType.word()).suggests(invitablePlayerSuggestion)
                .executes(context -> runInviteCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));

        clanCommand.then(Commands.literal("kick")
                .then(Commands.argument("target", StringArgumentType.word())
                .executes(context -> runKickCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
        clanCommandWithClan.then(Commands.literal("kick")
                .then(Commands.argument("target", StringArgumentType.word())
                .executes(context -> runKickCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));

        clanCommand.then(Commands.literal("leave")
                .executes(context -> runLeaveCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommandWithClan.then(Commands.literal("leave")
                .executes(context -> runLeaveCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))));

        clanCommand.then(Commands.literal("home")
                .executes(context -> runHomeCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommand.then(Commands.literal("h")
                .executes(context -> runHomeCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommandWithClan.then(Commands.literal("home")
                .executes(context -> runHomeCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))));
        clanCommandWithClan.then(Commands.literal("h")
                .executes(context -> runHomeCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))));

        clanCommand.then(Commands.literal("sethome")
                .executes(context -> runSetHomeCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
        clanCommandWithClan.then(Commands.literal("sethome")
                .executes(context -> runSetHomeCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))));

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
                    .executes(context -> runTakeFundsCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));
            clanCommandWithClan.then(Commands.literal("withdraw")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .executes(context -> runTakeFundsCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));
        }

        if(Clans.cfg.chargeRentDays > 0) {
            clanCommand.then(Commands.literal("setrent")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .executes(context -> runSetRentCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer())))));
            clanCommandWithClan.then(Commands.literal("setrent")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .executes(context -> runSetRentCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));
        }

        if(Clans.cfg.chargeRentDays > 0 || Clans.cfg.clanUpkeepDays > 0) {
            clanCommand.then(Commands.literal("finances")
                    .executes(context -> runFinancesCommand(context, ClanCache.getPlayerDefaultClan(context.getSource().asPlayer()))));
            clanCommandWithClan.then(Commands.literal("finances")
                    .executes(context -> runFinancesCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))));
        }

        clanCommand.then(clanCommandWithClan);

        LiteralCommandNode<CommandSource> clanNode = commandDispatcher.register(clanCommand);
        commandDispatcher.register(Commands.literal("c").redirect(clanNode));
    }

    private static int runBannerCommand(CommandContext<CommandSource> context, @Nullable NewClan selectedClan) throws CommandSyntaxException {
        if(!validateClanRank(context, selectedClan, EnumRank.MEMBER))
            return 0;
        assert selectedClan != null;
        NBTTagCompound banner;
        if(selectedClan.getClanBanner() != null) {
            try {
                banner = JsonToNBT.getTagFromJson(selectedClan.getClanBanner());
            } catch (CommandSyntaxException e) {
                throwCommandFailure("%s does not have a banner.", selectedClan.getClanName());
                return 1;
            }
        } else {
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

    private static int runDetailsCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) {
        if(!validateClanRank(context, clan, clan == null ? EnumRank.MEMBER : EnumRank.ANY))
            return 0;
        assert clan != null;
        sendFeedback(context, TextStyles.GREEN, "Clan name: %s", clan.getClanName());
        sendFeedback(context, TextStyles.GREEN, "Clan description: %s", clan.getDescription());
        sendFeedback(context, TextStyles.GREEN, "Number of claims: %s", clan.getClaimCount());
        if(!clan.isOpclan())
            sendFeedback(context, TextStyles.GREEN, "Number of members: %s", clan.getMemberCount());
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
            sendFeedback(context, TextStyles.GREEN, "Members:");
            for(UUID leader: leaders) {
                GameProfile l = context.getSource().getServer().getPlayerProfileCache().getProfileByUUID(leader);
                if(l != null)
                    sendFeedback(context, context.getSource().getServer().getPlayerList().getPlayerByUUID(leader) != null ? TextStyles.ONLINE_LEADER : TextStyles.OFFLINE_LEADER, "Leader %s", l.getName());
            }
            for(UUID admin: admins) {
                GameProfile a = context.getSource().getServer().getPlayerProfileCache().getProfileByUUID(admin);
                if(a != null)
                    sendFeedback(context, context.getSource().getServer().getPlayerList().getPlayerByUUID(admin) != null ? TextStyles.ONLINE_ADMIN : TextStyles.OFFLINE_ADMIN, "Admin %s", a.getName());
            }
            for(UUID member: members) {
                GameProfile m = context.getSource().getServer().getPlayerProfileCache().getProfileByUUID(member);
                if(m != null)
                    sendFeedback(context, context.getSource().getServer().getPlayerList().getPlayerByUUID(member) != null ? TextStyles.GREEN : TextStyles.YELLOW, "Member %s", m.getName());
            }
        } else if(!clan.isOpclan()) {
            throwCommandFailure("Error: %s has no members.", clan.getClanName());
            Clans.LOGGER.error("Clan %s has no members.", clan.getClanName());
        }
        return 1;
    }

    private static int runListCommand(CommandContext<CommandSource> context) {
        sendFeedback(context, TextStyles.GREEN, "Clans on this server:");
        if(!NewClanDatabase.getClans().isEmpty()) {
            for (NewClan clan : NewClanDatabase.getClans())
                sendFeedback(context, TextStyles.GREEN, clan.getClanName() + " - " + clan.getDescription());
        } else
            sendFeedback(context, TextStyles.YELLOW, "There are no clans on this server.");
        return 1;
    }

    private static int runPlayerInfoCommand(CommandContext<CommandSource> context, @Nullable GameProfile target) throws CommandSyntaxException {
        if(target == null) {
            if(!validateClanRank(context, null, EnumRank.ANY))
                return 0;
            target = context.getSource().asPlayer().getGameProfile();
        }
        sendFeedback(context, TextStyles.GREEN, "Player name: %s", target.getName());
        List<NewClan> leaders = Lists.newArrayList();
        List<NewClan> admins = Lists.newArrayList();
        List<NewClan> members = Lists.newArrayList();
        for(NewClan clan: ClanCache.getClansByPlayer(target.getId())) {
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
            NewClan defaultClan = null;
            if(ArrayUtils.contains(context.getSource().getServer().getOnlinePlayerNames(), target.getName()))
                defaultClan = ClanCache.getClanById(CapHelper.getPlayerClanCapability(Objects.requireNonNull(context.getSource().getServer().getPlayerList().getPlayerByUUID(target.getId()))).getDefaultClan());
            sendFeedback(context, TextStyles.GREEN, "Clans: ");
            for(NewClan leader: leaders)
                sendFeedback(context, defaultClan != null && leader.getClanId().equals(defaultClan.getClanId()) ? TextStyles.ONLINE_LEADER : TextStyles.GREEN, "Leader of %s", leader.getClanName());
            for(NewClan admin: admins)
                sendFeedback(context, defaultClan != null && admin.getClanId().equals(defaultClan.getClanId()) ? TextStyles.ONLINE_ADMIN : TextStyles.GREEN, "Admin of %s", admin.getClanName());
            for(NewClan member: members)
                sendFeedback(context, defaultClan != null && member.getClanId().equals(defaultClan.getClanId()) ? TextStyles.ONLINE_ADMIN : TextStyles.GREEN, "Member of %s", member.getClanName());
        } else
            sendFeedback(context, TextStyles.GREEN, "%s is not in any clans.", target.getName());
        return 1;
    }

    @SuppressWarnings("Duplicates")
    static int runDisbandCommand(CommandContext<CommandSource> context, @Nullable NewClan clan, boolean isOpclanCommand) throws CommandSyntaxException {
        if (!isOpclanCommand && !validateClanRank(context, clan, EnumRank.LEADER))
            return 0;
        assert clan != null;
        if(!clan.isOpclan()) {
            if (NewClanDatabase.removeClan(clan.getClanId())) {
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
                        player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(member);
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

    private static int runFormCommand(CommandContext<CommandSource> context, @Nullable NewClan selectedClan, @Nullable String banner) throws CommandSyntaxException {
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
                NewClan c = new NewClan(newClanName, context.getSource().asPlayer().getUniqueID(), banner);
                if(ClanCache.getClansByPlayer(context.getSource().asPlayer().getUniqueID()).size() == 1)
                    CapHelper.getPlayerClanCapability(context.getSource().asPlayer()).setDefaultClan(c.getClanId());
                sendFeedback(context, TextStyles.GREEN, "Clan formed!");
            } else
                throwCommandFailure("Insufficient funds to form clan. It costs %s %s.", Clans.cfg.formClanCost, Clans.getPaymentHandler().getCurrencyName(Clans.cfg.formClanCost));
        }
        return 1;
    }

    private static final char[] mapchars = {'#', '&', '@', '*', '+', '<', '>', '~', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7', '8', '9', 'w', 'm'};
    private static int runMapCommand(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if(!validateClanRank(context, null, EnumRank.ANY))
            return 0;
        World w = context.getSource().asPlayer().getEntityWorld();
        Chunk center = w.getChunk(context.getSource().asPlayer().getPosition());

        Map<UUID, Character> symbolMap = Maps.newHashMap();
        sendFeedback(context, TextStyles.GREEN, "=====================================================");

        UUID sender = context.getSource().asPlayer().getUniqueID();

        new Thread(() -> {
            for (int z = center.z - 5; z <= center.z + 5; z++) {
                StringBuilder row = new StringBuilder();
                for (int x = center.x - 26; x <= center.x + 26; x++) {
                    String wildernessColor = center.z == z && center.x == x ? "§9" : "§e";
                    NewClan clan = ClanChunkCache.getChunkClan(x, z, w.getDimension().getType().getId());
                    if (clan == null)
                        row.append(wildernessColor).append('-');
                    else {
                        if (!symbolMap.containsKey(clan.getClanId()))
                            symbolMap.put(clan.getClanId(), mapchars[symbolMap.size() % mapchars.length]);
                        row.append(center.z == z && center.x == x ? "§9" : clan.getMembers().containsKey(sender) ? "§a" : "§c").append(symbolMap.get(clan.getClanId()));
                    }
                }
                sendFeedback(context, null, row.toString());
            }
            sendFeedback(context, TextStyles.GREEN, "=====================================================");
            for (Map.Entry<UUID, Character> symbol : symbolMap.entrySet()) {
                NewClan c = ClanCache.getClanById(symbol.getKey());
                sendFeedback(context, TextStyles.GREEN, symbol.getValue() + ": " + (c != null ? c.getClanName() : "Wilderness"));
            }
        }).start();
        return 1;
    }

    private static int runFancyMapCommand(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if(!validateClanRank(context, null, EnumRank.ANY))
            return 0;
        World w = context.getSource().asPlayer().getEntityWorld();
        Chunk center = w.getChunk(context.getSource().asPlayer().getPosition());

        Map<UUID, Character> symbolMap = Maps.newHashMap();
        sendFeedback(context, TextStyles.GREEN, "=====================================================");
        new Thread(() -> {
            for(int z=center.z-26; z <= center.z + 26; z++) {
                StringBuilder row = new StringBuilder();
                for (int x = center.x - 26; x <= center.x + 26; x++) {
                    String wildernessColor = center.z == z && center.x == x ? "§9" : Clans.cfg.protectWilderness ? "§e" : "§2";
                    NewClan clan = ClanChunkCache.getChunkClan(x, z, w.getDimension().getType().getId());
                    if(clan == null)
                        row.append(wildernessColor).append('-');
                    else {
                        if (!symbolMap.containsKey(clan.getClanId()))
                            symbolMap.put(clan.getClanId(), mapchars[symbolMap.size() % mapchars.length]);
                        row.append(center.z == z && center.x == x ? "§9": '§'+Integer.toHexString(clan.getTextColor().getColorIndex())).append(symbolMap.get(clan.getClanId()));
                    }
                }
                sendFeedback(context, null, row.toString());
            }
            sendFeedback(context, TextStyles.GREEN, "=====================================================");
            for(Map.Entry<UUID, Character> symbol: symbolMap.entrySet()) {
                NewClan c = ClanCache.getClanById(symbol.getKey());
                sendFeedback(context, c != null ? new Style().setColor(c.getTextColor()) : TextStyles.YELLOW, symbol.getValue() + ": " +(c != null ? c.getClanName() : "Wilderness"));
            }
        }).start();
        return 1;
    }

    private static int runSetBannerCommand(CommandContext<CommandSource> context, @Nullable NewClan clan, @Nullable String banner) throws CommandSyntaxException {
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

    private static void setClanBannerFromItem(CommandContext<CommandSource> context, NewClan playerClan, @Nullable NBTTagCompound tags) {
        String banner = tags != null ? tags.toString() : "";
        if(ClanCache.clanBannerTaken(banner))
            throwCommandFailure("The clan banner you have specified is already taken.");
        else {
            playerClan.setClanBanner(banner);
            sendFeedback(context, TextStyles.GREEN, "Clan banner set!");
        }
    }

    private static int runSetDefaultCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, EnumRank.MEMBER))
            return 0;
        assert clan != null;
        CapHelper.getPlayerClanCapability(context.getSource().asPlayer()).setDefaultClan(clan.getClanId());
        return 1;
    }

    private static int runSetDescriptionCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) {
        if(!validateClanRank(context, clan, EnumRank.LEADER))
            return 0;
        assert clan != null;
        clan.setDescription(context.getArgument("description", String.class));
        sendFeedback(context, TextStyles.GREEN, "Clan description for %s set!", clan.getClanName());
        return 1;
    }

    private static int runSetNameCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) {
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

    private static int runSetColorCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) {
        if(!validateClanRank(context, clan, EnumRank.LEADER))
            return 0;
        assert clan != null;
        String newColor = context.getArgument("color", String.class);
        try {
            clan.setColor(newColor.startsWith("0x") ? Integer.parseInt(newColor.substring(2), 16) : Integer.parseInt(newColor));
            sendFeedback(context, TextStyles.GREEN, "Clan color for %s set!", clan.getClanName());
        } catch(NumberFormatException e) {
            throwCommandFailure("Invalid color integer: %s", newColor);
        }
        return 1;
    }

    private static int runAddFundsCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) throws CommandSyntaxException {
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

    private static int runBalanceCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) {
        if(!validateClanRank(context, clan, EnumRank.MEMBER))
            return 0;
        assert clan != null;
        long balance = Clans.getPaymentHandler().getBalance(clan.getClanId());
        sendFeedback(context, TextStyles.GREEN, "Clan balance: %s %s", balance, Clans.getPaymentHandler().getCurrencyName(balance));
        return 1;
    }

    private static int runFinancesCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) {
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
        if(rent <= 0 && upkeep <= 0)
            sendFeedback(context, TextStyles.GREEN, "Your clan is not earning or losing money.");
        return 1;
    }

    private static int runSetRentCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) {
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

    private static int runTakeFundsCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) throws CommandSyntaxException {
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
    private static int runAbandonClaimCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) throws CommandSyntaxException {
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

    static void abandonClaim(EntityPlayerMP sender, Chunk c, NewClan targetClan) {
        if (targetClan.hasHome()
                && targetClan.getHome() != null
                && sender.dimension.getId() == targetClan.getHomeDim()
                && targetClan.getHome().getX() >= c.getPos().getXStart()
                && targetClan.getHome().getX() <= c.getPos().getXEnd()
                && targetClan.getHome().getZ() >= c.getPos().getZStart()
                && targetClan.getHome().getZ() <= c.getPos().getZEnd()) {
            targetClan.unsetHome();
        }

        ClanChunkCache.delChunk(targetClan, c.x, c.z, c.getWorld().getDimension().getType().getId());
        targetClan.subClaimCount();
        Clans.getPaymentHandler().addAmount(Clans.cfg.claimChunkCost, targetClan.getClanId());
    }

    static void abandonClaimWithAdjacencyCheck(CommandContext<CommandSource> context, Chunk c, NewClan targetClan) throws CommandSyntaxException {
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
    private static int runClaimCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) throws CommandSyntaxException {
        if (!validateClanRank(context, clan, EnumRank.ADMIN))
            return 0;
        assert clan != null;
        Chunk c = context.getSource().asPlayer().getEntityWorld().getChunk(context.getSource().asPlayer().getPosition());
        UUID claimFaction = ChunkUtils.getChunkOwner(c);
        if (claimFaction != null) {
            if (claimFaction.equals(clan.getClanId()))
                sendFeedback(context, TextStyles.YELLOW,"%s has already claimed this land.", clan.getClanName());
            else
                throwCommandFailure("Another clan (%s) has already claimed this land.", Objects.requireNonNull(ClanCache.getClanById(claimFaction)).getClanName());
        } else {
            if (!Clans.cfg.forceConnectedClaims || ChunkUtils.hasConnectedClaim(c, clan.getClanId()) || clan.getClaimCount() == 0) {
                if (Clans.cfg.maxClanPlayerClaims <= 0 || clan.getClaimCount() < clan.getMaxClaimCount()) {
                    if(clan.getClaimCount() > 0) {
                        claimChunk(context, c, clan);
                    } else if(Clans.cfg.minClanHomeDist > 0 && Clans.cfg.initialClaimSeparationMultiplier > 0) {
                        boolean inClanHomeRange = false;
                        for(Map.Entry<NewClan, BlockPos> pos: ClanCache.getClanHomes().entrySet())
                            if(!pos.getKey().getClanId().equals(clan.getClanId()) && pos.getKey().hasHome() && pos.getValue() != null && pos.getValue().getDistance(context.getSource().asPlayer().getPosition().getX(), context.getSource().asPlayer().getPosition().getY(), context.getSource().asPlayer().getPosition().getZ()) < Clans.cfg.minClanHomeDist*Clans.cfg.initialClaimSeparationMultiplier)
                                inClanHomeRange = true;
                        if(inClanHomeRange) {
                            if(Clans.cfg.enforceInitialClaimSeparation)
                                context.getSource().asPlayer().sendMessage(new TextComponentTranslation("You cannot claim this chunk of land because it is too close to another clan's home. Make sure you are at least %s blocks away from other clans' homes before trying again.", Clans.cfg.minClanHomeDist*Clans.cfg.initialClaimSeparationMultiplier).setStyle(TextStyles.RED));
                            else if(CapHelper.getPlayerClanCapability(context.getSource().asPlayer()).getClaimWarning())
                                claimChunk(context, c, clan);
                            else {
                                sendFeedback(context, TextStyles.YELLOW, "It is recommended that you do not claim this chunk of land because it is within %s blocks of another clan's home. Type /clan claim again to claim this land anyways.", Clans.cfg.minClanHomeDist*Clans.cfg.initialClaimSeparationMultiplier);
                                CapHelper.getPlayerClanCapability(context.getSource().asPlayer()).setClaimWarning(true);
                            }
                        } else
                            claimChunk(context, c, clan);
                    } else
                        claimChunk(context, c, clan);
                } else
                    throwCommandFailure("%s is already at or above its max claim count of %s.", clan.getClanName(), clan.getMaxClaimCount());
            } else
                throwCommandFailure("You cannot claim this chunk of land because it is not next to another of %s's claims.", clan.getClanName());
        }
        return 1;
    }

    private static void claimChunk(CommandContext<CommandSource> context, Chunk c, NewClan selectedClan) {
        if (Clans.getPaymentHandler().deductAmount(Clans.cfg.claimChunkCost, selectedClan.getClanId())) {
            ChunkUtils.setChunkOwner(c, selectedClan.getClanId());
            ClanChunkCache.addChunk(selectedClan, c.x, c.z, c.getWorld().getDimension().getType().getId());
            selectedClan.addClaimCount();
            sendFeedback(context, TextStyles.GREEN, "Land claimed!");
        } else
            throwCommandFailure("Insufficient funds in clan account to claim chunk. It costs %s %s.", Clans.cfg.claimChunkCost, Clans.getPaymentHandler().getCurrencyName(Clans.cfg.claimChunkCost));
    }

    @SuppressWarnings("Duplicates")
    private static int runAcceptCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, Clans.cfg.allowMultiClanMembership ? EnumRank.ANY : EnumRank.NOCLAN))
            return 0;
        NewClan acceptClan = ClanCache.getInvite(context.getSource().asPlayer().getUniqueID());
        if(acceptClan != null){
            acceptClan.addMember(context.getSource().asPlayer().getUniqueID());
            if(ClanCache.getClansByPlayer(context.getSource().assertIsEntity().getUniqueID()).size() == 1)
                CapHelper.getPlayerClanCapability(context.getSource().asPlayer()).setDefaultClan(acceptClan.getClanId());
            sendFeedback(context, TextStyles.GREEN, "You joined %s.", acceptClan.getClanName());
        } else
            throwCommandFailure("You don't have any pending invites.");
        return 1;
    }

    @SuppressWarnings("Duplicates")
    private static int runDeclineCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, Clans.cfg.allowMultiClanMembership ? EnumRank.ANY : EnumRank.NOCLAN))
            return 0;
        NewClan declineClan = ClanCache.getInvite(context.getSource().asPlayer().getUniqueID());
        if(declineClan != null){
            ClanCache.removeInvite(context.getSource().asPlayer().getUniqueID());
            sendFeedback(context, TextStyles.GREEN, "You declined the invitation to join %s.", declineClan.getClanName());
        } else
            throwCommandFailure("You don't have any pending invites.");
        return 1;
    }

    @SuppressWarnings("Duplicates")
    private static int runDemoteCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) {
        if(!validateClanRank(context, clan, EnumRank.LEADER))
            return 0;
        assert clan != null;
        demotePlayer(context, clan);
        return 1;
    }

    static void demotePlayer(CommandContext<CommandSource> context, @Nonnull NewClan clan) {
        String playerName = context.getArgument("target", String.class);
        GameProfile target = ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache().getGameProfileForUsername(playerName);

        if(target != null) {
            if (!ClanCache.getClansByPlayer(target.getId()).isEmpty()) {
                if (ClanCache.getClansByPlayer(target.getId()).contains(clan)) {
                    if (clan.demoteMember(target.getId())) {
                        sendFeedback(context, TextStyles.GREEN, "You have demoted %s to %s in %s.", target.getName(), clan.getMembers().get(target.getId()).toString().toLowerCase(), clan.getClanName());
                        if(ArrayUtils.contains(ServerLifecycleHooks.getCurrentServer().getOnlinePlayerNames(), target))
                            Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(playerName)).sendMessage(new TextComponentTranslation("You have been demoted in %s to %s by %s.", clan.getClanName(), clan.getMembers().get(target.getId()).toString().toLowerCase(), context.getSource().getName()).setStyle(TextStyles.YELLOW));
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
    private static int runPromoteCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) {
        if(!validateClanRank(context, clan, EnumRank.LEADER))
            return 0;
        assert clan != null;
        promotePlayer(context, clan);
        return 1;
    }

    static void promotePlayer(CommandContext<CommandSource> context, @Nonnull NewClan clan) {
        String playerName = context.getArgument("target", String.class);
        GameProfile target = ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache().getGameProfileForUsername(playerName);

        if(target != null) {
            if (!ClanCache.getClansByPlayer(target.getId()).isEmpty()) {
                if (ClanCache.getClansByPlayer(target.getId()).contains(clan)) {
                    if (clan.promoteMember(target.getId())) {
                        sendFeedback(context, TextStyles.GREEN, "You have promoted %s to %s in %s.", target.getName(), clan.getMembers().get(target.getId()).toString().toLowerCase(), clan.getClanName());
                        for(Map.Entry<EntityPlayerMP, EnumRank> m : clan.getOnlineMembers().entrySet())
                            if(m.getValue().greaterOrEquals(clan.getMembers().get(target.getId())))
                                if(!m.getKey().getUniqueID().equals(target.getId()))
                                    m.getKey().sendMessage(new TextComponentTranslation("%s has been promoted to %s in %s by %s.", target.getName(), clan.getMembers().get(target.getId()).toString().toLowerCase(), clan.getClanName(), context.getSource().getDisplayName().getFormattedText()).setStyle(TextStyles.GREEN));
                        if(ArrayUtils.contains(ServerLifecycleHooks.getCurrentServer().getOnlinePlayerNames(), target))
                            Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(playerName)).sendMessage(new TextComponentTranslation("You have been promoted in %s to %s by %s.", clan.getClanName(), clan.getMembers().get(target.getId()).toString().toLowerCase(), context.getSource().getName()).setStyle(TextStyles.GREEN));
                    } else
                        throwCommandFailure("The player %s could not be promoted.", target.getName());
                } else
                    throwCommandFailure("The player %s is not in %s.", target.getName(), clan.getClanName());
            } else
                throwCommandFailure("The player %s is not in %s.", target.getName(), clan.getClanName());
        } else
            throwCommandFailure("The player %s was not found.", playerName);
    }

    private static int runInviteCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) {
        if(!validateClanRank(context, clan, EnumRank.ADMIN))
            return 0;
        assert clan != null;
        String playerName = context.getArgument("target", String.class);
        EntityPlayerMP target = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(playerName);
        if(target != null) {
            if (Clans.cfg.allowMultiClanMembership || ClanCache.getClansByPlayer(target.getUniqueID()).isEmpty()) {
                if(!ClanCache.getClansByPlayer(target.getUniqueID()).contains(clan)) {
                    if (ClanCache.inviteToClan(target.getUniqueID(), clan)) {
                        sendFeedback(context, TextStyles.GREEN, "You have invited %s to join %s.", target.getDisplayName().getFormattedText(), clan.getClanName());
                        target.sendMessage(new TextComponentTranslation("You have been invited to join %1$s. To join %1$s, type /clan accept. To decline, type /clan decline.", clan.getClanName()));
                    } else
                        throwCommandFailure("The player %s has already been invited to join a clan. They must accept or decline that invitation first.", target.getName());
                } else
                    throwCommandFailure("The player %s is already in %s.", target.getName(), clan.getClanName());
            } else
                throwCommandFailure("The player %s is already in a clan.", target.getName());
        } else
            throwCommandFailure("The target player %s is not online.", playerName);
        return 1;
    }

    private static int runKickCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, EnumRank.ADMIN))
            return 0;
        assert clan != null;
        String playerName = context.getArgument("target", String.class);
        GameProfile target = ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache().getGameProfileForUsername(playerName);

        if(target != null) {
            if(target.getId().equals(context.getSource().asPlayer().getUniqueID())) {
                sendFeedback(context, TextStyles.YELLOW, "To leave a clan, use /clan leave.");
                return 0;
            }
            if (!ClanCache.getClansByPlayer(target.getId()).isEmpty()) {
                if (ClanCache.getClansByPlayer(target.getId()).contains(clan)) {
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

    public static void removeMember(CommandContext<CommandSource> context, NewClan playerClan, GameProfile target) throws CommandException {
        if(playerClan.removeMember(target.getId())) {
            sendFeedback(context, TextStyles.GREEN, "You have kicked %s out of %s.", target.getName(), playerClan.getClanName());
            if(ArrayUtils.contains(ServerLifecycleHooks.getCurrentServer().getOnlinePlayerNames(), target.getName())) {
                EntityPlayerMP targetPlayer = Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(target.getId()));
                targetPlayer.sendMessage(new TextComponentTranslation("You have been kicked out of %s by %s.", playerClan.getClanName(), context.getSource().getName()).setStyle(TextStyles.YELLOW));
                if(playerClan.getClanId().equals(CapHelper.getPlayerClanCapability(targetPlayer).getDefaultClan()))
                    updateDefaultClan(targetPlayer, playerClan);
            }
        } else
            throwCommandFailure("The player %s could not be kicked from %s. If %1$s is the only leader of %2$s, another leader should be promoted to leader before attempting to kick %1$s.", target.getName(), playerClan.getClanName());
    }

    private static int runLeaveCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) throws CommandSyntaxException {
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
    public static void updateDefaultClan(EntityPlayerMP player, @Nullable NewClan removeClan) {
        UUID oldDef = CapHelper.getPlayerClanCapability(player).getDefaultClan();
        if(removeClan == null || removeClan.getClanId().equals(oldDef))
            if(ClanCache.getClansByPlayer(player.getUniqueID()).isEmpty())
                CapHelper.getPlayerClanCapability(player).setDefaultClan(null);
            else
                CapHelper.getPlayerClanCapability(player).setDefaultClan(ClanCache.getClansByPlayer(player.getUniqueID()).get(0).getClanId());
    }

    private static int runHomeCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, EnumRank.MEMBER))
            return 0;
        assert clan != null;
        if(Clans.cfg.clanHomeWarmupTime > -1) {
            BlockPos home = clan.getHome();
            int playerDim = context.getSource().asPlayer().dimension.getId();

            int cooldown = CapHelper.getPlayerClanCapability(context.getSource().asPlayer()).getCooldown();
            if (cooldown <= 0) {
                if (!clan.hasHome() || home == null)
                    throwCommandFailure("Error: %s does not have a set home. The clan leader should use /clan sethome to set one.", clan.getClanName());
                else {
                    if (Clans.cfg.clanHomeWarmupTime > 0)
                        Timer.clanHomeWarmups.put(context.getSource().asPlayer(), new Pair<>(Clans.cfg.clanHomeWarmupTime, ClanCache.getClansByPlayer(context.getSource().asPlayer().getUniqueID()).indexOf(clan)));
                    else
                        teleportHome(context.getSource().asPlayer(), clan, home, playerDim);
                }
            } else
                throwCommandFailure("You cannot use this command until your cooldown runs out in %s seconds.", cooldown);
        } else
            throwCommandFailure("/clan home is disabled on this server.");
        return 1;
    }

    public static void teleportHome(EntityPlayerMP player, NewClan playerClan, BlockPos home, int playerDim) {
        home = getSafeExitLocation(Objects.requireNonNull(player.getServer()).getWorld(Objects.requireNonNull(DimensionType.getById(playerClan.getHomeDim()))), home, 5);
        if (playerDim == playerClan.getHomeDim()) {
            if (!player.attemptTeleport(home.getX(), home.getY(), home.getZ())) {
                throwCommandFailure("Error teleporting to clan home. Ensure that it is not blocked.");
                if (playerDim != player.dimension.getId() && player.changeDimension(Objects.requireNonNull(DimensionType.getById(playerDim)), player.getServerWorld().getDefaultTeleporter()) == null)
                    throwCommandFailure("Error teleporting you back to the dimension you were in.");
            } else
                CapHelper.getPlayerClanCapability(player).setCooldown(Clans.cfg.clanHomeCooldownTime);
        } else {
            player.setPortal(player.getPosition());
            if (player.changeDimension(Objects.requireNonNull(DimensionType.getById(playerClan.getHomeDim())), player.getServerWorld().getDefaultTeleporter()) != null) {
                if (!player.attemptTeleport(home.getX(), home.getY(), home.getZ())) {
                    player.sendMessage(new TextComponentString("Error teleporting to clan home. Ensure that it is not blocked.").setStyle(TextStyles.RED));
                    if (playerDim != player.dimension.getId() && player.changeDimension(Objects.requireNonNull(DimensionType.getById(playerDim)), player.getServerWorld().getDefaultTeleporter()) == null)
                        player.sendMessage(new TextComponentString("Error teleporting you back to the dimension you were in.").setStyle(TextStyles.RED));
                } else
                    CapHelper.getPlayerClanCapability(player).setCooldown(Clans.cfg.clanHomeCooldownTime);
            } else {
                player.sendMessage(new TextComponentString("Error teleporting to clan home dimension.").setStyle(TextStyles.RED));
            }
        }
    }

    private static BlockPos getSafeExitLocation(World worldIn, BlockPos pos, int tries) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();

        for (int l = 0; l <= 1; ++l) {
            int i1 = i - Integer.compare(pos.getX(), 0) * l - 1;
            int j1 = k - Integer.compare(pos.getZ(), 0) * l - 1;
            int k1 = i1 + 2;
            int l1 = j1 + 2;

            for (int i2 = i1; i2 <= k1; ++i2) {
                for (int j2 = j1; j2 <= l1; ++j2) {
                    BlockPos blockpos = new BlockPos(i2, j, j2);

                    if (hasRoomForPlayer(worldIn, blockpos) || --tries <= 0)
                        return blockpos;
                }
            }
        }

        return pos;
    }

    private static boolean hasRoomForPlayer(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.down()).isTopSolid() && !worldIn.getBlockState(pos).getMaterial().isSolid() && !worldIn.getBlockState(pos.up()).getMaterial().isSolid();
    }

    private static int runSetHomeCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, EnumRank.LEADER))
            return 0;
        assert clan != null;
        Chunk c = context.getSource().asPlayer().getEntityWorld().getChunk(context.getSource().asPlayer().getPosition());
        if(clan.getClanId().equals(CapHelper.getClaimedLandCapability(c).getClan())) {
            for(Map.Entry<NewClan, BlockPos> pos: ClanCache.getClanHomes().entrySet())
                if(pos.getValue() != null && pos.getKey() != clan && pos.getValue().getDistance(context.getSource().asPlayer().getPosition().getX(), context.getSource().asPlayer().getPosition().getY(), context.getSource().asPlayer().getPosition().getZ()) < Clans.cfg.minClanHomeDist) {
                    throwCommandFailure("You are too close to another clan's home! You must be at least %s blocks away from other clans' homes to set your clan home. Use /clan fancymap to see where nearby clans are.", Clans.cfg.minClanHomeDist);
                    return 0;
                }
            clan.setHome(context.getSource().asPlayer().getPosition(), context.getSource().asPlayer().dimension.getId());
            sendFeedback(context, TextStyles.GREEN, "Clan home set!");
        } else
            throwCommandFailure("Clan home can only be set in clan territory!");
        return 1;
    }

    private static int runTrappedCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) throws CommandSyntaxException {
        if(!validateClanRank(context, clan, EnumRank.ANY))
            return 0;
        assert clan != null;
        Chunk origin = context.getSource().asPlayer().world.getChunk(context.getSource().asPlayer().getPosition());
        NewClan chunkOwner = ClanCache.getClanById(ChunkUtils.getChunkOwner(origin));
        if(chunkOwner == null && Clans.cfg.protectWilderness && context.getSource().asPlayer().getPosition().getY() >= Clans.cfg.minWildernessY) {
            BlockPos spawn = context.getSource().asPlayer().world.getSpawnPoint();
            context.getSource().asPlayer().attemptTeleport(spawn.getX(), spawn.getY(), spawn.getZ());
        } else if(chunkOwner != null && !chunkOwner.getMembers().containsKey(context.getSource().asPlayer().getUniqueID()) && (!RaidingParties.hasActiveRaid(chunkOwner) || !RaidingParties.getActiveRaid(chunkOwner).getAttackers().contains(context.getSource().asPlayer()))) {
            int x = 0, z = 0, tmp, dx = 0, dz = -1;
            while(true) {//Spiral out until a player friendly chunk is found
                Chunk test = context.getSource().asPlayer().world.getChunk(origin.x + x, origin.z + z);
                NewClan testChunkOwner = ClanCache.getClanById(ChunkUtils.getChunkOwner(test));
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
        } else
            throwCommandFailure("No, you're not trapped in someone else's claim.");
        return 1;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean validateClanRank(CommandContext<CommandSource> context, @Nullable NewClan clan, EnumRank requiredRank) {
        if(!(context.getSource().getEntity() instanceof EntityPlayerMP)) {
            throwCommandFailure("You must be a player to do this!");
            return false;
        }

        if(clan == null && !requiredRank.equals(EnumRank.ANY) && !requiredRank.equals(EnumRank.NOCLAN)) {
            throwCommandFailure("Invalid clan! Either you are not in a clan or the clan you typed does not exist!");
            return false;
        }

        if(clan != null && requiredRank.equals(EnumRank.NOCLAN)) {
            throwCommandFailure("You cannot do this because you are in a clan!");
            return false;
        }

        if(clan != null && !clan.getMembers().containsKey(context.getSource().getEntity().getUniqueID())) {
            throwCommandFailure("You are not in %s!", clan.getClanName());
            return false;
        }

        if(clan != null && !clan.getMembers().get(context.getSource().getEntity().getUniqueID()).greaterOrEquals(requiredRank)) {
            throwCommandFailure("You do not have permission to do this in %s!", clan.getClanName());
            return false;
        }

        return true;
    }

    private static void throwCommandFailure(String message, Object... args) throws CommandException {
        throw new CommandException(new TextComponentTranslation(message, args).setStyle(TextStyles.RED));
    }

    private static void sendFeedback(CommandContext<CommandSource> context, @Nullable Style color, String message, Object... args) throws CommandException {
        ITextComponent out = new TextComponentTranslation(message, args);
        if(color != null)
            out.setStyle(color);
        context.getSource().sendFeedback(out, false);
    }
}
