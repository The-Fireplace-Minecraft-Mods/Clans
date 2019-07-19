package the_fireplace.clans.util;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import the_fireplace.clans.Clans;
import the_fireplace.clans.abstraction.IPermissionHandler;
import the_fireplace.clans.compat.SpongePermissionHandler;
import the_fireplace.clans.forge.ForgePermissionHandler;

public final class PermissionManager {

    public static final String CLAN_COMMAND_PREFIX = "command.clans.clan.";
    public static final String OPCLAN_COMMAND_PREFIX = "command.clans.opclan.";
    public static final String RAID_COMMAND_PREFIX = "command.clans.raid.";
    public static final String PROTECTION_PREFIX = "clans.protection.";

    private static IPermissionHandler permissionManager;

    public static void registerPermissionHandlers() {//TODO this still needs abstraction
        if(Clans.getMinecraftHelper().isPluginLoaded("spongeapi") && !Clans.getConfig().isForgePermissionPrecedence())
            permissionManager = new SpongePermissionHandler();
        else
            permissionManager = new ForgePermissionHandler();
    }

    public static boolean hasPermission(EntityPlayerMP player, String permissionKey) {
        if(permissionManager != null)
            return permissionManager.hasPermission(player, permissionKey);
        else
            return true;
    }

    public static boolean hasPermission(ICommandSender sender, String permissionKey) {
        if(sender instanceof EntityPlayerMP)
            return hasPermission((EntityPlayerMP)sender, permissionKey);
        return true;
    }
}
