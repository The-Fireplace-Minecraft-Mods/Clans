package dev.the_fireplace.clans.legacy.commands.op.land;

import dev.the_fireplace.clans.legacy.commands.OpClanSubCommand;
import dev.the_fireplace.clans.legacy.player.ClaimAdmins;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandBuildAdmin extends OpClanSubCommand
{
    @Override
    public String getName() {
        return "buildadmin";
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
        if (ClaimAdmins.toggleClaimAdmin(sender)) {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.opclan.buildadmin.on").setStyle(TextStyles.YELLOW));
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.opclan.buildadmin.off").setStyle(TextStyles.GREEN));
        }
    }

    @Override
    protected boolean allowConsoleUsage() {
        return false;
    }
}
