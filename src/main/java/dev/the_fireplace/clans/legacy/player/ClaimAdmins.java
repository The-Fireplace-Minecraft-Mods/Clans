package dev.the_fireplace.clans.legacy.player;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Set;
import java.util.UUID;

public class ClaimAdmins
{
    private static final Set<UUID> CLAIM_ADMINS = new ConcurrentSet<>();

    public static boolean toggleClaimAdmin(ServerPlayerEntity admin) {
        return toggleClaimAdmin(admin.getUniqueID());
    }

    public static boolean toggleClaimAdmin(UUID admin) {
        if (CLAIM_ADMINS.contains(admin)) {
            CLAIM_ADMINS.remove(admin);
            return false;
        } else {
            CLAIM_ADMINS.add(admin);
            return true;
        }
    }

    public static boolean isClaimAdmin(ServerPlayerEntity admin) {
        return isClaimAdmin(admin.getUniqueID());
    }

    public static boolean isClaimAdmin(UUID admin) {
        return CLAIM_ADMINS.contains(admin);
    }
}
