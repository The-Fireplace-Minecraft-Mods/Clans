package the_fireplace.clans.commands.op.management;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.util.PlayerClanCapability;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandDisband extends OpClanSubCommand {
	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return TranslationUtil.getRawTranslationString(sender, "commands.opclan.disband.usage");
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) {
		String clan = args[0];
		Clan c = ClanCache.getClanByName(clan);
		if(c != null) {
			disbandClan(server, sender, c);
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clan).setStyle(TextStyles.RED));
	}

	@SuppressWarnings("Duplicates")
	public static void disbandClan(MinecraftServer server, ICommandSender sender, Clan c) {
		if(!c.isOpclan()) {
			if (ClanDatabase.removeClan(c.getClanId())) {
				long distFunds = Clans.getPaymentHandler().getBalance(c.getClanId());
				long rem;
				distFunds += Clans.cfg.claimChunkCost * c.getClaimCount();
				if (Clans.cfg.leaderRecieveDisbandFunds) {
					distFunds = c.payLeaders(distFunds);
					rem = distFunds % c.getMemberCount();
					distFunds /= c.getMemberCount();
				} else {
					rem = c.payLeaders(distFunds % c.getMemberCount());
					distFunds /= c.getMemberCount();
				}
				for (UUID member : c.getMembers().keySet()) {
					Clans.getPaymentHandler().ensureAccountExists(member);
					if (!Clans.getPaymentHandler().addAmount(distFunds + (rem-- > 0 ? 1 : 0), member))
						rem += c.payLeaders(distFunds);
					EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(member);
					//noinspection ConstantConditions
					if (player != null) {
						PlayerClanCapability.updateDefaultClan(player, c);
						if (!(sender instanceof EntityPlayerMP) || !player.getUniqueID().equals(((EntityPlayerMP)sender).getUniqueID()))
							player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "commands.opclan.disband.disbanded", c.getClanName(), sender.getName()).setStyle(TextStyles.YELLOW));
					}
				}
				Clans.getPaymentHandler().deductAmount(Clans.getPaymentHandler().getBalance(c.getClanId()), c.getClanId());
				sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.opclan.disband.success", c.getClanName()).setStyle(TextStyles.GREEN));
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.opclan.disband.error", c.getClanName()).setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.opclan.disband.opclan", c.getClanName()).setStyle(TextStyles.RED));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		ArrayList<String> removable = Lists.newArrayList(ClanCache.getClanNames().keySet());
		removable.remove(ClanDatabase.getOpClan().getClanName());
		return args.length == 1 ? removable : Collections.emptyList();
	}
}
