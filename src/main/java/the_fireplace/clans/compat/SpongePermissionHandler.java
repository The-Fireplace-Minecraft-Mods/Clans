package the_fireplace.clans.compat;

import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import the_fireplace.clans.abstraction.IPermissionHandler;
import the_fireplace.clans.ClansForge;

import static the_fireplace.clans.util.PermissionManager.*;

public class SpongePermissionHandler implements IPermissionHandler {

    private boolean hasPermissionPlugin;
    private PermissionService permissionService;

    public SpongePermissionHandler() {
        if(!Sponge.getServiceManager().provide(PermissionService.class).isPresent())
            return;
        permissionService = Sponge.getServiceManager().provide(PermissionService.class).get();
        hasPermissionPlugin = true;

        registerPermission(CLAN_COMMAND_PREFIX+"help", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"banner", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"details", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"disband", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"form", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"list", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"playerinfo", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"setbanner", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"setcolor", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"setdefault", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"setdescription", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"setname", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"addfunds", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"balance", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"finances", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"setrent", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"takefunds", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"abandonclaim", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"claim", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"fancymap", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"map", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"accept", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"decline", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"demote", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"invite", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"kick", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"leave", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"promote", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"home", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"sethome", PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"trapped", PermissionDescription.ROLE_USER, "");

        registerPermission(OPCLAN_COMMAND_PREFIX+"help", PermissionDescription.ROLE_ADMIN, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"abandonclaim", PermissionDescription.ROLE_ADMIN, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"addfunds", PermissionDescription.ROLE_ADMIN, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"buildadmin", PermissionDescription.ROLE_ADMIN, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"claim", PermissionDescription.ROLE_ADMIN, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"demote", PermissionDescription.ROLE_ADMIN, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"disband", PermissionDescription.ROLE_ADMIN, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"kick", PermissionDescription.ROLE_ADMIN, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"promote", PermissionDescription.ROLE_ADMIN, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"setcolor", PermissionDescription.ROLE_ADMIN, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"setdescription", PermissionDescription.ROLE_ADMIN, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"setname", PermissionDescription.ROLE_ADMIN, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"setshield", PermissionDescription.ROLE_ADMIN, "");

        registerPermission(RAID_COMMAND_PREFIX+"help", PermissionDescription.ROLE_USER, "");
        registerPermission(RAID_COMMAND_PREFIX+"collect", PermissionDescription.ROLE_USER, "");
        registerPermission(RAID_COMMAND_PREFIX+"invite", PermissionDescription.ROLE_USER, "");
        registerPermission(RAID_COMMAND_PREFIX+"join", PermissionDescription.ROLE_USER, "");
        registerPermission(RAID_COMMAND_PREFIX+"leave", PermissionDescription.ROLE_USER, "");
        registerPermission(RAID_COMMAND_PREFIX+"start", PermissionDescription.ROLE_USER, "");

        registerPermission(PROTECTION_PREFIX+"break.protected_wilderness", "", "");
        registerPermission(PROTECTION_PREFIX+"build.protected_wilderness", "", "");
    }

    @Override
    public boolean hasPermission(EntityPlayerMP player, String permissionName) {
        if(hasPermissionPlugin && player instanceof Subject)
            return ((Subject) player).hasPermission(permissionName);
        return true;
    }

    @Override
    public void registerPermission(String permissionName, Object permissionLevel, String permissionDescription) {
        permissionService
                .newDescriptionBuilder(ClansForge.instance)
                .id(permissionName)
                .description(Text.of(permissionDescription))
                .assign(((String)permissionLevel).isEmpty() ? PermissionDescription.ROLE_USER : (String)permissionLevel, !((String) permissionLevel).isEmpty())
                .register();
    }
}
