package dev.the_fireplace.clans.legacy.commands.land;

import dev.the_fireplace.clans.legacy.clan.metadata.ClanNames;
import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.logic.ClaimManagement;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.player.autoland.AutoClaim;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandAutoClaim extends ClanSubCommand
{
    @Override
    public String getName() {
        return "autoclaim";
    }

    @Override
    public EnumRank getRequiredClanRank() {
        return EnumRank.ADMIN;
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
        UUID rm = AutoClaim.cancelAutoClaim(sender.getUniqueID());
        if (rm == null) {
            AutoClaim.activateAutoClaim(sender.getUniqueID(), selectedClan);
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autoclaim.start", selectedClanName).setStyle(TextStyles.GREEN));
            ClaimManagement.checkAndAttemptClaim(sender, selectedClan);
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autoclaim.stop", ClanNames.get(rm).getName()).setStyle(TextStyles.GREEN));
        }
    }
}
