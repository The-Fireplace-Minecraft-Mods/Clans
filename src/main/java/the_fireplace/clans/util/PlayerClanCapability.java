package the_fireplace.clans.util;

import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.UUID;

public interface PlayerClanCapability {

	int getCooldown();
	void setCooldown(int cooldown);
	UUID getDefaultClan();
	void setDefaultClan(UUID defaultClan);
	boolean getClaimWarning();
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
		public INBTBase writeNBT(Capability<PlayerClanCapability> capability, PlayerClanCapability instance, EnumFacing side) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.putInt("cooldown", instance.getCooldown());
			tag.putBoolean("claimWarning", instance.getClaimWarning());
			if(instance.getDefaultClan() != null)
				tag.putUniqueId("defaultClan", instance.getDefaultClan());
			return tag;
		}

		@Override
		public void readNBT(Capability<PlayerClanCapability> capability, PlayerClanCapability instance, EnumFacing side, INBTBase nbt) {
			if(nbt instanceof NBTTagCompound) {
				instance.setCooldown(((NBTTagCompound) nbt).getInt("cooldown"));
				if(((NBTTagCompound) nbt).hasUniqueId("defaultClan"))
					instance.setDefaultClan(((NBTTagCompound) nbt).getUniqueId("defaultClan"));
				if(((NBTTagCompound) nbt).contains("claimWarning"))
					instance.setClaimWarning(((NBTTagCompound) nbt).getBoolean("claimWarning"));
			}
		}
	}
}
