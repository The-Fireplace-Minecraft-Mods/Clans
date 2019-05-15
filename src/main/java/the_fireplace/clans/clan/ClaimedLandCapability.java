package the_fireplace.clans.clan;

import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import java.util.UUID;

/**
 * Used on Chunks to say which clan the chunk is claimed for.
 * Used on Players to say which clan's land the player was last standing in, and show the player a message if they enter another clan's territory
 */
public interface ClaimedLandCapability {

	UUID getClan();
	void setClan(UUID faction);
	boolean pre120();
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
		public UUID getClan(){
			return claimingFaction;
		}

		@Override
		public boolean pre120() {
			return pre120;
		}

		@Override
		public void setPre120(boolean p120) {
			pre120 = p120;
		}
	}

	class Storage implements Capability.IStorage<ClaimedLandCapability> {
		@Override
		public INBTBase writeNBT(Capability<ClaimedLandCapability> capability, ClaimedLandCapability instance, EnumFacing side) {
			NBTTagCompound out = new NBTTagCompound();
			if(instance.getClan() != null)
				out.putUniqueId("clan", instance.getClan());
			out.putBoolean("p120", instance.pre120());
			return out;
		}

		@Override
		public void readNBT(Capability<ClaimedLandCapability> capability, ClaimedLandCapability instance, EnumFacing side, INBTBase nbt) {
			if(nbt instanceof NBTTagCompound) {
				if(((NBTTagCompound) nbt).hasUniqueId("clan"))
					instance.setClan(((NBTTagCompound) nbt).getUniqueId("clan"));
				if(((NBTTagCompound) nbt).contains("p120"))
					instance.setPre120(((NBTTagCompound) nbt).getBoolean("p120"));
			} else if(nbt instanceof NBTTagString && !nbt.getString().isEmpty()) {
				instance.setClan(UUID.fromString(nbt.getString()));
				instance.setPre120(true);
			}
		}
	}
}
