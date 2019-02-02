package the_fireplace.clans.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
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
public class CommandSetBanner extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.LEADER;
	}

	@Override
	public int getMinArgs() {
		return 0;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/clan setbanner [banner]";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		Clan playerClan = ClanCache.getPlayerClan(sender.getUniqueID());
		assert playerClan != null;
		if(args.length == 1){
			try {
				JsonToNBT.getTagFromJson(args[0]);
				//TODO: Check that the tag would actually be a valid NBTTagList of patterns
				if(ClanCache.clanBannerTaken(args[0]))
					sender.sendMessage(new TextComponentString(MinecraftColors.RED+"The clan banner you have specified is already taken."));
				else {
					playerClan.setClanBanner(args[0]);
					sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "Clan banner set!"));
				}
			} catch(NBTException e){
				throw new SyntaxErrorException("Invalid Banner NBT: "+args[0]);
			}
		} else if(sender.getHeldItemMainhand().getItem() instanceof ItemBanner) {
			NBTTagCompound tags = sender.getHeldItemMainhand().getSubCompound("BlockEntityTag");
			if(tags != null)
				tags.setShort("ClanBaseColor", (short) sender.getHeldItemMainhand().getMetadata());
			setClanBannerFromItem(sender, playerClan, tags);
		} else if(sender.getHeldItemOffhand().getItem() instanceof ItemBanner) {
			NBTTagCompound tags = sender.getHeldItemOffhand().getSubCompound("BlockEntityTag");
			if(tags != null)
				tags.setShort("ClanBaseColor", (short) sender.getHeldItemOffhand().getMetadata());
			setClanBannerFromItem(sender, playerClan, tags);
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "You are not holding a banner!"));
	}

	private void setClanBannerFromItem(EntityPlayerMP sender, Clan playerClan, @Nullable NBTTagCompound tags) {
		String banner = tags != null ? tags.toString() : "";
		if(ClanCache.clanBannerTaken(banner))
			sender.sendMessage(new TextComponentString(MinecraftColors.RED+"The clan banner you have specified is already taken."));
		else {
			playerClan.setClanBanner(banner);
			sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "Clan banner set!"));
		}
	}
}
