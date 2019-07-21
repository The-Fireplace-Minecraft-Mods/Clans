package the_fireplace.clans.forge;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import the_fireplace.clans.abstraction.IPermissionHandler;

import static the_fireplace.clans.util.PermissionManager.*;

public class ForgePermissionHandler implements IPermissionHandler {

    public ForgePermissionHandler() {
        registerPermission(CLAN_COMMAND_PREFIX+"help", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"banner", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"details", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"disband", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"form", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"list", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"playerinfo", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"setbanner", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"setcolor", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"setdefault", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"setdescription", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"setname", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"addfunds", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"balance", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"finances", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"setrent", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"takefunds", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"abandonclaim", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"claim", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"fancymap", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"map", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"accept", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"decline", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"demote", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"invite", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"kick", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"leave", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"promote", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"home", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"sethome", DefaultPermissionLevel.ALL, "");
        registerPermission(CLAN_COMMAND_PREFIX+"trapped", DefaultPermissionLevel.ALL, "");

        registerPermission(OPCLAN_COMMAND_PREFIX+"help", DefaultPermissionLevel.OP, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"abandonclaim", DefaultPermissionLevel.OP, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"addfunds", DefaultPermissionLevel.OP, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"buildadmin", DefaultPermissionLevel.OP, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"claim", DefaultPermissionLevel.OP, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"demote", DefaultPermissionLevel.OP, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"disband", DefaultPermissionLevel.OP, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"kick", DefaultPermissionLevel.OP, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"promote", DefaultPermissionLevel.OP, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"setcolor", DefaultPermissionLevel.OP, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"setdescription", DefaultPermissionLevel.OP, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"setname", DefaultPermissionLevel.OP, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"setshield", DefaultPermissionLevel.OP, "");

        registerPermission(RAID_COMMAND_PREFIX+"help", DefaultPermissionLevel.ALL, "");
        registerPermission(RAID_COMMAND_PREFIX+"collect", DefaultPermissionLevel.ALL, "");
        registerPermission(RAID_COMMAND_PREFIX+"invite", DefaultPermissionLevel.ALL, "");
        registerPermission(RAID_COMMAND_PREFIX+"join", DefaultPermissionLevel.ALL, "");
        registerPermission(RAID_COMMAND_PREFIX+"leave", DefaultPermissionLevel.ALL, "");
        registerPermission(RAID_COMMAND_PREFIX+"start", DefaultPermissionLevel.ALL, "");

        registerPermission(PROTECTION_PREFIX+"break.protected_wilderness", DefaultPermissionLevel.NONE, "");
        registerPermission(PROTECTION_PREFIX+"build.protected_wilderness", DefaultPermissionLevel.NONE, "");
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
