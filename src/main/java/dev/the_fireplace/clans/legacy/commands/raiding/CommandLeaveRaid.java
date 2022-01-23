package dev.the_fireplace.clans.legacy.commands.raiding;

import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.cache.RaidingParties;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanNames;
import dev.the_fireplace.clans.legacy.commands.RaidSubCommand;
import dev.the_fireplace.clans.legacy.model.Raid;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandLeaveRaid extends RaidSubCommand
{
    @Override
    public String getName() {
        return "leave";
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
        if (!RaidingParties.getRaidingPlayers().contains(sender.getUniqueID())) {
            Raid raid = RaidingParties.getRaid(sender);
            if (raid != null) {
                raid.removeAttacker(sender.getUniqueID());
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.leave.success", ClanNames.get(raid.getTarget()).getName()).setStyle(TextStyles.GREEN));
            } else {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.common.notinparty").setStyle(TextStyles.RED));
                ClansModContainer.getMinecraftHelper().getLogger().error("Player was in getRaidingPlayers but getRaid was null!");
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.raid.common.notinparty").setStyle(TextStyles.RED));
        }
    }
}
