package dev.the_fireplace.clans.legacy.commands;

import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.util.PermissionManager;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class OpClanSubCommand extends ClanSubCommand
{
    @Override
    public final boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return PermissionManager.hasPermission(sender, PermissionManager.OPCLAN_COMMAND_PREFIX + getUsage(server).split(" ")[1], sender.canUseCommand(server.getOpPermissionLevel(), this.getName()));
    }

    @Override
    public final EnumRank getRequiredClanRank() {
        return EnumRank.LEADER;
    }

    @Override
    protected boolean allowConsoleUsage() {
        return true;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return TranslationUtil.getRawTranslationString(sender, "commands.opclan." + getName() + ".usage");
    }
}
