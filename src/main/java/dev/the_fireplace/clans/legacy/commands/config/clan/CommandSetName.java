package dev.the_fireplace.clans.legacy.commands.config.clan;

import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanNames;
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
public class CommandSetName extends ClanSubCommand
{
    @Override
    public String getName() {
        return "setname";
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
        return 1;
    }

    @Override
    public void run(MinecraftServer server, ServerPlayerEntity sender, String[] args) {
        String newName = args[0];
        if (Config.getInstance().chatCensor.censorClanNames) {
            newName = ClansModContainer.getChatCensorCompat().getCensoredString(newName);
        }
        if (ClansModContainer.getConfig().getMaxNameLength() > 0 && newName.length() > ClansModContainer.getConfig().getMaxNameLength()) {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setname.toolong", ClansModContainer.getConfig().getMaxNameLength()).setStyle(TextStyles.RED));
        } else if (ClanNames.isClanNameAvailable(newName)) {
            String oldName = selectedClanName;
            ClanNames.get(selectedClan).setName(newName);
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setname.success", oldName, newName).setStyle(TextStyles.GREEN));
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setname.taken", newName).setStyle(TextStyles.RED));
        }
    }
}
