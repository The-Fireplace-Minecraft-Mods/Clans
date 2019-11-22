package the_fireplace.clans.commands.details;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.data.PlayerData;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandForm extends ClanSubCommand {
	@Override
	public String getName() {
		return "form";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return Clans.getConfig().isAllowMultiClanMembership() ? EnumRank.ANY : EnumRank.NOCLAN;
	}

	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}

	@Override
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
		if(selectedClan == null || Clans.getConfig().isAllowMultiClanMembership()) {
			String newClanName = TextStyles.stripFormatting(args[0]);
			if (Clans.getConfig().getMaxNameLength() > 0 && newClanName.length() > Clans.getConfig().getMaxNameLength())
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setname.toolong", newClanName, Clans.getConfig().getMaxNameLength()).setStyle(TextStyles.RED));
			else if (ClanCache.clanNameTaken(newClanName))
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setname.taken", newClanName).setStyle(TextStyles.RED));
			else {
				if (Clans.getPaymentHandler().deductAmount(Clans.getConfig().getFormClanCost(), sender.getUniqueID())) {
					Clan c = new Clan(newClanName, sender.getUniqueID());
					if(ClanCache.getPlayerClans(sender.getUniqueID()).size() == 1)
						PlayerData.setDefaultClan(sender.getUniqueID(), c.getId());
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.form.success").setStyle(TextStyles.GREEN));
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.form.insufficient_funds", Clans.getConfig().getFormClanCost(), Clans.getPaymentHandler().getCurrencyName(Clans.getConfig().getFormClanCost())).setStyle(TextStyles.RED));
			}
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.form.already_in_clan").setStyle(TextStyles.RED));
	}
}
