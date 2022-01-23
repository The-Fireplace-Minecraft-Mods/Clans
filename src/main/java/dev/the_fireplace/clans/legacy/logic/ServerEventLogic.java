package dev.the_fireplace.clans.legacy.logic;

import dev.the_fireplace.clans.clan.LegacyClanImporter;
import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.clan.ClanIdRegistry;
import dev.the_fireplace.clans.legacy.clan.ClanSaver;
import dev.the_fireplace.clans.legacy.commands.CommandClan;
import dev.the_fireplace.clans.legacy.commands.CommandOpClan;
import dev.the_fireplace.clans.legacy.commands.CommandRaid;
import dev.the_fireplace.clans.legacy.data.ClaimData;
import dev.the_fireplace.clans.legacy.data.RaidCollectionDatabase;
import dev.the_fireplace.clans.legacy.data.RaidRestoreDatabase;
import dev.the_fireplace.clans.legacy.player.InvitedPlayers;
import dev.the_fireplace.clans.legacy.player.PlayerDataStorage;
import dev.the_fireplace.clans.multithreading.ConcurrentExecutionManager;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;

public class ServerEventLogic
{
    public static void onServerStarting(MinecraftServer server) {
        ConcurrentExecutionManager.startExecutors();
        LegacyClanImporter.importLegacyClans();
        InvitedPlayers.loadInvitedPlayers();
        ICommandManager command = server.getCommandManager();
        ServerCommandManager manager = (ServerCommandManager) command;
        manager.registerCommand(new CommandClan());
        manager.registerCommand(new CommandOpClan());
        manager.registerCommand(new CommandRaid());
        ClansModContainer.getDynmapCompat().serverStart();
    }

    public static void onServerStopping() {
        ClanIdRegistry.saveInstance();
        ClaimData.INSTANCE.save();
        ClanSaver.saveAll();
        RaidRestoreDatabase.getInstance().save();
        RaidCollectionDatabase.getInstance().save();
        PlayerDataStorage.save();
        try {
            ConcurrentExecutionManager.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
