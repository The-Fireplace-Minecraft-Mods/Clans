package the_fireplace.clans.legacy.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import the_fireplace.clans.ClansModContainer;

import java.util.Objects;

public class BlockSerializeUtil {
	public static String blockToString(IBlockState state) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("name", Objects.requireNonNull(state.getBlock().getRegistryName()).toString());
		nbt.setInteger("meta", state.getBlock().getMetaFromState(state));
		return nbt.toString();
	}

	public static IBlockState blockFromString(String blockStr) {
		NBTTagCompound nbt;
		try {
			nbt = JsonToNBT.getTagFromJson(blockStr);
		} catch(NBTException e) {
			e.printStackTrace();
			return Blocks.AIR.getDefaultState();
		}
		Block block = ClansModContainer.getMinecraftHelper().getBlock(new ResourceLocation(nbt.getString("name")));
		if(block == null)
			return Blocks.AIR.getDefaultState();
		return block.getStateFromMeta(nbt.getInteger("meta"));
	}
}
