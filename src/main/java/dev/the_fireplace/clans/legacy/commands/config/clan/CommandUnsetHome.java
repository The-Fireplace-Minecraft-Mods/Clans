package dev.the_fireplace.clans.legacy.commands.config.clan;

import dev.the_fireplace.clans.legacy.clan.home.ClanHomes;
import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandUnsetHome extends ClanSubCommand
{
    @Override
    public String getName() {
        return "unsethome";
    }

    @Override
    public EnumRank getRequiredClanRank() {
        return EnumRank.LEADER;
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
        if (ClanHomes.hasHome(selectedClan)) {
            ClanHomes.delete(selectedClan);
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.unsethome.success", selectedClanName).setStyle(TextStyles.GREEN));
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.unsethome.failed", selectedClanName).setStyle(TextStyles.RED));
        }
    }
}
