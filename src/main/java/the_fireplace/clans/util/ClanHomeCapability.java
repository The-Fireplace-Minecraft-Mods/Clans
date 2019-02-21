package the_fireplace.clans.util;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public interface ClanHomeCapability {

	int getCooldown();
	void setCooldown(int cooldown);

	class Default implements ClanHomeCapability {
		private int cooldown;

		public Default(){
			cooldown = 0;
		}

		@Override
		public void setCooldown(int cooldown){
			this.cooldown = cooldown;
		}

		@Override
		public int getCooldown(){
			return cooldown;
		}
	}

	class Storage implements Capability.IStorage<ClanHomeCapability> {

		@Nullable
		@Override
		public NBTBase writeNBT(Capability<ClanHomeCapability> capability, ClanHomeCapability instance, EnumFacing side) {
			return new NBTTagInt(instance.getCooldown());
		}

		@Override
		public void readNBT(Capability<ClanHomeCapability> capability, ClanHomeCapability instance, EnumFacing side, NBTBase nbt) {
			if(nbt instanceof NBTTagInt)
				instance.setCooldown(((NBTTagInt) nbt).getInt());
		}
	}
}
