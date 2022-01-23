package dev.the_fireplace.clans.legacy.commands.config.player;

import com.google.common.collect.Lists;
import dev.the_fireplace.clans.legacy.clan.membership.ClanMembers;
import dev.the_fireplace.clans.legacy.clan.membership.PlayerClans;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanNames;
import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.player.PlayerClanSettings;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSetDefault extends ClanSubCommand
{
    @Override
    public String getName() {
        return "setdefault";
    }

    @Override
    public EnumRank getRequiredClanRank() {
        return EnumRank.MEMBER;
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
    public void run(MinecraftServer server, ServerPlayerEntity sender, String[] args) {
        UUID newDefaultClan = ClanNames.getClanByName(args[0]);
        if (newDefaultClan != null) {
            if (ClanMembers.get(newDefaultClan).isMember(sender.getUniqueID())) {
                PlayerClanSettings.setDefaultClan(sender.getUniqueID(), newDefaultClan);
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setdefault.success", ClanNames.get(newDefaultClan).getName()).setStyle(TextStyles.GREEN));
            } else {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.not_in_clan", ClanNames.get(newDefaultClan).getName()).setStyle(TextStyles.RED));
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.notfound", args[0]).setStyle(TextStyles.RED));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> ret = Lists.newArrayList();
        if (args.length == 1 && sender instanceof ServerPlayerEntity) {
            for (UUID c : PlayerClans.getClansPlayerIsIn(((ServerPlayerEntity) sender).getUniqueID())) {
                ret.add(ClanNames.get(c).getName());
            }
        }
        return getListOfStringsMatchingLastWord(args, ret);
    }
}
