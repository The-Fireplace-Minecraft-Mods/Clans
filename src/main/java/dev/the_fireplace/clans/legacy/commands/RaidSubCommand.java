package dev.the_fireplace.clans.legacy.commands;

import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.util.PermissionManager;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class RaidSubCommand extends ClanSubCommand
{
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return PermissionManager.hasPermission(sender, PermissionManager.RAID_COMMAND_PREFIX + getUsage(server).split(" ")[1], true);
    }

    @Override
    public final EnumRank getRequiredClanRank() {
        return EnumRank.ANY;
    }

    @Override
    protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return TranslationUtil.getRawTranslationString(sender, "commands.raid." + getName() + ".usage");
    }
}
