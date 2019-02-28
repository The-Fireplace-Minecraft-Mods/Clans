package the_fireplace.clans.clan;

import net.minecraft.nbt.INBTBase;
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

	class Default implements ClaimedLandCapability {
		private UUID claimingFaction;

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
	}

	class Storage implements Capability.IStorage<ClaimedLandCapability> {
		@Override
		public INBTBase writeNBT(Capability<ClaimedLandCapability> capability, ClaimedLandCapability instance, EnumFacing side) {
			return new NBTTagString(instance.getClan() != null ? instance.getClan().toString() : "");
		}

		@Override
		public void readNBT(Capability<ClaimedLandCapability> capability, ClaimedLandCapability instance, EnumFacing side, INBTBase nbt) {
			if(nbt instanceof NBTTagString && !nbt.getString().isEmpty())
				instance.setClan(UUID.fromString(nbt.getString()));
		}
	}
}
