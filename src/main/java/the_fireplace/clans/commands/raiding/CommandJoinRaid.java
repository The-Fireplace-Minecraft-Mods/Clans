package the_fireplace.clans.commands.raiding;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.cache.RaidingParties;
import the_fireplace.clans.commands.RaidSubCommand;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.Raid;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandJoinRaid extends RaidSubCommand {
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
		return TranslationUtil.getRawTranslationString(sender, "commands.raid.join.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Clan target = ClanCache.getClanByName(args[0]);
		if(target == null)
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.notfound", args[0]).setStyle(TextStyles.RED));
		else if(target.isLimitless())
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.not_on_limitless", "raid", args[0]).setStyle(TextStyles.RED));
		else {
			if(!RaidingParties.getRaidingPlayers().contains(sender.getUniqueID())) {
				if(!target.getMembers().containsKey(sender.getUniqueID())) {
					if (!RaidingParties.getRaidedClans().contains(target)) {
						if(!target.isShielded()) {
							if (!target.getOnlineSurvivalMembers().isEmpty() && target.getOnlineSurvivalMembers().size() + Clans.getConfig().getMaxRaidersOffset() > 0) {
								new Raid(sender, target);
								sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.created", target.getClanName()).setStyle(TextStyles.GREEN));
							} else
								sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.create_fail", target.getClanName()).setStyle(TextStyles.RED));
						} else
							sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.shield", target.getClanName(), Math.round(100f*target.getShield()/60)/100f).setStyle(TextStyles.RED));
					} else { //Join an existing raid
						Raid raid = RaidingParties.getRaid(target);
						if(target.getOnlineSurvivalMembers().size() + Clans.getConfig().getMaxRaidersOffset() > raid.getAttackerCount()) {
							raid.addAttacker(sender);
							sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.success", target.getClanName()).setStyle(TextStyles.GREEN));
						} else
							sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.limit", target.getClanName(), raid.getAttackerCount(), target.getOnlineMembers().size() + Clans.getConfig().getMaxRaidersOffset()).setStyle(TextStyles.RED));
					}
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.inclan").setStyle(TextStyles.RED));
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.inparty").setStyle(TextStyles.RED));
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		Map<Clan, Raid> raids = RaidingParties.getRaids();
		List<String> targetClanNames = Lists.newArrayList();
		for(Map.Entry<Clan, Raid> entry: raids.entrySet())
			if(sender.getCommandSenderEntity() != null && !entry.getKey().getMembers().containsKey(sender.getCommandSenderEntity().getUniqueID()))
				targetClanNames.add(entry.getKey().getClanName());
		return args.length == 1 ? targetClanNames : Collections.emptyList();
	}
}
