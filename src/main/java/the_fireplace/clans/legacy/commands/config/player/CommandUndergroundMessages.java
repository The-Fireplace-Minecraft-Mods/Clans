package the_fireplace.clans.legacy.commands.config.player;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.player.TerritoryMessageSettings;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandUndergroundMessages extends ClanSubCommand
{
    @Override
    public String getName() {
        return "undergroundmessages";
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
        return 1;
    }

    @Override
    public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
        boolean showMessages = parseBool(args[0]);
        TerritoryMessageSettings.setShowUndergroundMessages(sender.getUniqueID(), showMessages);
        sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.undergroundmessages.success").setStyle(TextStyles.GREEN));
    }
}
