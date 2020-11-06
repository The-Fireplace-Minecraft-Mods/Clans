package the_fireplace.clans.legacy.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandBanner extends ClanSubCommand {
	@Override
	public String getName() {
		return "banner";
	}

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
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
		NBTTagCompound banner;
		if(selectedClan.getClanMetadata().getClanBanner() != null) {
			try {
				banner = JsonToNBT.getTagFromJson(selectedClan.getClanMetadata().getClanBanner());
			} catch (NBTException e) {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.banner.clan_nobanner", selectedClan.getClanMetadata().getClanName()).setStyle(TextStyles.RED));
				return;
			}
		} else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.banner.clan_nobanner", selectedClan.getClanMetadata().getClanName()).setStyle(TextStyles.RED));
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
		} else if(sender.getHeldItemMainhand().getItem() instanceof ItemShield) {
			ItemStack bannerStack = new ItemStack(banner);
			NBTTagCompound bet = bannerStack.getSubCompound("BlockEntityTag");
			NBTTagCompound finalBet = bet == null ? new NBTTagCompound() : bet.copy();
			finalBet.setInteger("Base", bannerStack.getMetadata() & 15);
			ItemStack shieldStack = sender.getHeldItemMainhand();
			shieldStack.setTagInfo("BlockEntityTag", finalBet);
			sender.setHeldItem(EnumHand.MAIN_HAND, shieldStack);
		} else if(sender.getHeldItemOffhand().getItem() instanceof ItemShield) {
			ItemStack bannerStack = new ItemStack(banner);
			NBTTagCompound bet = bannerStack.getSubCompound("BlockEntityTag");
			NBTTagCompound finalBet = bet == null ? new NBTTagCompound() : bet.copy();
			finalBet.setInteger("Base", bannerStack.getMetadata() & 15);
			ItemStack shieldStack = sender.getHeldItemOffhand();
			shieldStack.setTagInfo("BlockEntityTag", finalBet);
			sender.setHeldItem(EnumHand.OFF_HAND, shieldStack);
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.banner.player_nobanner").setStyle(TextStyles.RED));
	}
}
