package the_fireplace.clans.logic;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.CommandClan;
import the_fireplace.clans.commands.CommandOpClan;
import the_fireplace.clans.commands.CommandRaid;
import the_fireplace.clans.data.*;
import the_fireplace.clans.multithreading.SaveExecutionManager;

public class ServerEventLogic {
    public static void onServerStarting(MinecraftServer server) {
        ClanCache.loadInvitedPlayers();
        ICommandManager command = server.getCommandManager();
        ServerCommandManager manager = (ServerCommandManager) command;
        manager.registerCommand(new CommandClan());
        manager.registerCommand(new CommandOpClan());
        manager.registerCommand(new CommandRaid());
        Clans.getDynmapCompat().serverStart();
    }

    public static void onServerStopping() {
        ClaimData.save();
        ClanDatabase.save();
        RaidRestoreDatabase.getInstance().save();
        RaidCollectionDatabase.getInstance().save();
        PlayerData.save();
        try {
            SaveExecutionManager.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
