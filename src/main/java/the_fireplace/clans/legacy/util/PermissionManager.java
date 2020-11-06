package the_fireplace.clans.legacy.util;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import the_fireplace.clans.legacy.ClansModContainer;

public final class PermissionManager {

    public static final String CLAN_COMMAND_PREFIX = "command.clans.clan.";
    public static final String OPCLAN_COMMAND_PREFIX = "command.clans.opclan.";
    public static final String RAID_COMMAND_PREFIX = "command.clans.raid.";
    public static final String PROTECTION_PREFIX = "clans.protection.";

    public static boolean hasPermission(EntityPlayerMP player, String permissionKey) {
        if(ClansModContainer.getPermissionManager() != null)
            return ClansModContainer.getPermissionManager().hasPermission(player, permissionKey);
        else
            return true;
    }

    public static boolean hasPermission(ICommandSender sender, String permissionKey) {
        if(sender instanceof EntityPlayerMP)
            return hasPermission((EntityPlayerMP)sender, permissionKey);
        return true;
    }

    public static boolean permissionManagementExists() {
        return ClansModContainer.getPermissionManager().permissionManagementExists();
    }
}
