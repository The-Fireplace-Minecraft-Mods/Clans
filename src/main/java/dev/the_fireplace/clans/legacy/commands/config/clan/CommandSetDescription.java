package dev.the_fireplace.clans.legacy.commands.config.clan;

import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanDescriptions;
import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.config.Config;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSetDescription extends ClanSubCommand
{
    @Override
    public String getName() {
        return "setdescription";
    }

    @Override
    public EnumRank getRequiredClanRank() {
        return EnumRank.LEADER;
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void run(MinecraftServer server, ServerPlayerEntity sender, String[] args) {
        StringBuilder newTagline = new StringBuilder();
        for (String arg : args) {
            newTagline.append(arg).append(' ');
        }
        String descString = newTagline.toString();
        if (Config.getInstance().chatCensor.censorClanDescriptions) {
            descString = ClansModContainer.getChatCensorCompat().getCensoredString(descString);
        }
        ClanDescriptions.get(selectedClan).setDescription(descString);
        sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setdescription.success", selectedClanName).setStyle(TextStyles.GREEN));
    }
}
