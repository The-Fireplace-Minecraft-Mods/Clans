package the_fireplace.clans.forge.legacy;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.model.Clan;

import javax.annotation.Nullable;
import java.util.UUID;

@Deprecated
public interface PlayerClanCapability {

    /**
     * Check if a clan is the player's default clan, and if it is, update the player's default clan to something else.
     * @param player
     * The player to check and update (if needed)
     * @param removeClan
     * The clan the player is being removed from. Use null to forcibly change the player's default clan, regardless of what it currently is.
     */
    static void updateDefaultClan(EntityPlayerMP player, @Nullable Clan removeClan) {
        UUID oldDef = CapHelper.getPlayerClanCapability(player).getDefaultClan();
        if(removeClan == null || removeClan.getClanId().equals(oldDef))
            if(ClanCache.getPlayerClans(player.getUniqueID()).isEmpty())
                CapHelper.getPlayerClanCapability(player).setDefaultClan(null);
            else
                CapHelper.getPlayerClanCapability(player).setDefaultClan(ClanCache.getPlayerClans(player.getUniqueID()).get(0).getClanId());
    }

	@Deprecated
    int getCooldown();
	@Deprecated
	void setCooldown(int cooldown);
	@Deprecated
	UUID getDefaultClan();
	@Deprecated
	void setDefaultClan(UUID defaultClan);
	@Deprecated
	boolean getClaimWarning();
	@Deprecated
	void setClaimWarning(boolean claimWarning);

	class Default implements PlayerClanCapability {
		private int cooldown;
		private UUID defaultClan;
		private boolean claimWarning;

		public Default(){
			cooldown = 0;
			defaultClan = null;
			claimWarning = false;
		}

		@Override
		public void setCooldown(int cooldown){
			this.cooldown = cooldown;
		}

		@Override
		public int getCooldown(){
			return cooldown;
		}

		@Override
		public UUID getDefaultClan() {
			return defaultClan;
		}

		@Override
		public void setDefaultClan(UUID defaultClan) {
			this.defaultClan = defaultClan;
		}

		@Override
		public boolean getClaimWarning() {
			return claimWarning;
		}

		@Override
		public void setClaimWarning(boolean claimWarning) {
			this.claimWarning = claimWarning;
		}
	}

	class Storage implements Capability.IStorage<PlayerClanCapability> {

		@Nullable
		@Override
		public NBTBase writeNBT(Capability<PlayerClanCapability> capability, PlayerClanCapability instance, EnumFacing side) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("cooldown", instance.getCooldown());
			tag.setBoolean("claimWarning", instance.getClaimWarning());
			if(instance.getDefaultClan() != null)
				tag.setUniqueId("defaultClan", instance.getDefaultClan());
			return tag;
		}

		@Override
		public void readNBT(Capability<PlayerClanCapability> capability, PlayerClanCapability instance, EnumFacing side, NBTBase nbt) {
			if(nbt instanceof NBTTagCompound) {
				instance.setCooldown(((NBTTagCompound) nbt).getInteger("cooldown"));
				if(((NBTTagCompound) nbt).hasUniqueId("defaultClan"))
					instance.setDefaultClan(((NBTTagCompound) nbt).getUniqueId("defaultClan"));
				if(((NBTTagCompound) nbt).hasKey("claimWarning"))
					instance.setClaimWarning(((NBTTagCompound) nbt).getBoolean("claimWarning"));
			}
		}
	}
}
