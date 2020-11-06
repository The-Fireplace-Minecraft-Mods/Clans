package the_fireplace.clans.legacy.logic;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.commands.CommandClan;
import the_fireplace.clans.legacy.commands.CommandOpClan;
import the_fireplace.clans.legacy.commands.CommandRaid;
import the_fireplace.clans.legacy.data.ClaimData;
import the_fireplace.clans.legacy.data.RaidCollectionDatabase;
import the_fireplace.clans.legacy.data.RaidRestoreDatabase;
import the_fireplace.clans.multithreading.ConcurrentExecutionManager;
import the_fireplace.clans.player.InvitedPlayers;
import the_fireplace.clans.player.PlayerDataStorage;

public class ServerEventLogic {
    public static void onServerStarting(MinecraftServer server) {
        InvitedPlayers.loadInvitedPlayers();
        ICommandManager command = server.getCommandManager();
        ServerCommandManager manager = (ServerCommandManager) command;
        manager.registerCommand(new CommandClan());
        manager.registerCommand(new CommandOpClan());
        manager.registerCommand(new CommandRaid());
        ClansModContainer.getDynmapCompat().serverStart();
    }

    public static void onServerStopping() {
        ClaimData.save();
        ClanDatabase.save();
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
