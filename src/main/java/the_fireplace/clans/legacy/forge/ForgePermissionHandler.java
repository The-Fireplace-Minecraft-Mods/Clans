package the_fireplace.clans.legacy.forge;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import the_fireplace.clans.legacy.abstraction.IPermissionHandler;
import the_fireplace.clans.legacy.commands.CommandClan;
import the_fireplace.clans.legacy.commands.CommandOpClan;
import the_fireplace.clans.legacy.commands.CommandRaid;

import static the_fireplace.clans.legacy.util.PermissionManager.*;

public class ForgePermissionHandler implements IPermissionHandler {

    public ForgePermissionHandler() {
        for(String subcommand: CommandClan.COMMANDS.keySet())
            registerPermission(CLAN_COMMAND_PREFIX+subcommand, DefaultPermissionLevel.ALL, "");

        for(String subcommand: CommandRaid.COMMANDS.keySet())
            registerPermission(RAID_COMMAND_PREFIX+subcommand, DefaultPermissionLevel.ALL, "");

        for(String subcommand: CommandOpClan.COMMANDS.keySet())
            registerPermission(OPCLAN_COMMAND_PREFIX+subcommand, DefaultPermissionLevel.OP, "");

        registerPermission(PROTECTION_PREFIX+"break.protected_wilderness", DefaultPermissionLevel.NONE, "");
        registerPermission(PROTECTION_PREFIX+"build.protected_wilderness", DefaultPermissionLevel.NONE, "");

        registerPermission(CLAN_COMMAND_PREFIX+"claim.radius", DefaultPermissionLevel.ALL, "");
    }

    @Override
    public boolean hasPermission(EntityPlayerMP player, String permissionName) {
        return PermissionAPI.hasPermission(player, permissionName);
    }

    @Override
    public void registerPermission(String permissionName, Object permissionLevel, String permissionDescription) {
        PermissionAPI.registerNode(permissionName, (DefaultPermissionLevel)permissionLevel, permissionDescription);
    }

    @Override
    public boolean permissionManagementExists() {
        return true;
    }
}
