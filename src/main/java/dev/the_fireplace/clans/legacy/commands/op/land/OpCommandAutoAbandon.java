package dev.the_fireplace.clans.legacy.commands.op.land;

import dev.the_fireplace.clans.legacy.commands.OpClanSubCommand;
import dev.the_fireplace.clans.legacy.logic.ClaimManagement;
import dev.the_fireplace.clans.legacy.player.autoland.OpAutoAbandon;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandAutoAbandon extends OpClanSubCommand
{
    @Override
    public String getName() {
        return "autoabandon";
    }

    @Override
    public int getMinArgs() {
        return 0;
    }

    @Override
    public int getMaxArgs() {
        return 0;
    }

    @Override
    public void run(MinecraftServer server, ServerPlayerEntity sender, String[] args) {
        if (!OpAutoAbandon.cancelOpAutoAbandon(sender.getUniqueID())) {
            OpAutoAbandon.activateOpAutoAbandon(sender.getUniqueID());
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.opclan.autoabandon.start").setStyle(TextStyles.YELLOW));
            ClaimManagement.checkAndAttemptAbandon(sender, null);
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.opclan.autoabandon.stop").setStyle(TextStyles.GREEN));
        }
    }

    @Override
    protected boolean allowConsoleUsage() {
        return false;
    }
}
