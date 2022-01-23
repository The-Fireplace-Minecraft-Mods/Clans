package dev.the_fireplace.clans.legacy.commands.teleportation;

import dev.the_fireplace.clans.legacy.ClansModContainer;
import dev.the_fireplace.clans.legacy.cache.PlayerCache;
import dev.the_fireplace.clans.legacy.clan.home.ClanHomes;
import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.player.PlayerHomeCooldown;
import dev.the_fireplace.clans.legacy.util.EntityUtil;
import dev.the_fireplace.clans.legacy.util.TextStyles;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandHome extends ClanSubCommand
{
    @Override
    public String getName() {
        return "home";
    }

    @Override
    public EnumRank getRequiredClanRank() {
        return EnumRank.MEMBER;
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
    public void run(MinecraftServer server, ServerPlayerEntity sender, String[] args) throws CommandException {
        if (ClansModContainer.getConfig().getClanHomeWarmupTime() <= -1) {
            throw new CommandException(TranslationUtil.getRawTranslationString(sender, "commands.clan.home.disabled"));
        }

        boolean isCoolingDown = PlayerHomeCooldown.isCoolingDown(sender.getUniqueID());
        if (!isCoolingDown || sender.isCreative()) {
            if (!ClanHomes.hasHome(selectedClan)) {
                sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.home.nohome", selectedClanName).setStyle(TextStyles.RED));
            } else {
                if (ClansModContainer.getConfig().getClanHomeWarmupTime() > 0 && !sender.isCreative()) {
                    sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.home.warmup", selectedClanName, ClansModContainer.getConfig().getClanHomeWarmupTime()).setStyle(TextStyles.GREEN));
                    PlayerCache.setClanHomeCheckX(sender.getUniqueID(), (float) sender.posX);
                    PlayerCache.setClanHomeCheckY(sender.getUniqueID(), (float) sender.posY);
                    PlayerCache.setClanHomeCheckZ(sender.getUniqueID(), (float) sender.posZ);
                    PlayerCache.startHomeTeleportWarmup(sender, selectedClan);
                } else {
                    BlockPos home = ClanHomes.get(selectedClan).toBlockPos();
                    int playerDim = sender.dimension;
                    EntityUtil.teleportHome(sender, home, ClanHomes.get(selectedClan).getHomeDim(), playerDim, false);
                }
            }
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.home.cooldown", PlayerHomeCooldown.getCooldown(sender.getUniqueID())).setStyle(TextStyles.RED));
        }
    }
}
