package the_fireplace.clans.legacy.commands.op.management;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.legacy.commands.OpClanSubCommand;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandSetName extends OpClanSubCommand
{
    @Override
    public String getName() {
        return "setname";
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) {
        String clanName = args[0];
        UUID clan = ClanNames.getClanByName(clanName);
        if (clan != null) {
            String newName = args[1];
            if (ClanNames.isClanNameAvailable(newName)) {
                String oldName = ClanNames.get(clan).getName();
                ClanNames.get(clan).setName(newName);
                sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.setname.success", oldName, newName).setStyle(TextStyles.GREEN));
            } else {
                sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.setname.taken", newName).setStyle(TextStyles.RED));
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", clanName).setStyle(TextStyles.RED));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, ClanNames.getClanNames()) : Collections.emptyList();
    }
}
