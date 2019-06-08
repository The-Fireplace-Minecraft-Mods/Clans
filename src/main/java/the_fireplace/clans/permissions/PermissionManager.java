package the_fireplace.clans.permissions;

import com.google.common.collect.Lists;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Loader;
import the_fireplace.clans.compat.sponge.SpongePermissionHandler;

import java.util.List;

public final class PermissionManager {

    public static final String CLAN_COMMAND_PREFIX = "command.clans.clan.";
    public static final String OPCLAN_COMMAND_PREFIX = "command.clans.opclan.";
    public static final String RAID_COMMAND_PREFIX = "command.clans.raid.";
    public static final String PROTECTION_PREFIX = "clans.protection.";

    private static List<IPermissionHandler> permissionManagers = Lists.newArrayList();

    public static void registerPermissionHandlers() {
        permissionManagers.add(new ForgePermissionHandler());
        if(Loader.isModLoaded("spongeapi"))
            permissionManagers.add(new SpongePermissionHandler());
    }

    public static boolean hasPermission(EntityPlayerMP player, String permissionKey) {
        for(IPermissionHandler perm: permissionManagers)
            if(!perm.hasPermission(player, permissionKey))
                return false;
        return true;
    }

    public static boolean hasPermission(ICommandSender sender, String permissionKey) {
        if(sender instanceof EntityPlayerMP)
            return hasPermission((EntityPlayerMP)sender, permissionKey);
        return true;
    }
}
