package the_fireplace.clans.legacy.commands.raiding;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.clan.raids.ClanShield;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.cache.RaidingParties;
import the_fireplace.clans.legacy.commands.RaidSubCommand;
import the_fireplace.clans.legacy.model.Raid;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandJoinRaid extends RaidSubCommand {
	@Override
	public String getName() {
		return "join";
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
		Clan target = ClanNames.getClanByName(args[0]);
        if(target == null)
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.notfound", args[0]).setStyle(TextStyles.RED));
		else if(AdminControlledClanSettings.get().isUnraidable())
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.unraidable",  args[0]).setStyle(TextStyles.RED));
		else {
			if(!RaidingParties.getRaidingPlayers().contains(sender.getUniqueID())) {
                if(!ClanMembers.get().getMemberRanks().containsKey(sender.getUniqueID())) {
					if (!RaidingParties.getRaidedClans().contains(target)) {
                        if(!ClanShield.get().isShielded()) {
                            if (!ClanMembers.get().getRaidDefenders().isEmpty() && ClanMembers.get().getRaidDefenders().size() + ClansModContainer.getConfig().getMaxRaidersOffset() > 0) {
								new Raid(sender, target);
                                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.created", target.getClanMetadata().getClanName()).setStyle(TextStyles.GREEN));
							} else
                                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.create_fail", target.getClanMetadata().getClanName()).setStyle(TextStyles.RED));
						} else
                            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.shield", target.getClanMetadata().getClanName(), Math.round(100f* ClanShield.get().getShield() /60)/100f).setStyle(TextStyles.RED));
					} else { //Join an existing raid
						Raid raid = RaidingParties.getInactiveRaid(target);
                        if(ClanMembers.get().getRaidDefenderCount() + ClansModContainer.getConfig().getMaxRaidersOffset() > raid.getAttackerCount()) {
							raid.addAttacker(sender);
                            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.success", target.getClanMetadata().getClanName()).setStyle(TextStyles.GREEN));
						} else
                            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.limit", target.getClanMetadata().getClanName(), raid.getAttackerCount(), ClanMembers.get().getOnlineMemberRanks().size() + ClansModContainer.getConfig().getMaxRaidersOffset()).setStyle(TextStyles.RED));
					}
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.inclan").setStyle(TextStyles.RED));
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.inparty").setStyle(TextStyles.RED));
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		List<String> targetClanNames = Lists.newArrayList();
        for(Clan clan: ClanDatabase.getClans())
			if(sender.getCommandSenderEntity() != null && !ClanMembers.get().getMemberRanks().containsKey(sender.getCommandSenderEntity().getUniqueID()) && !AdminControlledClanSettings.get().isServerOwned())
                targetClanNames.add(clan.getClanMetadata().getClanName());
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, targetClanNames) : Collections.emptyList();
	}
}
