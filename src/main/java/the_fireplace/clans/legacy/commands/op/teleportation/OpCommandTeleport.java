package the_fireplace.clans.legacy.commands.op.teleportation;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.clan.home.ClanHomes;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.legacy.commands.OpClanSubCommand;
import the_fireplace.clans.legacy.util.EntityUtil;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandTeleport extends OpClanSubCommand
{
    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public String getName() {
        return "tp";
    }

    @Override
    protected boolean allowConsoleUsage() {
        return false;
    }

    @Override
    protected void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
        UUID targetClan = ClanNames.getClanByName(args[0]);
        if (targetClan != null) {
            if (!ClanHomes.hasHome(targetClan)) {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.home.nohome", ClanNames.get(targetClan).getName()).setStyle(TextStyles.RED));
            } else {
                BlockPos home = ClanHomes.get(targetClan).toBlockPos();
                int playerDim = sender.dimension;

                EntityUtil.teleportHome(sender, home, ClanHomes.get(targetClan).getHomeDim(), playerDim, false);
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", args[0]).setStyle(TextStyles.RED));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, ClanNames.getClanNames()) : Collections.emptyList();
    }
}
