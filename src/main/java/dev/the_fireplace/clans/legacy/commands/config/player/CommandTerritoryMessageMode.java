package dev.the_fireplace.clans.legacy.commands.config.player;

import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.model.TerritoryDisplayMode;
import dev.the_fireplace.clans.legacy.player.TerritoryMessageSettings;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandTerritoryMessageMode extends ClanSubCommand
{
    @Override
    public String getName() {
        return "territorymessagemode";
    }

    @Override
    public EnumRank getRequiredClanRank() {
        return EnumRank.ANY;
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public void run(MinecraftServer server, ServerPlayerEntity sender, String[] args) throws CommandException {
        String mode = args[0];
        Boolean showDesc = args.length == 2 ? parseBool(args[1]) : null;
        try {
            boolean newShowDesc = showDesc != null ? showDesc : TerritoryMessageSettings.getTerritoryDisplayMode(sender.getUniqueID()).showsDescription();
            if (mode.toUpperCase().equals("CHAT") && !newShowDesc) {
                mode += "_NODESC";
            }
            TerritoryDisplayMode newMode = TerritoryDisplayMode.valueOf(mode.toUpperCase());
            TerritoryMessageSettings.setTerritoryDisplayMode(sender.getUniqueID(), newMode);
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.territorymessagemode.success").setStyle(TextStyles.GREEN));
        } catch (IllegalArgumentException e) {
            throwWrongUsage(sender);
        }
    }
}
