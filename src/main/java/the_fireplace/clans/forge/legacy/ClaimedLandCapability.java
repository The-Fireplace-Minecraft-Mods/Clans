package the_fireplace.clans.forge.legacy;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.UUID;

@Deprecated
public interface ClaimedLandCapability {

	@Deprecated
	UUID getClan();
	@Deprecated
	void setClan(UUID faction);
	@Deprecated
	boolean pre120();
	@Deprecated
	void setPre120(boolean p120);

	class Default implements ClaimedLandCapability {
		private UUID claimingFaction;
		private boolean pre120 = false;

		public Default(){
			claimingFaction = null;
		}

		@Override
		public void setClan(UUID faction){
			claimingFaction = faction;
		}

		@Override
		public boolean pre120() {
			return pre120;
		}

		@Override
		public void setPre120(boolean p120) {
			pre120 = p120;
		}

		@Override
		public UUID getClan(){
			return claimingFaction;
		}
	}

	class Storage implements Capability.IStorage<ClaimedLandCapability> {

		@Nullable
		@Override
		public NBTBase writeNBT(Capability<ClaimedLandCapability> capability, ClaimedLandCapability instance, EnumFacing side) {
			NBTTagCompound out = new NBTTagCompound();
			if(instance.getClan() != null)
			    out.setUniqueId("clan", instance.getClan());
			out.setBoolean("p120", instance.pre120());
			return out;
		}

		@Override
		public void readNBT(Capability<ClaimedLandCapability> capability, ClaimedLandCapability instance, EnumFacing side, NBTBase nbt) {
			if(nbt instanceof NBTTagCompound) {
				if(((NBTTagCompound) nbt).hasUniqueId("clan"))
					instance.setClan(((NBTTagCompound) nbt).getUniqueId("clan"));
				if(((NBTTagCompound) nbt).hasKey("p120"))
					instance.setPre120(((NBTTagCompound) nbt).getBoolean("p120"));
			} else if(nbt instanceof NBTTagString && !((NBTTagString) nbt).getString().isEmpty()) {
				instance.setClan(UUID.fromString(((NBTTagString) nbt).getString()));
				instance.setPre120(true);
			}
		}
	}
}
