package the_fireplace.clans.legacy.sponge;

import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.abstraction.IPermissionHandler;
import the_fireplace.clans.legacy.commands.CommandClan;
import the_fireplace.clans.legacy.commands.CommandOpClan;
import the_fireplace.clans.legacy.commands.CommandRaid;

import static the_fireplace.clans.legacy.util.PermissionManager.*;

public class SpongePermissionHandler implements IPermissionHandler {

    private PermissionService permissionService;

    public SpongePermissionHandler() {
        if(!Sponge.getServiceManager().provide(PermissionService.class).isPresent())
            return;
        permissionService = Sponge.getServiceManager().provide(PermissionService.class).get();

        for(String subcommand: CommandClan.commands.keySet())
            registerPermission(CLAN_COMMAND_PREFIX+subcommand, PermissionDescription.ROLE_USER, "");
        registerPermission(CLAN_COMMAND_PREFIX+"help", PermissionDescription.ROLE_USER, "");

        for(String subcommand: CommandOpClan.commands.keySet())
            registerPermission(OPCLAN_COMMAND_PREFIX+subcommand, PermissionDescription.ROLE_ADMIN, "");
        registerPermission(OPCLAN_COMMAND_PREFIX+"help", PermissionDescription.ROLE_ADMIN, "");

        for(String subcommand: CommandRaid.commands.keySet())
            registerPermission(RAID_COMMAND_PREFIX+subcommand, PermissionDescription.ROLE_USER, "");
        registerPermission(RAID_COMMAND_PREFIX+"help", PermissionDescription.ROLE_USER, "");

        registerPermission(PROTECTION_PREFIX+"break.protected_wilderness", "", "");
        registerPermission(PROTECTION_PREFIX+"build.protected_wilderness", "", "");

        registerPermission(CLAN_COMMAND_PREFIX+"claim.radius", PermissionDescription.ROLE_USER, "");
    }

    @Override
    public boolean hasPermission(EntityPlayerMP player, String permissionName) {
        if(permissionManagementExists() && player instanceof Subject)
            return ((Subject) player).hasPermission(permissionName);
        return true;
    }

    @Override
    public void registerPermission(String permissionName, Object permissionLevel, String permissionDescription) {
        permissionService
                .newDescriptionBuilder(ClansModContainer.instance)
                .id(permissionName)
                .description(Text.of(permissionDescription))
                .assign(((String)permissionLevel).isEmpty() ? PermissionDescription.ROLE_USER : (String)permissionLevel, !((String) permissionLevel).isEmpty())
                .register();
    }

    @Override
    public boolean permissionManagementExists() {
        return Sponge.getServiceManager().isRegistered(PermissionService.class);
    }
}
