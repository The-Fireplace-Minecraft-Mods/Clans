package the_fireplace.clans.logic;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.Style;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.cache.PlayerAutoClaimData;
import the_fireplace.clans.cache.PlayerCache;
import the_fireplace.clans.cache.RaidingParties;
import the_fireplace.clans.data.*;
import the_fireplace.clans.model.*;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.FormulaParser;
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
                if(clan.isServer())
                    continue;
                if (Clans.getConfig().getChargeRentDays() > 0 && System.currentTimeMillis() >= clan.getNextRentTimestamp()) {
                    Clans.getMinecraftHelper().getLogger().debug("Charging rent for {}.", clan.getName());
                    for (Map.Entry<UUID, EnumRank> member : Sets.newHashSet(clan.getMembers().entrySet())) {
                        if (Clans.getPaymentHandler().deductAmount(clan.getRent(), member.getKey()))
                            Clans.getPaymentHandler().addAmount(clan.getRent(), clan.getId());
                        else if (Clans.getConfig().isEvictNonpayers())
                            if (member.getValue() != EnumRank.LEADER && (Clans.getConfig().isEvictNonpayerAdmins() || member.getValue() == EnumRank.MEMBER)) {
                                clan.removeMember(member.getKey());
                                EntityPlayerMP player = Clans.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(member.getKey());
                                //noinspection ConstantConditions
                                if (player != null) {
                                    PlayerData.updateDefaultClan(player.getUniqueID(), clan.getId());
                                    player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.rent.kicked", clan.getName()).setStyle(TextStyles.YELLOW));
                                }
                            }
                    }
                    clan.updateNextRentTimestamp();
                }
                if (Clans.getConfig().getClanUpkeepDays() > 0 && !clan.isUpkeepExempt() && System.currentTimeMillis() >= clan.getNextUpkeepTimestamp()) {
                    Clans.getMinecraftHelper().getLogger().debug("Charging upkeep for {}.", clan.getName());
                    double upkeep = FormulaParser.eval(Clans.getConfig().getClanUpkeepCostFormula(), clan, 0);
                    if(Clans.getConfig().isDisbandNoUpkeep() && upkeep > Clans.getPaymentHandler().getBalance(clan.getId()) && upkeep <= Clans.getPaymentHandler().getBalance(clan.getId()) + clan.getClaimCost() * clan.getClaimCount()) {
                        while(upkeep > Clans.getPaymentHandler().getBalance(clan.getId())) {
                            ArrayList<ChunkPositionWithData> chunks = Lists.newArrayList(ClaimData.getClaimedChunks(clan.getId()));
                            if(chunks.isEmpty())//This _should_ always be false, but just in case...
                                break;
                            ChunkPositionWithData pos = chunks.get(new Random().nextInt(chunks.size()));
                            ClanManagementLogic.abandonClaim(pos.getPosX(), pos.getPosZ(), pos.getDim(), clan);
                        }
                    }
                    if (Clans.getPaymentHandler().deductPartialAmount(upkeep, clan.getId()) > 0 && Clans.getConfig().isDisbandNoUpkeep())
                        clan.disband(Clans.getMinecraftHelper().getServer(), null, "clans.upkeep.disbanded", clan.getName());
                    else
                        clan.updateNextUpkeepTimestamp();
                }
            }
    }

    public static void runOneSecondLogic() {
        RaidingParties.decrementBuffers();
        PlayerCache.decrementHomeWarmupTimers();

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
        Clan c = ClaimData.getChunkClan(mob.chunkCoordX, mob.chunkCoordZ, mob.dimension);
        if(c != null) {
            if(Boolean.FALSE.equals(c.getMobSpawnOverride()))
                mob.onKillCommand();
            else if (Clans.getConfig().isPreventMobsOnClaims() && (Clans.getConfig().isPreventMobsOnBorderlands() || !Objects.requireNonNull(ClaimData.getChunkPositionData(mob.chunkCoordX, mob.chunkCoordZ, mob.dimension)).isBorderland()))
                mob.onKillCommand();
        }
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
        Collection<Clan> playerClans = ClanCache.getPlayerClans(player.getUniqueID());
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
            if(PlayerAutoClaimData.isOpAutoAbandoning(player.getUniqueID()))
                needsRecalc = ClanManagementLogic.checkAndAttemptAbandon((EntityPlayerMP) player, null);
            if(PlayerAutoClaimData.isAutoAbandoning(player.getUniqueID()))
                needsRecalc = ClanManagementLogic.checkAndAttemptAbandon((EntityPlayerMP) player, PlayerAutoClaimData.getAutoAbandoningClan(player.getUniqueID())) || needsRecalc;
            if(PlayerAutoClaimData.isOpAutoClaiming(player.getUniqueID()))
                needsRecalc = ClanManagementLogic.checkAndAttemptClaim((EntityPlayerMP) player, PlayerAutoClaimData.getOpAutoClaimingClan(player.getUniqueID()), true) || needsRecalc;
            if(PlayerAutoClaimData.isAutoClaiming(player.getUniqueID()))
                needsRecalc = ClanManagementLogic.checkAndAttemptClaim((EntityPlayerMP) player, PlayerAutoClaimData.getAutoClaimingClan(player.getUniqueID()), false) || needsRecalc;
            if(needsRecalc) {
                data = ClaimData.getChunkPositionData(player.chunkCoordX, player.chunkCoordZ, player.dimension);
                chunkClan = ClaimData.getChunkClan(data);
                chunkClanId = chunkClan != null ? chunkClan.getId() : null;
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
        TerritoryDisplayMode mode = PlayerData.getTerritoryDisplayMode(player.getUniqueID());
        if(!PlayerData.showUndergroundMessages(player.getUniqueID()) || mode.equals(TerritoryDisplayMode.OFF))
            return;
        int curY = (int) Math.round(player.posY);
        int prevY = PlayerCache.getPreviousY(player.getUniqueID());
        int yBound = (Clans.getConfig().getMinWildernessY() < 0 ? player.world.getSeaLevel() : Clans.getConfig().getMinWildernessY());
        if (curY >= yBound && prevY < yBound) {
            player.sendStatusMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entry", TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.wilderness")).setStyle(TextStyles.YELLOW), mode.isAction());
            if(mode.showsDescription())
                player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entrydesc", TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.protected")).setStyle(TextStyles.YELLOW));
        } else if (prevY >= yBound && curY < yBound) {
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entry", TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.underground")).setStyle(TextStyles.DARK_GREEN));
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entrydesc", TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.unclaimed")).setStyle(TextStyles.DARK_GREEN));
        }
        PlayerCache.setPreviousY(player.getUniqueID(), curY);
    }

    private static void handleTerritoryChangedMessage(EntityPlayer player, @Nullable Clan chunkClan, Collection<Clan> playerClans, boolean isBorderland) {
        PlayerCache.setPreviousChunkOwner(player.getUniqueID(), chunkClan != null ? chunkClan.getId() : null, isBorderland);
        TerritoryDisplayMode mode = PlayerData.getTerritoryDisplayMode(player.getUniqueID());
        if(mode.equals(TerritoryDisplayMode.OFF))
            return;
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
            territoryName = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.borderland", chunkClan.getName());
            territoryDesc = "";
        } else {
            territoryName = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.clanterritory", chunkClan.getName());
            territoryDesc = chunkClan.getDescription();
        }

        player.sendStatusMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entry", territoryName).setStyle(color), mode.isAction());
        if(!territoryDesc.isEmpty() && mode.showsDescription())
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entrydesc", territoryDesc).setStyle(color));
    }

    private static void checkAndResetClaimWarning(EntityPlayerMP player) {
        if(PlayerCache.getClaimWarning(player.getUniqueID()))
            PlayerCache.setClaimWarning(player.getUniqueID(), false);
    }

    private static void checkRaidAbandonmentTime(@Nullable UUID chunkClan, Collection<Clan> playerClans, EntityPlayer player) {
        for(Clan pc: playerClans)
            if (RaidingParties.hasActiveRaid(pc)) {
                Raid r = RaidingParties.getActiveRaid(pc);
                assert r != null;
                if(r.getDefenders().contains(player.getUniqueID()))
                    if (pc.getId().equals(chunkClan))
                        r.resetDefenderAbandonmentTime(player);
                    else
                        r.incrementDefenderAbandonmentTime(player);
            }
        if (RaidingParties.getRaidingPlayers().contains(player.getUniqueID())) {
            Raid r = RaidingParties.getRaid(player);
            assert r != null;
            if (r.isActive()) {
                if(r.getAttackers().contains(player.getUniqueID()))
                    if (r.getTarget().getId().equals(chunkClan))
                        r.resetAttackerAbandonmentTime(player);
                    else
                        r.incrementAttackerAbandonmentTime(player);
            }
        }
    }
}
