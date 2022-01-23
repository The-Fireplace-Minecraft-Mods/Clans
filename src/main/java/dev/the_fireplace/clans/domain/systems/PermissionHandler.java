package dev.the_fireplace.clans.domain.systems;

import net.minecraft.server.network.ServerPlayerEntity;

public interface PermissionHandler
{
    boolean hasPermission(ServerPlayerEntity player, String permissionName);

    void registerPermission(String permissionName, Object permissionLevel, String permissionDescription);

    boolean permissionManagementExists();
}
