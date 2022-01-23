package dev.the_fireplace.clans.legacy.commands.config.clan;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import dev.the_fireplace.clans.legacy.clan.accesscontrol.ClanPermissions;
import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Locale;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSetPermission extends ClanSubCommand
{
    @Override
    public String getName() {
        return "set";
    }

    @Override
    public EnumRank getRequiredClanRank() {
        return EnumRank.LEADER;
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public void run(MinecraftServer server, ServerPlayerEntity sender, String[] args) throws CommandException {
        String perm = args[0];
        if (!ClanPermissions.DEFAULT_PERMISSIONS.containsKey(perm)) {
            throw new IllegalArgumentException(TranslationUtil.getStringTranslation(sender.getUniqueID(), "commands.clan.set.invalid_perm", perm));
        }
        if (args.length == 3) {
            GameProfile player = parsePlayerName(server, args[1]);
            boolean value = parseBool(args[2]);
            ClanPermissions.get(selectedClan).addPermissionOverride(perm, player.getId(), value);
        } else {
            String rankName = args[1].toUpperCase(Locale.getDefault());
            try {
                EnumRank rank = EnumRank.valueOf(rankName);
                ClanPermissions.get(selectedClan).setPerm(perm, rank);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(TranslationUtil.getStringTranslation(sender.getUniqueID(), "commands.clan.set.invalid_rank", args[1]));
            }
        }
        sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.set.success").setStyle(TextStyles.GREEN));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> ret = Lists.newArrayList();
        if (args.length == 1) {
            ret.addAll(ClanPermissions.DEFAULT_PERMISSIONS.keySet());
        } else if (args.length == 2) {
            for (EnumRank rank : EnumRank.values()) {
                if (!rank.equals(EnumRank.NOCLAN)) {
                    ret.add(rank.name());
                }
            }
            for (GameProfile profile : server.getOnlinePlayerProfiles()) {
                ret.add(profile.getName());
            }
        } else {
            ret.add("true");
            ret.add("false");
        }
        return getListOfStringsMatchingLastWord(args, ret);
    }
}
