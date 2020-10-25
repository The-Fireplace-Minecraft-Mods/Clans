package the_fireplace.clans.legacy.cache.player;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Set;
import java.util.UUID;

public class ClaimAdminCache {
    private static final Set<UUID> claimAdmins = new ConcurrentSet<>();

    public static boolean toggleClaimAdmin(EntityPlayerMP admin){
        return toggleClaimAdmin(admin.getUniqueID());
    }

    public static boolean toggleClaimAdmin(UUID admin){
        if(claimAdmins.contains(admin)) {
            claimAdmins.remove(admin);
            return false;
        } else {
            claimAdmins.add(admin);
            return true;
        }
    }

    public static boolean isClaimAdmin(EntityPlayerMP admin) {
        return isClaimAdmin(admin.getUniqueID());
    }

    public static boolean isClaimAdmin(UUID admin) {
        return claimAdmins.contains(admin);
    }
}
