package the_fireplace.clans.logic;

import com.google.common.collect.Sets;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.Style;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.cache.PlayerCache;
import the_fireplace.clans.cache.RaidingParties;
import the_fireplace.clans.commands.teleportation.CommandHome;
import the_fireplace.clans.data.*;
import the_fireplace.clans.model.*;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.ClanManagementUtil;
import the_fireplace.clans.util.PermissionManager;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import java.util.*;

public class TimerLogic {
    public static void runFiveMinuteLogic() {
        ClaimData.save();
        ClanDatabase.save();
        RaidCollectionDatabase.save();
        RaidRestoreDatabase.save();
        PlayerData.save();
        PlayerCache.cleanup();
    }

    public static void runOneMinuteLogic() {
        for (Clan clan : ClanDatabase.getClans())
            clan.decrementShield();

        if (Clans.getConfig().getClanUpkeepDays() > 0 || Clans.getConfig().getChargeRentDays() > 0)
            for (Clan clan : ClanDatabase.getClans()) {
                if (Clans.getConfig().getChargeRentDays() > 0 && System.currentTimeMillis() >= clan.getNextRentTimestamp()) {
                    Clans.getMinecraftHelper().getLogger().debug("Charging rent for {}.", clan.getClanName());
                    for (Map.Entry<UUID, EnumRank> member : clan.getMembers().entrySet()) {
                        if (Clans.getPaymentHandler().deductAmount(clan.getRent(), member.getKey()))
                            Clans.getPaymentHandler().addAmount(clan.getRent(), clan.getClanId());
                        else if (Clans.getConfig().isEvictNonpayers())
                            if (member.getValue() != EnumRank.LEADER && (Clans.getConfig().isEvictNonpayerAdmins() || member.getValue() == EnumRank.MEMBER)) {
                                clan.removeMember(member.getKey());
                                EntityPlayerMP player = Clans.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(member.getKey());
                                //noinspection ConstantConditions
                                if (player != null) {
                                    PlayerData.updateDefaultClan(player.getUniqueID(), clan.getClanId());
                                    player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.rent.kicked", clan.getClanName()).setStyle(TextStyles.YELLOW));
                                }
                            }
                    }
                    clan.updateNextRentTimeStamp();
                }
                if (Clans.getConfig().getClanUpkeepDays() > 0 && System.currentTimeMillis() >= clan.getNextUpkeepTimestamp()) {
                    Clans.getMinecraftHelper().getLogger().debug("Charging upkeep for {}.", clan.getClanName());
                    int upkeep = Clans.getConfig().getClanUpkeepCost();
                    if (Clans.getConfig().isMultiplyUpkeepMembers())
                        upkeep *= clan.getMemberCount();
                    if (Clans.getConfig().isMultiplyUpkeepClaims())
                        upkeep *= clan.getClaimCount();
                    if (Clans.getPaymentHandler().deductPartialAmount(upkeep, clan.getClanId()) > 0 && Clans.getConfig().isDisbandNoUpkeep())
                        clan.disband(Clans.getMinecraftHelper().getServer(), null, "clans.upkeep.disbanded", clan.getClanName());
                    else
                        clan.updateNextUpkeepTimeStamp();
                }
            }
    }

    public static void runOneSecondLogic() {
        RaidingParties.decrementBuffers();
        for(Map.Entry<EntityPlayerMP, OrderedPair<Integer, UUID>> entry : Sets.newHashSet(PlayerCache.clanHomeWarmups.entrySet())) {
            if (entry.getValue().getValue1() > 0) {
                if(Math.abs(entry.getKey().posX - PlayerCache.getClanHomeCheckX(entry.getKey().getUniqueID())) < 0.1f && Math.abs(entry.getKey().posZ - PlayerCache.getClanHomeCheckZ(entry.getKey().getUniqueID())) < 0.1f && Math.abs(entry.getKey().posY- PlayerCache.getClanHomeCheckY(entry.getKey().getUniqueID())) < 0.1f)
                    PlayerCache.clanHomeWarmups.put(entry.getKey(), new OrderedPair<>(entry.getValue().getValue1() - 1, entry.getValue().getValue2()));
                else {
                    entry.getKey().sendMessage(TranslationUtil.getTranslation(entry.getKey().getUniqueID(), "commands.clan.home.cancelled").setStyle(TextStyles.RED));
                    PlayerCache.clanHomeWarmups.remove(entry.getKey());
                }
            } else
                PlayerCache.clanHomeWarmups.remove(entry.getKey());

            if (entry.getValue().getValue1() == 0 && entry.getKey() != null && entry.getKey().isEntityAlive()) {
                Clan c = ClanCache.getClanById(entry.getValue().getValue2());
                //Ensure that the clan still has a home and that the player is still in the clan before teleporting.
                if(c != null && c.getHome() != null && c.getMembers().containsKey(entry.getKey().getUniqueID()))
                    CommandHome.teleportHome(entry.getKey(), c, c.getHome(), entry.getKey().dimension, false);
                else
                    entry.getKey().sendMessage(TranslationUtil.getTranslation(entry.getKey().getUniqueID(), "commands.clan.home.cancelled").setStyle(TextStyles.RED));
            }
        }

        for (Raid raid : RaidingParties.getActiveRaids())
            if (raid.checkRaidEndTimer())
                raid.defenderVictory();

        ClaimData.decrementBorderlandsRegenTimers();
    }

    public static void runTwoSecondLogic() {
        for(Raid raid: RaidingParties.getActiveRaids())
            RaidManagementLogic.checkAndRemoveForbiddenItems(Clans.getMinecraftHelper().getServer(), raid);
    }

    public static void runMobFiveSecondLogic(EntityLivingBase mob) {
        if(Clans.getConfig().isPreventMobsOnClaims() && ClaimData.getChunkClan(mob.chunkCoordX, mob.chunkCoordZ, mob.dimension) != null && (Clans.getConfig().isPreventMobsOnBorderlands() || !ClaimData.getChunkPositionData(mob.chunkCoordX, mob.chunkCoordZ, mob.dimension).isBorderland()))
            mob.setDead();
    }

    public static void runPlayerSecondLogic(EntityPlayer player) {
        int cooldown = PlayerData.getCooldown(player.getUniqueID());
        if(cooldown > 0)
            PlayerData.setCooldown(player.getUniqueID(), cooldown - 1);
        checkRaidAbandonmentTime(ClaimData.getChunkClanId(player.chunkCoordX, player.chunkCoordZ, player.dimension), ClanCache.getPlayerClans(player.getUniqueID()), player);
    }

    public static void runPlayerHalfSecondLogic(EntityPlayer player) {
        Chunk c = player.getEntityWorld().getChunk(player.getPosition());
        UUID chunkClanId = ChunkUtils.getChunkOwner(c);
        List<Clan> playerClans = ClanCache.getPlayerClans(player.getUniqueID());
        UUID playerStoredClaimId = PlayerCache.getPreviousChunkOwner(player.getUniqueID());
        Clan chunkClan = ClanCache.getClanById(chunkClanId);
        ChunkPositionWithData data = ClaimData.getChunkPositionData(player.chunkCoordX, player.chunkCoordZ, player.dimension);
        boolean isInBorderland = data != null && data.isBorderland();
        boolean playerStoredIsInBorderland = PlayerCache.getStoredIsInBorderland(player.getUniqueID());
        if (chunkClanId != null && chunkClan == null) {
            ChunkUtils.clearChunkOwner(c);
            chunkClanId = null;
        }

        if (!Objects.equals(chunkClanId, playerStoredClaimId) || (isInBorderland != playerStoredIsInBorderland)) {
            boolean needsRecalc = false;
            if(ClanCache.opAutoAbandonClaims.contains(player.getUniqueID()))
                needsRecalc = ClanManagementUtil.checkAndAttemptAbandon((EntityPlayerMP) player, null);
            if(ClanCache.autoAbandonClaims.containsKey(player.getUniqueID()))
                needsRecalc = ClanManagementUtil.checkAndAttemptAbandon((EntityPlayerMP) player, ClanCache.autoAbandonClaims.get(player.getUniqueID())) || needsRecalc;
            if(ClanCache.opAutoClaimLands.containsKey(player.getUniqueID()))
                needsRecalc = ClanManagementUtil.checkAndAttemptClaim((EntityPlayerMP) player, ClanCache.opAutoClaimLands.get(player.getUniqueID()), true) || needsRecalc;
            if(ClanCache.autoClaimLands.containsKey(player.getUniqueID()))
                needsRecalc = ClanManagementUtil.checkAndAttemptClaim((EntityPlayerMP) player, ClanCache.autoClaimLands.get(player.getUniqueID()), false) || needsRecalc;
            if(needsRecalc) {
                data = ClaimData.getChunkPositionData(player.chunkCoordX, player.chunkCoordZ, player.dimension);
                chunkClan = ClaimData.getChunkClan(data);
                chunkClanId = chunkClan != null ? chunkClan.getClanId() : null;
                isInBorderland = data != null && data.isBorderland();
            }

            if(!Objects.equals(chunkClanId, playerStoredClaimId) || (isInBorderland != playerStoredIsInBorderland))
                handleTerritoryChangedMessage(player, chunkClan, playerClans, isInBorderland);
            PlayerCache.setPreviousY(player.getUniqueID(), (int) Math.round(player.posY));
        } else if (chunkClanId == null && Clans.getConfig().isProtectWilderness() && Clans.getConfig().getMinWildernessY() > 0 && player.getEntityWorld().getTotalWorldTime() % 20 == 0 && !(PermissionManager.permissionManagementExists() && PermissionManager.hasPermission(player, PermissionManager.PROTECTION_PREFIX+"break.protected_wilderness") && PermissionManager.hasPermission(player, PermissionManager.PROTECTION_PREFIX+"build.protected_wilderness")))
            handleDepthChangedMessage(player);
        EntityPlayerMP playerMP = player instanceof EntityPlayerMP ? (EntityPlayerMP) player : null;
        if (playerMP != null) {
            if(PlayerCache.getPreviousChunkX(player.getUniqueID()) != c.x || PlayerCache.getPreviousChunkZ(player.getUniqueID()) != c.z) {
                checkAndResetClaimWarning(playerMP);
                if(PlayerCache.getIsShowingChunkBorders(player.getUniqueID()))
                    ChunkUtils.showChunkBounds(c, playerMP);

                PlayerCache.setPreviousChunkX(player.getUniqueID(), c.x);
                PlayerCache.setPreviousChunkZ(player.getUniqueID(), c.z);
            }
        }
    }

    private static void handleDepthChangedMessage(EntityPlayer player) {
        int curY = (int) Math.round(player.posY);
        int prevY = PlayerCache.getPreviousY(player.getUniqueID());
        int yBound = (Clans.getConfig().getMinWildernessY() < 0 ? player.world.getSeaLevel() : Clans.getConfig().getMinWildernessY());
        if (curY >= yBound && prevY < yBound) {
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entry", TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.wilderness")).setStyle(TextStyles.YELLOW));
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entrydesc", TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.protected")).setStyle(TextStyles.YELLOW));
        } else if (prevY >= yBound && curY < yBound) {
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entry", TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.underground")).setStyle(TextStyles.DARK_GREEN));
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entrydesc", TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.unclaimed")).setStyle(TextStyles.DARK_GREEN));
        }
        PlayerCache.setPreviousY(player.getUniqueID(), curY);
    }

    private static void handleTerritoryChangedMessage(EntityPlayer player, @Nullable Clan chunkClan, Collection<Clan> playerClans, boolean isBorderland) {
        PlayerCache.setPreviousChunkOwner(player.getUniqueID(), chunkClan != null ? chunkClan.getClanId() : null, isBorderland);
        Style color = TextStyles.GREEN;
        if ((!playerClans.isEmpty() && !playerClans.contains(chunkClan)) || (playerClans.isEmpty() && chunkClan != null))
            color = TextStyles.YELLOW;
        if (chunkClan == null)
            color = TextStyles.DARK_GREEN;
        String territoryName;
        String territoryDesc;
        if (chunkClan == null) {
            boolean canBuildInWilderness = PermissionManager.permissionManagementExists() && PermissionManager.hasPermission(player, PermissionManager.PROTECTION_PREFIX+"break.protected_wilderness") && PermissionManager.hasPermission(player, PermissionManager.PROTECTION_PREFIX+"build.protected_wilderness");
            if (Clans.getConfig().isProtectWilderness() && (Clans.getConfig().getMinWildernessY() < 0 ? player.posY < player.world.getSeaLevel() : player.posY < Clans.getConfig().getMinWildernessY()) && !canBuildInWilderness) {
                territoryName = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.underground");
                territoryDesc = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.unclaimed");
            } else {
                territoryName = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.wilderness");
                if(Clans.getConfig().isProtectWilderness() && !canBuildInWilderness) {
                    color = TextStyles.YELLOW;
                    territoryDesc = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.protected");
                } else
                    territoryDesc = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.unclaimed");
            }
        } else if(isBorderland) {
            territoryName = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.borderland", chunkClan.getClanName());
            territoryDesc = "";
        } else {
            territoryName = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.clanterritory", chunkClan.getClanName());
            territoryDesc = chunkClan.getDescription();
        }

        player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entry", territoryName).setStyle(color));
        if(!territoryDesc.isEmpty())
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entrydesc", territoryDesc).setStyle(color));
    }

    private static void checkAndResetClaimWarning(EntityPlayerMP player) {
        if(PlayerCache.getClaimWarning(player.getUniqueID())) {
            PlayerCache.setClaimWarning(player.getUniqueID(), false);
        }
    }

    private static void checkRaidAbandonmentTime(@Nullable UUID chunkClan, Collection<Clan> playerClans, EntityPlayer player) {
        for(Clan pc: playerClans)
            if (RaidingParties.hasActiveRaid(pc)) {
                Raid r = RaidingParties.getActiveRaid(pc);
                if(r.getDefenders().contains(player.getUniqueID()))
                    if (pc.getClanId().equals(chunkClan))
                        r.resetDefenderAbandonmentTime(player);
                    else
                        r.incrementDefenderAbandonmentTime(player);
            }
        if (RaidingParties.getRaidingPlayers().contains(player.getUniqueID())) {
            Raid r = RaidingParties.getRaid(player);
            if (r.isActive()) {
                if(r.getAttackers().contains(player.getUniqueID()))
                    if (r.getTarget().getClanId().equals(chunkClan))
                        r.resetAttackerAbandonmentTime(player);
                    else
                        r.incrementAttackerAbandonmentTime(player);
            }
        }
    }
}
