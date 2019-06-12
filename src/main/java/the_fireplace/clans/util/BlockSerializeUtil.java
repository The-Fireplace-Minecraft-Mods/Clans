package the_fireplace.clans.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Objects;

public class BlockSerializeUtil {
	public static String blockToString(IBlockState state) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("name", Objects.requireNonNull(state.getBlock().getRegistryName()).toString());
		nbt.setInteger("meta", state.getBlock().getMetaFromState(state));
		return nbt.toString();
	}

	public static IBlockState blockFromString(String block) {
		NBTTagCompound nbt;
		try {
			nbt = JsonToNBT.getTagFromJson(block);
		} catch(NBTException e) {
			e.printStackTrace();
			return Blocks.AIR.getDefaultState();
		}
		return Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString("name")))).getStateFromMeta(nbt.getInteger("meta"));
	}
}
