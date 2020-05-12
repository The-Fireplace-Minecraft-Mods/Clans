package the_fireplace.clans.commands.op.teleportation;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.util.EntityUtil;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandTeleport extends OpClanSubCommand {
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
        Clan targetClan = ClanCache.getClanByName(args[0]);
        if(targetClan != null) {
            BlockPos home = targetClan.getHome();
            int playerDim = sender.dimension;

            if (!targetClan.hasHome() || home == null)
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.home.nohome", targetClan.getName()).setStyle(TextStyles.RED));
            else
                EntityUtil.teleportHome(sender, home, targetClan.getHomeDim(), playerDim, false);
        } else
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.notfound", args[0]).setStyle(TextStyles.RED));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, ClanCache.getClanNames().keySet()) : Collections.emptyList();
    }
}
