package the_fireplace.clans.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBanner;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.MinecraftColors;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;

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
		Clan playerClan = ClanCache.getPlayerClan(sender.getUniqueID());
		assert playerClan != null;
		NBTTagCompound banner;
		try{
			banner = JsonToNBT.getTagFromJson(playerClan.getClanBanner());
		} catch(NBTException e){
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Your clan does not have a banner."));
			return;
		}
		if(sender.getHeldItemMainhand().getItem() instanceof ItemBanner) {
			sender.getHeldItemMainhand().getOrCreateSubCompound("BlockEntityTag").setTag("Patterns", banner.getTagList("Patterns", 10));
			sender.getHeldItemMainhand().setItemDamage(banner.getShort("ClanBaseColor"));
		} else if(sender.getHeldItemOffhand().getItem() instanceof ItemBanner) {
			sender.getHeldItemOffhand().getOrCreateSubCompound("BlockEntityTag").setTag("Patterns", banner.getTagList("Patterns", 10));
			sender.getHeldItemOffhand().setItemDamage(banner.getShort("ClanBaseColor"));
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "You are not holding a banner!"));
	}
}
