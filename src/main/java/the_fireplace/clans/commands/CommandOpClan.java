package the_fireplace.clans.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.*;
import the_fireplace.clans.util.CapHelper;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandOpClan {

    private static Predicate<CommandSource> isOp = commandSource -> commandSource.hasPermissionLevel(commandSource.getServer().getOpPermissionLevel());

    @SuppressWarnings("Duplicates")
    public static void register(CommandDispatcher<CommandSource> commandDispatcher) {
        LiteralArgumentBuilder<CommandSource> opclanCommand = Commands.literal("opclan").requires(isOp);

        opclanCommand.then(Commands.literal("claim")
                .executes(context -> runClaimCommand(context, NewClanDatabase.getOpClan(), false))
                .then(Commands.literal("force")
                .executes(context -> runClaimCommand(context, NewClanDatabase.getOpClan(), true))));
        opclanCommand.then(Commands.literal("c")
                .executes(context -> runClaimCommand(context, NewClanDatabase.getOpClan(), false))
                .then(Commands.literal("force")
                .executes(context -> runClaimCommand(context, NewClanDatabase.getOpClan(), true))));
        opclanCommand.then(Commands.literal("claim")
                .then(Commands.argument("clan", StringArgumentType.word()).suggests(CommandClan.clanSuggestion)
                .executes(context -> runClaimCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)), false))
                .then(Commands.literal("force")
                .executes(context -> runClaimCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)), true)))));
        opclanCommand.then(Commands.literal("c")
                .then(Commands.argument("clan", StringArgumentType.word()).suggests(CommandClan.clanSuggestion)
                .executes(context -> runClaimCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)), false))
                .then(Commands.literal("force")
                .executes(context -> runClaimCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)), true)))));

        opclanCommand.then(Commands.literal("abandonclaim")
                .executes(context -> runAbandonClaimCommand(context, false))
                .then(Commands.literal("force")
                .executes(context -> runAbandonClaimCommand(context, true))));
        opclanCommand.then(Commands.literal("ac")
                .executes(context -> runAbandonClaimCommand(context, false))
                .then(Commands.literal("force")
                .executes(context -> runAbandonClaimCommand(context, true))));

        opclanCommand.then(Commands.literal("addfunds")
                .then(Commands.argument("clan", StringArgumentType.word()).suggests(CommandClan.clanSuggestion)
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .executes(context -> runAddFundsCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))))));
        opclanCommand.then(Commands.literal("af")
                .then(Commands.argument("clan", StringArgumentType.word()).suggests(CommandClan.clanSuggestion)
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .executes(context -> runAddFundsCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))))));

        opclanCommand.then(Commands.literal("promote")
                .then(Commands.argument("clan", StringArgumentType.word()).suggests(CommandClan.clanSuggestion)
                .then(Commands.argument("target", StringArgumentType.word())
                .executes(context -> runPromoteCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))))));

        opclanCommand.then(Commands.literal("demote")
                .then(Commands.argument("clan", StringArgumentType.word()).suggests(CommandClan.clanSuggestion)
                .then(Commands.argument("target", StringArgumentType.word())
                .executes(context -> runDemoteCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))))));

        opclanCommand.then(Commands.literal("buildadmin")
                .executes(CommandOpClan::runBuildAdminCommand));
        opclanCommand.then(Commands.literal("ba")
                .executes(CommandOpClan::runBuildAdminCommand));

        opclanCommand.then(Commands.literal("disband")
                .then(Commands.argument("clan", StringArgumentType.word()).suggests(CommandClan.clanSuggestion)
                .executes(context -> runDisbandCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class))))));

        opclanCommand.then(Commands.literal("kick")
                .then(Commands.argument("clan", StringArgumentType.word()).suggests(CommandClan.clanSuggestion)
                .then(Commands.argument("target", StringArgumentType.word())
                .executes(context -> runKickCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))))));

        opclanCommand.then(Commands.literal("setdescription")
                .then(Commands.argument("description", StringArgumentType.greedyString())
                .executes(context -> runSetDescriptionCommand(context, NewClanDatabase.getOpClan()))));
        opclanCommand.then(Commands.literal("setdesc")
                .then(Commands.argument("description", StringArgumentType.greedyString())
                .executes(context -> runSetDescriptionCommand(context, NewClanDatabase.getOpClan()))));
        opclanCommand.then(Commands.argument("clan", StringArgumentType.word()).suggests(CommandClan.clanSuggestion)
                .then(Commands.literal("setdescription")
                .then(Commands.argument("description", StringArgumentType.greedyString())
                .executes(context -> runSetDescriptionCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))))));
        opclanCommand.then(Commands.argument("clan", StringArgumentType.word()).suggests(CommandClan.clanSuggestion)
                .then(Commands.literal("setdesc")
                .then(Commands.argument("description", StringArgumentType.greedyString())
                .executes(context -> runSetDescriptionCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))))));

        opclanCommand.then(Commands.literal("setcolor")
                .then(Commands.argument("color", StringArgumentType.word())
                .executes(context -> runSetColorCommand(context, NewClanDatabase.getOpClan()))));
        opclanCommand.then(Commands.literal("setcolour")
                .then(Commands.argument("color", StringArgumentType.word())
                .executes(context -> runSetColorCommand(context, NewClanDatabase.getOpClan()))));
        opclanCommand.then(Commands.argument("clan", StringArgumentType.word()).suggests(CommandClan.clanSuggestion)
                .then(Commands.literal("setcolor")
                .then(Commands.argument("color", StringArgumentType.word())
                .executes(context -> runSetColorCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))))));
        opclanCommand.then(Commands.argument("clan", StringArgumentType.word()).suggests(CommandClan.clanSuggestion)
                .then(Commands.literal("setcolour")
                .then(Commands.argument("color", StringArgumentType.word())
                .executes(context -> runSetColorCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))))));

        opclanCommand.then(Commands.literal("setname")
                .then(Commands.argument("name", StringArgumentType.word())
                .executes(context -> runSetNameCommand(context, NewClanDatabase.getOpClan()))));
        opclanCommand.then(Commands.literal("setname")
                .then(Commands.argument("clan", StringArgumentType.word()).suggests(CommandClan.clanSuggestion)
                .then(Commands.argument("name", StringArgumentType.word())
                .executes(context -> runSetNameCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))))));

        opclanCommand.then(Commands.literal("setshield")
                .then(Commands.argument("clan", StringArgumentType.word()).suggests(CommandClan.clanSuggestion)
                .then(Commands.argument("duration",IntegerArgumentType.integer(0))
                .executes(context -> runSetShieldCommand(context, ClanCache.getClanByName(context.getArgument("clan", String.class)))))));

        LiteralCommandNode<CommandSource> opclanNode = commandDispatcher.register(opclanCommand);
        commandDispatcher.register(Commands.literal("oc").redirect(opclanNode));
    }

    private static int runClaimCommand(CommandContext<CommandSource> context, @Nullable NewClan clan, boolean force) throws CommandSyntaxException {
        if(!validateClan(context, clan, true, false))
            return 0;
        assert clan != null;
        Chunk c = context.getSource().asPlayer().getEntityWorld().getChunk(context.getSource().asPlayer().getPosition());
        UUID chunkOwner = ChunkUtils.getChunkOwner(c);
        NewClan chunkOwnerClan = chunkOwner != null ? ClanCache.getClanById(chunkOwner) : null;
        if(chunkOwner != null && chunkOwnerClan != null && (!force || chunkOwner.equals(clan.getClanId()))) {
            if(chunkOwner.equals(clan.getClanId()))
                throwCommandFailure("%s has already claimed this land.", clan.getClanName());
            else
                throwCommandFailure("Another clan (%1$s) has already claimed this land. To take this land from %1$s, use /opclan claim [clan] force.", chunkOwnerClan.getClanName());
        } else {
            if(chunkOwnerClan != null) {
                chunkOwnerClan.subClaimCount();
                Clans.getPaymentHandler().addAmount(Clans.cfg.claimChunkCost, chunkOwnerClan.getClanId());
            }
            if(clan.isOpclan()) {
                ChunkUtils.setChunkOwner(c, clan.getClanId());
                ClanChunkCache.addChunk(clan, c.x, c.z, c.getWorld().getDimension().getType().getId());
                clan.addClaimCount();
                sendFeedback(context, TextStyles.GREEN, "Land claimed for %s!", clan.getClanName());
            } else {
                if(force || !Clans.cfg.forceConnectedClaims || ChunkUtils.hasConnectedClaim(c, clan.getClanId()) || clan.getClaimCount() == 0) {
                    if(force || Clans.cfg.maxClanPlayerClaims <= 0 || clan.getClaimCount() < clan.getMaxClaimCount()) {
                        if (force || Clans.getPaymentHandler().deductAmount(Clans.cfg.claimChunkCost, clan.getClanId())) {
                            ChunkUtils.setChunkOwner(c, clan.getClanId());
                            ClanChunkCache.addChunk(clan, c.x, c.z, c.getWorld().getDimension().getType().getId());
                            clan.addClaimCount();
                            sendFeedback(context, TextStyles.GREEN, "Land claimed for %s!", clan.getClanName());
                        } else
                            throwCommandFailure("Insufficient funds in %s's account to claim chunk. It costs %s %s.", clan.getClanName(), Clans.cfg.claimChunkCost, Clans.getPaymentHandler().getCurrencyName(Clans.cfg.claimChunkCost));
                    } else
                        throwCommandFailure("%s is already at or above its max claim count of %s.", clan.getClanName(), clan.getMaxClaimCount());
                } else
                    throwCommandFailure("You cannot claim this chunk of land because it is not next to another of %s's claims.", clan.getClanName());
            }
        }
        return 1;
    }

    private static int runAbandonClaimCommand(CommandContext<CommandSource> context, boolean force) throws CommandSyntaxException {
        if(!(context.getSource().getEntity() instanceof EntityPlayerMP)) {
            throwCommandFailure("You must be a player to do this!");
            return 0;
        }
        NewClan opClan = NewClanDatabase.getOpClan();
        Chunk c = context.getSource().asPlayer().getEntityWorld().getChunk(context.getSource().asPlayer().getPosition());
        UUID claimFaction = CapHelper.getClaimedLandCapability(c).getClan();
        if(claimFaction != null) {
            NewClan targetClan = ClanCache.getClanById(claimFaction);
            if(claimFaction.equals(opClan.getClanId()) || force || targetClan == null) {
                if(targetClan != null) {
                    if (force || targetClan.isOpclan() || !Clans.cfg.forceConnectedClaims || !ChunkUtils.hasConnectedClaim(c, targetClan.getClanId())) {
                        CommandClan.abandonClaim(context.getSource().asPlayer(), c, targetClan);
                        ChunkUtils.clearChunkOwner(c);
                        sendFeedback(context, TextStyles.GREEN, "Claim abandoned!");
                    } else {//We are forcing connected claims and there is a claim connected
                        //Prevent creation of disconnected claims
                        CommandClan.abandonClaimWithAdjacencyCheck(context, c, targetClan);
                    }
                } else {
                    ChunkUtils.clearChunkOwner(c);
                    sendFeedback(context, TextStyles.GREEN, "Claim abandoned!");
                }
            } else
                throwCommandFailure("This land does not belong to %s. To force %s to abandon it, use /opclan abandonclaim force", opClan.getClanName(), targetClan.getClanName());
        } else
            throwCommandFailure("This land is not claimed.");
        return 1;
    }

    private static int runAddFundsCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) {
        if(!validateClan(context, clan, false, true))
            return 0;
        assert clan != null;
        long amount = context.getArgument("amount", Integer.class);
        if(Clans.getPaymentHandler().addAmount(amount, clan.getClanId()))
            sendFeedback(context, TextStyles.GREEN, "Successfully added %s %s to %s's balance.", amount, Clans.getPaymentHandler().getCurrencyName(amount), clan.getClanName());
        else
            throwCommandFailure("Internal error: Clan account for %s not found.", clan.getClanName());
        return 1;
    }

    private static int runBuildAdminCommand(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if(!(context.getSource().getEntity() instanceof EntityPlayerMP)) {
            throwCommandFailure("You must be a player to do this!");
            return 0;
        }
        if(ClanCache.toggleClaimAdmin(context.getSource().assertIsEntity().getUniqueID()))
            sendFeedback(context, TextStyles.YELLOW, "You are now in Build Admin mode.");
        else
            sendFeedback(context, TextStyles.GREEN, "You are no longer in Build Admin mode.");
        return 1;
    }

    private static int runDemoteCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) {
        if(!validateClan(context, clan, false, true))
            return 0;
        assert clan != null;
        CommandClan.demotePlayer(context, clan);
        return 1;
    }

    private static int runPromoteCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) {
        if(!validateClan(context, clan, false, true))
            return 0;
        assert clan != null;
        CommandClan.promotePlayer(context, clan);
        return 1;
    }

    private static int runDisbandCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) throws CommandSyntaxException {
        if(!validateClan(context, clan, false, true))
            return 0;
        assert clan != null;
        CommandClan.runDisbandCommand(context, clan, true);
        return 1;
    }

    private static int runKickCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) {
        if(!validateClan(context, clan, false, true))
            return 0;
        assert clan != null;
        String playerName = context.getArgument("target", String.class);
        GameProfile target = ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache().getGameProfileForUsername(playerName);

        if(target != null) {
            if (ClanCache.getClansByPlayer(target.getId()).contains(clan)) {
                CommandClan.removeMember(context, clan, target);
            } else
                throwCommandFailure("The player %s is not in %s.", target.getName(), clan.getClanName());
        } else
            throwCommandFailure("The player %s was not found.", playerName);
        return 1;
    }

    private static int runSetDescriptionCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) {
        if(!validateClan(context, clan, false, false))
            return 0;
        assert clan != null;
        String newDescription = context.getArgument("description", String.class);
        clan.setDescription(newDescription);
        sendFeedback(context, TextStyles.GREEN, "%s description set!", clan.getClanName());
        return 1;
    }

    private static int runSetNameCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) {
        if(!validateClan(context, clan, false, false))
            return 0;
        assert clan != null;
        String newName = context.getArgument("name", String.class);
        if(!ClanCache.clanNameTaken(newName)) {
            String oldName = clan.getClanName();
            clan.setClanName(newName);
            sendFeedback(context, TextStyles.GREEN, "%s renamed to %s!", oldName, newName);
        } else
            throwCommandFailure("The clan name \"%s\" is already taken or invalid.", newName);
        return 1;
    }

    private static int runSetColorCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) {
        if(!validateClan(context, clan, false, false))
            return 0;
        assert clan != null;
        String newColor = context.getArgument("color", String.class);
        try {
            clan.setColor(newColor.startsWith("0x") ? Integer.parseInt(newColor.substring(2), 16) : Integer.parseInt(newColor));
            sendFeedback(context, TextStyles.GREEN, "Clan color for %s set!", clan.getClanName());
        } catch (NumberFormatException e) {
            throwCommandFailure("Invalid color integer: %s", newColor);
        }
        return 1;
    }

    private static int runSetShieldCommand(CommandContext<CommandSource> context, @Nullable NewClan clan) {
        if(!validateClan(context, clan, false, true))
            return 0;
        assert clan != null;
        long duration = context.getArgument("duration", Integer.class);
        clan.setShield(duration);
        sendFeedback(context, TextStyles.GREEN, "Clan shield for %s set to %s minutes!", clan.getClanName(), duration);
        return 1;
    }

    private static boolean validateClan(CommandContext<CommandSource> context, @Nullable NewClan selectedClan, boolean requiresPlayer, boolean denyOpclan) {
        if(requiresPlayer && !(context.getSource().getEntity() instanceof EntityPlayerMP)) {
            throwCommandFailure("You must be a player to do this!");
            return false;
        }

        if(selectedClan == null) {
            throwCommandFailure("The clan name you have selected is invalid!");
            return false;
        }

        if(denyOpclan && selectedClan.isOpclan()) {
            throwCommandFailure("You cannot select opclan for this!");
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
