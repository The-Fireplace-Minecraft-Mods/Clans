package the_fireplace.clans.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.CapHelper;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandForm extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return Clans.cfg.allowMultiClanMembership ? EnumRank.ANY : EnumRank.NOCLAN;
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
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.form.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		if(selectedClan == null || Clans.cfg.allowMultiClanMembership) {
			String newClanName = args[0];
			if (Clans.cfg.maxNameLength > 0 && newClanName.length() > Clans.cfg.maxNameLength)
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setname.toolong", newClanName, Clans.cfg.maxNameLength).setStyle(TextStyles.RED));
			else if (ClanCache.clanNameTaken(newClanName))
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setname.taken", newClanName).setStyle(TextStyles.RED));
			else {
				String banner = null;
				if (args.length == 2) {
					try {
						banner = args[1];
						if(new ItemStack(JsonToNBT.getTagFromJson(banner)).isEmpty()) {
							sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setbanner.invalid").setStyle(TextStyles.RED));
							return;
						}
						if (ClanCache.clanBannerTaken(banner)) {
							sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setbanner.taken").setStyle(TextStyles.RED));
							return;
						}
					} catch (NBTException e) {
						throw new SyntaxErrorException(TranslationUtil.getRawTranslationString(sender.getUniqueID(), "commands.clan.setbanner.invalid"));
					}
				}
				if (Clans.getPaymentHandler().deductAmount(Clans.cfg.formClanCost, sender.getUniqueID())) {
					Clan c = new Clan(newClanName, sender.getUniqueID(), banner);
					if(ClanCache.getPlayerClans(sender.getUniqueID()).size() == 1)
						CapHelper.getPlayerClanCapability(sender).setDefaultClan(c.getClanId());
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.form.success").setStyle(TextStyles.GREEN));
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.form.insufficient_funds", Clans.cfg.formClanCost, Clans.getPaymentHandler().getCurrencyName(Clans.cfg.formClanCost)).setStyle(TextStyles.RED));
			}
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.form.already_in_clan").setStyle(TextStyles.RED));
	}
}
