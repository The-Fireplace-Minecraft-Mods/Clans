package the_fireplace.clans.legacy.commands.raiding;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.clan.ClanIdRegistry;
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
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandJoinRaid extends RaidSubCommand
{
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
        UUID target = ClanNames.getClanByName(args[0]);
        if (target == null) {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.notfound", args[0]).setStyle(TextStyles.RED));
        } else if (AdminControlledClanSettings.get(target).isUnraidable()) {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.unraidable", args[0]).setStyle(TextStyles.RED));
        } else {
            if (!RaidingParties.getRaidingPlayers().contains(sender.getUniqueID())) {
                if (!ClanMembers.get(target).getMemberRanks().containsKey(sender.getUniqueID())) {
                    if (!RaidingParties.getRaidedClans().contains(target)) {
                        if (!ClanShield.get(target).isShielded()) {
                            if (!ClanMembers.get(target).getRaidDefenders().isEmpty() && ClanMembers.get(target).getRaidDefenders().size() + ClansModContainer.getConfig().getMaxRaidersOffset() > 0) {
                                new Raid(sender, target);
                                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.created", ClanNames.get(target).getName()).setStyle(TextStyles.GREEN));
                            } else {
                                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.create_fail", ClanNames.get(target).getName()).setStyle(TextStyles.RED));
                            }
                        } else {
                            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.shield", ClanNames.get(target).getName(), Math.round(100f * ClanShield.get(target).getShield() / 60) / 100f).setStyle(TextStyles.RED));
                        }
                    } else { //Join an existing raid
                        Raid raid = RaidingParties.getInactiveRaid(target);
                        if (ClanMembers.get(target).getRaidDefenderCount() + ClansModContainer.getConfig().getMaxRaidersOffset() > raid.getAttackerCount()) {
                            raid.addAttacker(sender);
                            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.success", ClanNames.get(target).getName()).setStyle(TextStyles.GREEN));
                        } else {
                            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.limit", ClanNames.get(target).getName(), raid.getAttackerCount(), ClanMembers.get(target).getOnlineMemberRanks().size() + ClansModContainer.getConfig().getMaxRaidersOffset()).setStyle(TextStyles.RED));
                        }
                    }
                } else {
                    sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.inclan").setStyle(TextStyles.RED));
                }
            } else {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.join.inparty").setStyle(TextStyles.RED));
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> targetClanNames = Lists.newArrayList();
        for (UUID clan : ClanIdRegistry.getIds()) {
            if (sender.getCommandSenderEntity() != null && !ClanMembers.get(clan).getMemberRanks().containsKey(sender.getCommandSenderEntity().getUniqueID()) && !AdminControlledClanSettings.get(clan).isServerOwned()) {
                targetClanNames.add(ClanNames.get(clan).getName());
            }
        }
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, targetClanNames) : Collections.emptyList();
    }
}
