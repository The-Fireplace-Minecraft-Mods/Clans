package the_fireplace.clans.util;

import net.minecraft.nbt.NBTBase;
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

	class Default implements PlayerClanCapability {
		private int cooldown;
		private UUID defaultClan;

		public Default(){
			cooldown = 0;
			defaultClan = null;
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
	}

	class Storage implements Capability.IStorage<PlayerClanCapability> {

		@Nullable
		@Override
		public NBTBase writeNBT(Capability<PlayerClanCapability> capability, PlayerClanCapability instance, EnumFacing side) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("cooldown", instance.getCooldown());
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
			}
		}
	}
}
