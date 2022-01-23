package dev.the_fireplace.clans.legacy.commands.op.land;

import dev.the_fireplace.clans.legacy.clan.metadata.ClanNames;
import dev.the_fireplace.clans.legacy.commands.OpClanSubCommand;
import dev.the_fireplace.clans.legacy.logic.ClaimManagement;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandClaim extends OpClanSubCommand
{
    @Override
    public String getName() {
        return "claim";
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
        String clanName = args[0];
        UUID clan = ClanNames.getClanByName(clanName);
        if (clan != null) {
            ClaimManagement.adminClaimChunk(sender, clan);
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clanName).setStyle(TextStyles.RED));
        }
    }

    @Override
    protected boolean allowConsoleUsage() {
        return false;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, ClanNames.getClanNames()) : Collections.emptyList();
    }
}
