package dev.the_fireplace.clans.legacy.commands.land;

import dev.the_fireplace.clans.legacy.api.ClaimAccessor;
import dev.the_fireplace.clans.legacy.clan.metadata.ClanNames;
import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.logic.ClaimManagement;
import dev.the_fireplace.clans.legacy.model.ChunkPositionWithData;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandAbandonAll extends ClanSubCommand
{
    @Override
    public String getName() {
        return "abandonall";
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
        for (ChunkPositionWithData claim : ClaimAccessor.getInstance().getClaimedChunks(selectedClan)) {
            ClaimManagement.abandonClaim(claim.getPosX(), claim.getPosZ(), claim.getDim(), selectedClan);
        }
        sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.abandonall.success", ClanNames.get(selectedClan).getName()).setStyle(TextStyles.GREEN));
    }
}
