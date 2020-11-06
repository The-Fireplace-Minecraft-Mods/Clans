package the_fireplace.clans.legacy.logic;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.Style;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.clan.economics.ClanClaimCosts;
import the_fireplace.clans.clan.economics.ClanRent;
import the_fireplace.clans.clan.economics.ClanUpkeep;
import the_fireplace.clans.clan.land.ClanClaims;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.membership.PlayerClans;
import the_fireplace.clans.clan.raids.ClanShield;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.cache.PlayerAutoClaimData;
import the_fireplace.clans.legacy.cache.PlayerCache;
import the_fireplace.clans.legacy.cache.RaidingParties;
import the_fireplace.clans.legacy.data.ClaimData;
import the_fireplace.clans.legacy.data.RaidCollectionDatabase;
import the_fireplace.clans.legacy.data.RaidRestoreDatabase;
import the_fireplace.clans.legacy.model.ChunkPositionWithData;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.model.Raid;
import the_fireplace.clans.legacy.model.TerritoryDisplayMode;
import the_fireplace.clans.legacy.util.ChunkUtils;
import the_fireplace.clans.legacy.util.FormulaParser;
import the_fireplace.clans.legacy.util.PermissionManager;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.player.PlayerClanSettings;
import the_fireplace.clans.player.PlayerDataStorage;
import the_fireplace.clans.player.TerritoryMessageSettings;

import javax.annotation.Nullable;
import java.util.*;

public class TimerLogic {
    public static void runFiveMinuteLogic() {
        ClaimData.save();
        ClanDatabase.save();
        RaidCollectionDatabase.getInstance().save();
        RaidRestoreDatabase.getInstance().save();
        PlayerDataStorage.save();
        PlayerCache.cleanup();
    }

    public static void runOneMinuteLogic() {
        decrementShields();

        chargeRentAndUpkeep();
    }

    private static void chargeRentAndUpkeep() {
        if (ClansModContainer.getConfig().getClanUpkeepDays() > 0 || ClansModContainer.getConfig().getChargeRentDays() > 0)
            for (Clan clan : ClanDatabase.getClans()) {
                if(AdminControlledClanSettings.get().isServerOwned())
                    continue;
                chargeRent(clan);
                chargeUpkeep(clan);
            }
    }

    private static void chargeUpkeep(Clan clan) {
        if (ClansModContainer.getConfig().getClanUpkeepDays() > 0 && !AdminControlledClanSettings.get().isUpkeepExempt() && System.currentTimeMillis() >= ClanUpkeep.get().getNextUpkeepTimestamp()) {
            ClansModContainer.getMinecraftHelper().getLogger().debug("Charging upkeep for {}.", clan.getClanMetadata().getClanName());
            double upkeep = FormulaParser.eval(ClansModContainer.getConfig().getClanUpkeepCostFormula(), clan, 0);
            if(ClansModContainer.getConfig().isDisbandNoUpkeep() && upkeep > Economy.getBalance(clan.getClanMetadata().getClanId()) && upkeep <= Economy.getBalance(clan.getClanMetadata().getClanId()) + ClanClaimCosts.get().getNextClaimCost(ClanClaims.get().getClaimCount()) * ClanClaims.get().getClaimCount()) {
                while(upkeep > Economy.getBalance(clan.getClanMetadata().getClanId())) {
                    ArrayList<ChunkPositionWithData> chunks = Lists.newArrayList(ClaimData.getClaimedChunks(clan.getClanMetadata().getClanId()));
                    if(chunks.isEmpty())//This _should_ always be false, but just in case...
                        break;
                    ChunkPositionWithData pos = chunks.get(new Random().nextInt(chunks.size()));
                    ClaimManagement.abandonClaim(pos.getPosX(), pos.getPosZ(), pos.getDim(), clan);
                }
            }
            if (Economy.deductPartialAmount(upkeep, clan.getClanMetadata().getClanId()) > 0 && ClansModContainer.getConfig().isDisbandNoUpkeep())
                clan.disband(ClansModContainer.getMinecraftHelper().getServer(), null, "clans.upkeep.disbanded", clan.getClanMetadata().getClanName());
            else
                ClanUpkeep.get().updateNextUpkeepTimestamp();
        }
    }

    private static void chargeRent(Clan clan) {
        if (ClansModContainer.getConfig().getChargeRentDays() > 0 && System.currentTimeMillis() >= ClanRent.get().getNextRentTimestamp()) {
            ClansModContainer.getMinecraftHelper().getLogger().debug("Charging rent for {}.", clan.getClanMetadata().getClanName());
            for (Map.Entry<UUID, EnumRank> member : Sets.newHashSet(ClanMembers.get().getMemberRanks().entrySet())) {
                if (Economy.deductAmount(ClanRent.get().getRent(), member.getKey()))
                    Economy.addAmount(ClanRent.get().getRent(), clan.getClanMetadata().getClanId());
                else if (ClansModContainer.getConfig().isEvictNonpayers())
                    if (member.getValue() != EnumRank.LEADER && (ClansModContainer.getConfig().isEvictNonpayerAdmins() || member.getValue() == EnumRank.MEMBER)) {
                        ClanMembers.get().removeMember(member.getKey());
                        EntityPlayerMP player = ClansModContainer.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(member.getKey());
                        //noinspection ConstantConditions
                        if (player != null) {
                            PlayerClanSettings.updateDefaultClanIfNeeded(player.getUniqueID(), clan.getClanMetadata().getClanId());
                            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.rent.kicked", clan.getClanMetadata().getClanName()).setStyle(TextStyles.YELLOW));
                        }
                    }
            }
            ClanRent.get().updateNextRentTimestamp();
        }
    }

    private static void decrementShields() {
        for (Clan clan : ClanDatabase.getClans())
            ClanShield.get().decrementShield();
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
            RaidManagementLogic.checkAndRemoveForbiddenItems(ClansModContainer.getMinecraftHelper().getServer(), raid);
    }

    public static void runMobFiveSecondLogic(EntityLivingBase mob) {
        Clan c = ClaimData.getChunkClan(mob.chunkCoordX, mob.chunkCoordZ, mob.dimension);
        if(c != null) {
            if(Boolean.FALSE.equals(AdminControlledClanSettings.get().getMobSpawnOverride()))
                mob.onKillCommand();
            else if (ClansModContainer.getConfig().isPreventMobsOnClaims() && (ClansModContainer.getConfig().isPreventMobsOnBorderlands() || !Objects.requireNonNull(ClaimData.getChunkPositionData(mob.chunkCoordX, mob.chunkCoordZ, mob.dimension)).isBorderland()))
                mob.onKillCommand();
        }
    }

    public static void runPlayerSecondLogic(EntityPlayer player) {
        checkRaidAbandonmentTime(ClaimData.getChunkClan(player.chunkCoordX, player.chunkCoordZ, player.dimension), PlayerClans.getClansPlayerIsIn(player.getUniqueID()), player);
    }

    public static void runPlayerHalfSecondLogic(EntityPlayer player) {
        Chunk c = player.getEntityWorld().getChunk(player.getPosition());
        UUID chunkClanId = ChunkUtils.getChunkOwner(c);
        Collection<Clan> playerClans = PlayerClans.getClansPlayerIsIn(player.getUniqueID());
        UUID playerStoredClaimId = PlayerCache.getPreviousChunkOwner(player.getUniqueID());
        Clan chunkClan = ClanDatabase.getClanById(chunkClanId);
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
                needsRecalc = ClaimManagement.checkAndAttemptAbandon((EntityPlayerMP) player, null);
            if(PlayerAutoClaimData.isAutoAbandoning(player.getUniqueID()))
                needsRecalc = ClaimManagement.checkAndAttemptAbandon((EntityPlayerMP) player, PlayerAutoClaimData.getAutoAbandoningClan(player.getUniqueID())) || needsRecalc;
            if(PlayerAutoClaimData.isOpAutoClaiming(player.getUniqueID()))
                needsRecalc = ClaimManagement.checkAndAttemptClaim((EntityPlayerMP) player, PlayerAutoClaimData.getOpAutoClaimingClan(player.getUniqueID()), true) || needsRecalc;
            if(PlayerAutoClaimData.isAutoClaiming(player.getUniqueID()))
                needsRecalc = ClaimManagement.checkAndAttemptClaim((EntityPlayerMP) player, PlayerAutoClaimData.getAutoClaimingClan(player.getUniqueID()), false) || needsRecalc;
            if(needsRecalc) {
                data = ClaimData.getChunkPositionData(player.chunkCoordX, player.chunkCoordZ, player.dimension);
                chunkClan = ClaimData.getChunkClan(data);
                chunkClanId = chunkClan != null ? chunkClan.getClanMetadata().getClanId() : null;
                isInBorderland = data != null && data.isBorderland();
            }

            if(!Objects.equals(chunkClanId, playerStoredClaimId) || (isInBorderland != playerStoredIsInBorderland))
                handleTerritoryChangedMessage(player, chunkClan, playerClans, isInBorderland);
            PlayerCache.setPreviousY(player.getUniqueID(), (int) Math.round(player.posY));
        } else if (chunkClanId == null && ClansModContainer.getConfig().shouldProtectWilderness() && ClansModContainer.getConfig().getMinWildernessY() > 0 && player.getEntityWorld().getTotalWorldTime() % 20 == 0 && !(PermissionManager.permissionManagementExists() && PermissionManager.hasPermission(player, PermissionManager.PROTECTION_PREFIX+"break.protected_wilderness") && PermissionManager.hasPermission(player, PermissionManager.PROTECTION_PREFIX+"build.protected_wilderness")))
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
        TerritoryDisplayMode mode = TerritoryMessageSettings.getTerritoryDisplayMode(player.getUniqueID());
        if(!TerritoryMessageSettings.isShowingUndergroundMessages(player.getUniqueID()) || mode.equals(TerritoryDisplayMode.OFF))
            return;
        int curY = (int) Math.round(player.posY);
        int prevY = PlayerCache.getPreviousY(player.getUniqueID());
        int yBound = (ClansModContainer.getConfig().getMinWildernessY() < 0 ? player.world.getSeaLevel() : ClansModContainer.getConfig().getMinWildernessY());
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
        PlayerCache.setPreviousChunkOwner(player.getUniqueID(), chunkClan != null ? chunkClan.getClanMetadata().getClanId() : null, isBorderland);
        TerritoryDisplayMode mode = TerritoryMessageSettings.getTerritoryDisplayMode(player.getUniqueID());
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
            if (ClansModContainer.getConfig().shouldProtectWilderness() && (ClansModContainer.getConfig().getMinWildernessY() < 0 ? player.posY < player.world.getSeaLevel() : player.posY < ClansModContainer.getConfig().getMinWildernessY()) && !canBuildInWilderness) {
                territoryName = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.underground");
                territoryDesc = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.unclaimed");
            } else {
                territoryName = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.wilderness");
                if(ClansModContainer.getConfig().shouldProtectWilderness() && !canBuildInWilderness) {
                    color = TextStyles.YELLOW;
                    territoryDesc = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.protected");
                } else
                    territoryDesc = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.unclaimed");
            }
        } else if(isBorderland) {
            territoryName = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.borderland", chunkClan.getClanMetadata().getClanName());
            territoryDesc = "";
        } else {
            territoryName = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.clanterritory", chunkClan.getClanMetadata().getClanName());
            territoryDesc = chunkClan.getClanMetadata().getDescription();
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
                    if (pc.getClanMetadata().getClanId().equals(chunkClan))
                        r.resetDefenderAbandonmentTime(player);
                    else
                        r.incrementDefenderAbandonmentTime(player);
            }
        if (RaidingParties.getRaidingPlayers().contains(player.getUniqueID())) {
            Raid r = RaidingParties.getRaid(player);
            assert r != null;
            if (r.isActive()) {
                if(r.getAttackers().contains(player.getUniqueID()))
                    if (r.getTarget().getClanMetadata().getClanId().equals(chunkClan))
                        r.resetAttackerAbandonmentTime(player);
                    else
                        r.incrementAttackerAbandonmentTime(player);
            }
        }
    }
}
