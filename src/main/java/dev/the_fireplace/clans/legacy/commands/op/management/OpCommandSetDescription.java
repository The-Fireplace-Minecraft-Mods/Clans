package dev.the_fireplace.clans.legacy.commands.op.management;

import dev.the_fireplace.clans.legacy.clan.metadata.ClanDescriptions;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanNames;
import dev.the_fireplace.clans.legacy.commands.OpClanSubCommand;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandSetDescription extends OpClanSubCommand
{
    @Override
    public String getName() {
        return "setdescription";
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) {
        String clanName = args[0];
        UUID clan = ClanNames.getClanByName(clanName);
        if (clan != null) {
            StringBuilder newDescription = new StringBuilder();
            for (String arg : ArrayUtils.subarray(args, 1, args.length)) {
                newDescription.append(arg).append(' ');
            }
            ClanDescriptions.get(clan).setDescription(newDescription.toString());
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.setdescription.success", clanName).setStyle(TextStyles.GREEN));
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clanName).setStyle(TextStyles.RED));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, ClanNames.getClanNames()) : Collections.emptyList();
    }
}
