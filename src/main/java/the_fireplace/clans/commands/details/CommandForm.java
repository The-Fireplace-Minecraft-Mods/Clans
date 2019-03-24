package the_fireplace.clans.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.CapHelper;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandForm extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.NOCLAN;
	}

	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public int getMaxArgs() {
		return 2;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/clan form <name> [banner]";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		if(selectedClan == null || Clans.cfg.allowMultiClanMembership) {
			String newClanName = args[0];
			if (Clans.cfg.maxNameLength > 0 && newClanName.length() > Clans.cfg.maxNameLength)
				sender.sendMessage(new TextComponentString("The clan name you have specified is too long. This server's maximum name length is: " + Clans.cfg.maxNameLength).setStyle(TextStyles.RED));
			else if (ClanCache.clanNameTaken(newClanName))
				sender.sendMessage(new TextComponentString("The clan name you have specified is already taken.").setStyle(TextStyles.RED));
			else {
				String banner = null;
				if (args.length == 2) {
					try {
						JsonToNBT.getTagFromJson(args[1]);
						//TODO: Check that the tag would actually be a valid NBTTagList of patterns
						banner = args[1];
						if (ClanCache.clanBannerTaken(banner)) {
							sender.sendMessage(new TextComponentString("The clan banner you have specified is already taken.").setStyle(TextStyles.RED));
							return;
						}
					} catch (NBTException e) {
						throw new SyntaxErrorException("Invalid Banner NBT: " + args[1]);
					}
				}
				if (Clans.getPaymentHandler().deductAmount(Clans.cfg.formClanCost, sender.getUniqueID())) {
					Clan c = new Clan(newClanName, sender.getUniqueID(), banner);
					if(ClanCache.getPlayerClans(sender.getUniqueID()).size() == 1)
						CapHelper.getPlayerClanCapability(sender).setDefaultClan(c.getClanId());
					sender.sendMessage(new TextComponentString("Clan formed!").setStyle(TextStyles.GREEN));
				} else
					sender.sendMessage(new TextComponentString("Insufficient funds to form clan. It costs " + Clans.cfg.formClanCost + ' ' + Clans.getPaymentHandler().getCurrencyName(Clans.cfg.formClanCost)).setStyle(TextStyles.RED));
			}
		} else
			sender.sendMessage(new TextComponentString("You are already in a clan.").setStyle(TextStyles.RED));
	}
}
