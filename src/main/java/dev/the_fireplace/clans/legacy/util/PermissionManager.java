package dev.the_fireplace.clans.legacy.util;

import dev.the_fireplace.clans.legacy.ClansModContainer;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PermissionManager
{

    public static final String CLAN_COMMAND_PREFIX = "command.clans.clan.";
    public static final String OPCLAN_COMMAND_PREFIX = "command.clans.opclan.";
    public static final String RAID_COMMAND_PREFIX = "command.clans.raid.";
    public static final String PROTECTION_PREFIX = "clans.protection.";

    public static boolean hasPermission(ServerPlayerEntity player, String permissionKey, boolean ifNoPermissionManager) {
        if (permissionManagementExists()) {
            return ClansModContainer.getPermissionManager().hasPermission(player, permissionKey);
        } else {
            return ifNoPermissionManager;
        }
    }

    public static boolean hasPermission(ICommandSender sender, String permissionKey, boolean ifNoPermissionManager) {
        if (sender instanceof ServerPlayerEntity) {
            return hasPermission((ServerPlayerEntity) sender, permissionKey, ifNoPermissionManager);
        }
        return ifNoPermissionManager;
    }

    public static boolean permissionManagementExists() {
        return ClansModContainer.getPermissionManager() != null
            && ClansModContainer.getPermissionManager().permissionManagementExists();
    }
}
