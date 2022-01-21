package the_fireplace.clans.legacy.commands.op.management;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.legacy.commands.OpClanSubCommand;
import the_fireplace.clans.legacy.logic.ClanMemberManagement;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandSetRank extends OpClanSubCommand
{
    @Override
    public String getName() {
        return "setrank";
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        String clan = args[0];
        UUID c = ClanNames.getClanByName(clan);
        if (c != null) {
            try {
                if (args[1].equalsIgnoreCase("any") || args[1].equalsIgnoreCase("none")) {
                    throwWrongUsage(sender);
                }
                EnumRank rank = EnumRank.valueOf(args[1].toUpperCase());
                ClanMemberManagement.setRank(server, sender, args[2], c, rank);
            } catch (IllegalArgumentException e) {
                throwWrongUsage(sender);
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clan).setStyle(TextStyles.RED));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, ClanNames.getClanNames());
        } else if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, "member", "admin", "leader");
        } else if (args.length == 3) {
            return getListOfStringsMatchingLastWord(args, server.getPlayerProfileCache().getUsernames());
        }
        return Collections.emptyList();
    }
}
