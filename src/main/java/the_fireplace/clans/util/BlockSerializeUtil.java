package the_fireplace.clans.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class BlockSerializeUtil {
	public static String blockToString(IBlockState state) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.putString("name", Objects.requireNonNull(state.getBlock().getRegistryName()).toString());
		/*NBTTagList properties = new NBTTagList();
		for(IProperty p : state.getProperties()) {
			//noinspection unchecked
			properties.add(new NBTTagString(p.getName(state.get(p))));
		}
		nbt.put("properties", properties);*/
		return nbt.toString();
	}

	public static IBlockState blockFromString(String block) {
		NBTTagCompound nbt;
		try {
			nbt = JsonToNBT.getTagFromJson(block);
		} catch(CommandSyntaxException e) {
			e.printStackTrace();
			return Blocks.AIR.getDefaultState();
		}
		IBlockState out = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString("name")))).getDefaultState();
		/*for(IProperty p: out.getProperties())
		out.get()*///TODO see if this is needed, and if so, how to do it
		return out;
	}
}
