package the_fireplace.clans.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.TextStyles;

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
		if(args.length == 1){
			try {
				if(new ItemStack(JsonToNBT.getTagFromJson(args[0])).isEmpty()) {
					sender.sendMessage(new TextComponentString("The clan banner you have specified is invalid.").setStyle(TextStyles.RED));
					return;
				}
				if(ClanCache.clanBannerTaken(args[0]))
					sender.sendMessage(new TextComponentString("The clan banner you have specified is already taken.").setStyle(TextStyles.RED));
				else {
					selectedClan.setClanBanner(args[0]);
					sender.sendMessage(new TextComponentTranslation("Clan banner for %s set!", selectedClan.getClanName()).setStyle(TextStyles.GREEN));
				}
			} catch(NBTException e){
				throw new SyntaxErrorException("The clan banner you have specified is invalid.");
			}
		} else if(sender.getHeldItemMainhand().getItem() instanceof ItemBanner) {
			NBTTagCompound tags = sender.getHeldItemMainhand().writeToNBT(new NBTTagCompound());
			setClanBannerFromItem(sender, selectedClan, tags);
		} else if(sender.getHeldItemOffhand().getItem() instanceof ItemBanner) {
			NBTTagCompound tags = sender.getHeldItemOffhand().writeToNBT(new NBTTagCompound());
			setClanBannerFromItem(sender, selectedClan, tags);
		} else
			sender.sendMessage(new TextComponentString("You are not holding a banner!").setStyle(TextStyles.RED));
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

	}

	private void setClanBannerFromItem(EntityPlayerMP sender, NewClan playerClan, @Nullable NBTTagCompound tags) {
		String banner = tags != null ? tags.toString() : "";
		if(ClanCache.clanBannerTaken(banner))
			sender.sendMessage(new TextComponentString("The clan banner you have specified is already taken.").setStyle(TextStyles.RED));
		else {
			playerClan.setClanBanner(banner);
			sender.sendMessage(new TextComponentTranslation("Clan banner for %s set!", playerClan.getClanName()).setStyle(TextStyles.GREEN));
		}
	}
}
