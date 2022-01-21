package the_fireplace.clans.legacy.util;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import the_fireplace.clans.legacy.ClansModContainer;

public final class PermissionManager
{

    public static final String CLAN_COMMAND_PREFIX = "command.clans.clan.";
    public static final String OPCLAN_COMMAND_PREFIX = "command.clans.opclan.";
    public static final String RAID_COMMAND_PREFIX = "command.clans.raid.";
    public static final String PROTECTION_PREFIX = "clans.protection.";

    public static boolean hasPermission(EntityPlayerMP player, String permissionKey, boolean ifNoPermissionManager) {
        if (permissionManagementExists()) {
            return ClansModContainer.getPermissionManager().hasPermission(player, permissionKey);
        } else {
            return ifNoPermissionManager;
        }
    }

    public static boolean hasPermission(ICommandSender sender, String permissionKey, boolean ifNoPermissionManager) {
        if (sender instanceof EntityPlayerMP) {
            return hasPermission((EntityPlayerMP) sender, permissionKey, ifNoPermissionManager);
        }
        return ifNoPermissionManager;
    }

    public static boolean permissionManagementExists() {
        return ClansModContainer.getPermissionManager() != null
            && ClansModContainer.getPermissionManager().permissionManagementExists();
    }
}
