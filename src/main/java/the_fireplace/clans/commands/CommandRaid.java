package the_fireplace.clans.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.raid.Raid;
import the_fireplace.clans.raid.RaidBlockPlacementDatabase;
import the_fireplace.clans.raid.RaidingParties;
import the_fireplace.clans.util.TextStyles;

import java.util.HashMap;
import java.util.List;

public class CommandRaid {

    public static final SuggestionProvider<CommandSource> targetableClanSuggestion = (context, builder) -> {
        for(Clan c: ClanDatabase.getClans())
            if(!c.isShielded() && !c.isOpclan() && !RaidingParties.hasActiveRaid(c) && !RaidingParties.isPreparingRaid(c) && !c.getMembers().containsKey(context.getSource().asPlayer().getUniqueID()))
                builder.suggest(c.getClanName());
        return builder.buildFuture();
    };

    public static final SuggestionProvider<CommandSource> invitablePlayerSuggestion = (context, builder) -> {
        for(EntityPlayerMP p: ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
            if(!ClanCache.getPlayerClans(p.getUniqueID()).contains(RaidingParties.getRaid(context.getSource().asPlayer()).getTarget()) && RaidingParties.getRaid(p) == null)
                builder.suggest(p.getName().getFormattedText());
        return builder.buildFuture();
    };

    @SuppressWarnings("Duplicates")
    public static void register(CommandDispatcher<CommandSource> commandDispatcher) {
        LiteralArgumentBuilder<CommandSource> raidCommand = Commands.literal("raid").requires((iCommandSender) -> iCommandSender.getEntity() instanceof EntityPlayerMP);

        ArgumentBuilder<CommandSource, ?> joinArgs =
                      Commands.argument("target", StringArgumentType.word()).suggests(targetableClanSuggestion)
                .executes(joinCommand);
        ArgumentBuilder<CommandSource, ?> inviteArgs =
                      Commands.argument("player", EntityArgument.player()).suggests(invitablePlayerSuggestion)
                .executes(inviteCommand);

        raidCommand.then(Commands.literal("join").then(joinArgs));
        raidCommand.then(Commands.literal("j").then(joinArgs));
        raidCommand.then(Commands.literal("invite").then(inviteArgs));
        raidCommand.then(Commands.literal("i").then(inviteArgs));

        raidCommand.then(Commands.literal("collect").executes(collectCommand));
        raidCommand.then(Commands.literal("c").executes(collectCommand));
        raidCommand.then(Commands.literal("leave").executes(leaveCommand));
        raidCommand.then(Commands.literal("start").executes(startCommand));

        LiteralCommandNode<CommandSource> raidNode = commandDispatcher.register(raidCommand);
        commandDispatcher.register(Commands.literal("r").redirect(raidNode));
    }

    private static final Command<CommandSource> joinCommand = context -> {
        Clan target = ClanCache.getClan(context.getArgument("target", String.class));
        if(target == null)
            throwCommandFailure("Target clan not found.");
        else {
            if(!RaidingParties.getRaidingPlayers().contains(context.getSource().assertIsEntity().getUniqueID())) {
                Raid raid = RaidingParties.getRaid(target);
                HashMap<EntityPlayerMP, EnumRank> clanPlayers = raid.getTarget().getOnlineMembers();
                if(!clanPlayers.containsKey(context.getSource().asPlayer())) {
                    if (!RaidingParties.getRaidedClans().contains(target)) { //Form a new raid because one doesn't exist
                        if(!target.isShielded()) {
                            if (target.getOnlineMembers().size() > 0) {
                                new Raid(context.getSource().assertIsEntity().getUniqueID(), target);
                                sendFeedback(context, TextStyles.GREEN,"You successfully created the raiding party against %s!", target.getClanName());
                            } else
                                throwCommandFailure("Target clan has no online members!");
                        } else
                            throwCommandFailure("Target clan is currently shielded! Try again in %s hours.", Math.round(100f*target.getShield()*60)/100f);
                    } else { //Join an existing raid
                        if(clanPlayers.size() + Clans.cfg.maxRaidersOffset > raid.getMemberCount()) {
                            raid.addMember(context.getSource().assertIsEntity().getUniqueID());
                            sendFeedback(context, TextStyles.GREEN,"You successfully joined the raiding party against %s!", target.getClanName());
                        } else
                            throwCommandFailure("Target raiding party cannot hold any more people! It has %s raiders and the limit is currently %s.", raid.getMemberCount(), clanPlayers.size() + Clans.cfg.maxRaidersOffset);
                    }
                } else
                    throwCommandFailure("You cannot raid your own clan!");
            } else
                throwCommandFailure("You are already in a raiding party!");
        }
        return 1;
    };

    private static final Command<CommandSource> collectCommand = context -> {
        if(RaidBlockPlacementDatabase.hasPlacedBlocks(context.getSource().asPlayer().getUniqueID())){
            List<String> removeItems = Lists.newArrayList();
            for(String string: RaidBlockPlacementDatabase.getPlacedBlocks(context.getSource().asPlayer().getUniqueID())) {
                ItemStack stack;
                try {
                    stack = ItemStack.read(JsonToNBT.getTagFromJson(string));
                } catch (CommandException e) {
                    stack = null;
                }
                if (stack == null || context.getSource().asPlayer().addItemStackToInventory(stack))
                    removeItems.add(string);
            }
            RaidBlockPlacementDatabase.getInstance().removePlacedBlocks(context.getSource().asPlayer().getUniqueID(), removeItems);
            if(RaidBlockPlacementDatabase.hasPlacedBlocks(context.getSource().asPlayer().getUniqueID()))
                sendFeedback(context, TextStyles.YELLOW,"You have run out of room for collection. Make room in your inventory and try again.");
            else
                sendFeedback(context, TextStyles.GREEN,"Collection successful.");
        } else
            throwCommandFailure("You don't have anything to collect.");
        return 1;
    };

    private static final Command<CommandSource> inviteCommand = context -> {
        if(!RaidingParties.getRaidingPlayers().contains(context.getSource().assertIsEntity().getUniqueID())) {
            Raid raid = RaidingParties.getRaid(context.getSource().asPlayer());
            if (raid != null) {
                EntityPlayerMP targetPlayer = EntityArgument.getPlayer(context, "player");
                HashMap<EntityPlayerMP, EnumRank> clanPlayers = raid.getTarget().getOnlineMembers();
                if (clanPlayers.size() > raid.getMemberCount() - Clans.cfg.maxRaidersOffset) {
                    if (!clanPlayers.containsKey(targetPlayer)) {
                        targetPlayer.sendMessage(new TextComponentTranslation("You have been invited to a raiding party against %1$s! To join, type /raid join %1$s", raid.getTarget().getClanName()).setStyle(TextStyles.GREEN));
                        sendFeedback(context, TextStyles.GREEN,"You successfully invited %s to the raiding party!", targetPlayer.getName());
                    } else
                        throwCommandFailure("You cannot invite someone to raid their own clan!");
                } else
                    throwCommandFailure("Your raiding party cannot hold any more people! It has %s raiders and the limit is currently %s.", raid.getMemberCount(), clanPlayers.size() + Clans.cfg.maxRaidersOffset);
            } else//Internal error because we should not reach this point
                throwCommandFailure("Internal error: You are not in a raiding party!");
        } else
            throwCommandFailure("You are not in a raiding party!");
        return 1;
    };

    private static final Command<CommandSource> leaveCommand = context -> {
        if(RaidingParties.getRaidingPlayers().contains(context.getSource().assertIsEntity().getUniqueID())) {
            Raid raid = RaidingParties.getRaid(context.getSource().asPlayer());
            if (raid != null) {
                raid.removeMember(context.getSource().assertIsEntity().getUniqueID());
                sendFeedback(context, TextStyles.GREEN,"You successfully left the raiding party against %s!", raid.getTarget().getClanName());
            } else//Internal error because we should not reach this point
                throwCommandFailure("Internal error: You are not in a raiding party!");
        } else
            throwCommandFailure("You are not in a raiding party!");
        return 1;
    };

    private static final Command<CommandSource> startCommand = context -> {
        if(RaidingParties.getRaidingPlayers().contains(context.getSource().assertIsEntity().getUniqueID())) {
            Raid raid = RaidingParties.getRaid(context.getSource().asPlayer());
            if (raid != null) {
                HashMap<EntityPlayerMP, EnumRank> clanPlayers = raid.getTarget().getOnlineMembers();
                if(clanPlayers.size() + Clans.cfg.maxRaidersOffset >= raid.getMemberCount()) {
                    if(!RaidingParties.hasActiveRaid(raid.getTarget())) {
                        if(!RaidingParties.isPreparingRaid(raid.getTarget())) {
                            long raidCost = Clans.cfg.startRaidCost;
                            if (Clans.cfg.startRaidMultiplier)
                                raidCost *= raid.getTarget().getClaimCount();
                            raid.setCost(raidCost);
                            if (Clans.getPaymentHandler().deductAmount(raidCost, context.getSource().asPlayer().getUniqueID())) {
                                RaidingParties.initRaid(raid.getTarget());
                                sendFeedback(context, TextStyles.GREEN,"You successfully started the raid against %s!", raid.getTarget().getClanName());
                            } else
                                throwCommandFailure("You have insufficient funds to start the raid against %s. It costs %s %s.", raid.getTarget().getClanName(), raidCost, Clans.getPaymentHandler().getCurrencyName(raidCost));
                        } else
                            throwCommandFailure("You have already started this raid!");
                    } else//This should not be possible
                        throwCommandFailure("Internal error: Another raiding party is raiding this clan right now. Try again in %s hours.", Math.round(100f*(Clans.cfg.defenseShield*60f*60f+raid.getRemainingSeconds())/60f/60f)/100f);
                } else
                    throwCommandFailure("Your raiding party has too many people! It has %s raiders and the limit is currently %s.", raid.getMemberCount(), clanPlayers.size() + Clans.cfg.maxRaidersOffset);
            } else//Internal error because we should not reach this point
                throwCommandFailure("Internal error: You are not in a raiding party!");
        } else
            throwCommandFailure("You are not in a raiding party!");
        return 1;
    };

    private static void throwCommandFailure(String message, Object... args) throws CommandException {
        throw new CommandException(new TextComponentTranslation(message, args).setStyle(TextStyles.RED));
    }

    private static void sendFeedback(CommandContext<CommandSource> context, Style color, String message, Object... args) throws CommandException {
        context.getSource().sendFeedback(new TextComponentTranslation(message, args).setStyle(color), false);
    }
}
