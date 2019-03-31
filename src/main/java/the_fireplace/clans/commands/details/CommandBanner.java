package the_fireplace.clans.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandBanner extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.MEMBER;
	}

	@Override
	public int getMinArgs() {
		return 0;
	}

	@Override
	public int getMaxArgs() {
		return 0;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/clan banner";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		NBTTagCompound banner;
		try{
			banner = JsonToNBT.getTagFromJson(selectedClan.getClanBanner());
		} catch(NBTException e){
			sender.sendMessage(new TextComponentTranslation("%s does not have a banner.", selectedClan.getClanName()).setStyle(TextStyles.RED));
			return;
		}
		if(sender.getHeldItemMainhand().getItem() instanceof ItemBanner) {
			int count = sender.getHeldItemMainhand().getCount();
			ItemStack bannerStack = new ItemStack(banner);
			bannerStack.setCount(count);
			sender.setHeldItem(EnumHand.MAIN_HAND, bannerStack);
		} else if(sender.getHeldItemOffhand().getItem() instanceof ItemBanner) {
			int count = sender.getHeldItemOffhand().getCount();
			ItemStack bannerStack = new ItemStack(banner);
			bannerStack.setCount(count);
			sender.setHeldItem(EnumHand.OFF_HAND, bannerStack);
		} else
			sender.sendMessage(new TextComponentString("You are not holding a banner!").setStyle(TextStyles.RED));
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) {

	}
}
