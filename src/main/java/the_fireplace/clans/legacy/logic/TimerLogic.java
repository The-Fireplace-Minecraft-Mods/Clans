package the_fireplace.clans.legacy.logic;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.Style;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.clan.ClanDisbander;
import the_fireplace.clans.clan.ClanIdRegistry;
import the_fireplace.clans.clan.ClanSaver;
import the_fireplace.clans.clan.admin.AdminControlledClanSettings;
import the_fireplace.clans.clan.economics.ClanClaimCosts;
import the_fireplace.clans.clan.economics.ClanRent;
import the_fireplace.clans.clan.economics.ClanUpkeep;
import the_fireplace.clans.clan.land.ClanClaims;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.membership.PlayerClans;
import the_fireplace.clans.clan.metadata.ClanDescriptions;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.clan.raids.ClanShield;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.ClansModContainer;
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
import the_fireplace.clans.player.autoland.AutoAbandon;
import the_fireplace.clans.player.autoland.AutoClaim;
import the_fireplace.clans.player.autoland.OpAutoAbandon;
import the_fireplace.clans.player.autoland.OpAutoClaim;

import javax.annotation.Nullable;
import java.util.*;

public class TimerLogic {
    public static void runFiveMinuteLogic() {
        ClanIdRegistry.saveInstance();
        ClaimData.save();
        ClanSaver.saveAll();
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
            for (UUID clan : ClanIdRegistry.getIds()) {
                if(AdminControlledClanSettings.get(clan).isServerOwned())
                    continue;
                chargeRent(clan);
                chargeUpkeep(clan);
            }
    }

    private static void chargeUpkeep(UUID clan) {
        if (ClansModContainer.getConfig().getClanUpkeepDays() > 0 && !AdminControlledClanSettings.get(clan).isUpkeepExempt() && System.currentTimeMillis() >= ClanUpkeep.get(clan).getNextUpkeepTimestamp()) {
            ClansModContainer.getMinecraftHelper().getLogger().debug("Charging upkeep for {}.", ClanNames.get(clan).getName());
            double upkeep = FormulaParser.eval(ClansModContainer.getConfig().getClanUpkeepCostFormula(), clan, 0);
            if(ClansModContainer.getConfig().isDisbandNoUpkeep() && upkeep > Economy.getBalance(clan) && upkeep <= Economy.getBalance(clan) + ClanClaimCosts.get(clan).getNextClaimCost(ClanClaims.get(clan).getClaimCount()) * ClanClaims.get(clan).getClaimCount()) {
                while(upkeep > Economy.getBalance(clan)) {
                    ArrayList<ChunkPositionWithData> chunks = Lists.newArrayList(ClaimData.getClaimedChunks(clan));
                    if(chunks.isEmpty())//This _should_ always be false, but just in case...
                        break;
                    ChunkPositionWithData pos = chunks.get(new Random().nextInt(chunks.size()));
                    ClaimManagement.abandonClaim(pos.getPosX(), pos.getPosZ(), pos.getDim(), clan);
                }
            }
            if (Economy.deductPartialAmount(upkeep, clan) > 0 && ClansModContainer.getConfig().isDisbandNoUpkeep())
                ClanDisbander.create(clan).disband(null, "clans.upkeep.disbanded", ClanNames.get(clan).getName());
            else
                ClanUpkeep.get(clan).updateNextUpkeepTimestamp();
        }
    }

    private static void chargeRent(UUID clan) {
        if (ClansModContainer.getConfig().getChargeRentDays() > 0 && System.currentTimeMillis() >= ClanRent.get(clan).getNextRentTimestamp()) {
            ClansModContainer.getMinecraftHelper().getLogger().debug("Charging rent for {}.", ClanNames.get(clan).getName());
            for (Map.Entry<UUID, EnumRank> member : Sets.newHashSet(ClanMembers.get(clan).getMemberRanks().entrySet())) {
                if (Economy.deductAmount(ClanRent.get(clan).getRent(), member.getKey()))
                    Economy.addAmount(ClanRent.get(clan).getRent(), clan);
                else if (ClansModContainer.getConfig().isEvictNonpayers())
                    if (member.getValue() != EnumRank.LEADER && (ClansModContainer.getConfig().isEvictNonpayerAdmins() || member.getValue() == EnumRank.MEMBER)) {
                        ClanMembers.get(clan).removeMember(member.getKey());
                        EntityPlayerMP player = ClansModContainer.getMinecraftHelper().getServer().getPlayerList().getPlayerByUUID(member.getKey());
                        //noinspection ConstantConditions
                        if (player != null) {
                            PlayerClanSettings.updateDefaultClanIfNeeded(player.getUniqueID(), clan);
                            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.rent.kicked", ClanNames.get(clan).getName()).setStyle(TextStyles.YELLOW));
                        }
                    }
            }
            ClanRent.get(clan).updateNextRentTimestamp();
        }
    }

    private static void decrementShields() {
        for (UUID clan : ClanIdRegistry.getIds())
            ClanShield.get(clan).decrementShield();
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
        UUID clan = ClaimData.getChunkClan(mob.chunkCoordX, mob.chunkCoordZ, mob.dimension);
        if(clan != null) {
            if(Boolean.FALSE.equals(AdminControlledClanSettings.get(clan).getMobSpawnOverride()))
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
        UUID chunkClan = ChunkUtils.getChunkOwner(c);
        Collection<UUID> playerClans = PlayerClans.getClansPlayerIsIn(player.getUniqueID());
        UUID playerStoredClaimId = PlayerCache.getPreviousChunkOwner(player.getUniqueID());
        ChunkPositionWithData data = ClaimData.getChunkPositionData(player.chunkCoordX, player.chunkCoordZ, player.dimension);
        boolean isInBorderland = data != null && data.isBorderland();
        boolean playerStoredIsInBorderland = PlayerCache.getStoredIsInBorderland(player.getUniqueID());
        if (chunkClan != null && !ClanIdRegistry.isValidClan(chunkClan)) {
            ChunkUtils.clearChunkOwner(c);
            chunkClan = null;
        }

        if (!Objects.equals(chunkClan, playerStoredClaimId) || (isInBorderland != playerStoredIsInBorderland)) {
            boolean needsRecalc = false;
            if(OpAutoAbandon.isOpAutoAbandoning(player.getUniqueID()))
                needsRecalc = ClaimManagement.checkAndAttemptAbandon((EntityPlayerMP) player, null);
            if(AutoAbandon.isAutoAbandoning(player.getUniqueID()))
                needsRecalc = ClaimManagement.checkAndAttemptAbandon((EntityPlayerMP) player, AutoAbandon.getAutoAbandoningClan(player.getUniqueID())) || needsRecalc;
            if(OpAutoClaim.isAutoClaiming(player.getUniqueID()))
                needsRecalc = ClaimManagement.checkAndAttemptClaim((EntityPlayerMP) player, OpAutoClaim.getAutoClaimingClan(player.getUniqueID()), true) || needsRecalc;
            if(AutoClaim.isAutoClaiming(player.getUniqueID()))
                needsRecalc = ClaimManagement.checkAndAttemptClaim((EntityPlayerMP) player, AutoClaim.getAutoClaimingClan(player.getUniqueID()), false) || needsRecalc;
            if(needsRecalc) {
                data = ClaimData.getChunkPositionData(player.chunkCoordX, player.chunkCoordZ, player.dimension);
                chunkClan = ClaimData.getChunkClan(data);
                isInBorderland = data != null && data.isBorderland();
            }

            if(!Objects.equals(chunkClan, playerStoredClaimId) || (isInBorderland != playerStoredIsInBorderland))
                handleTerritoryChangedMessage(player, chunkClan, playerClans, isInBorderland);
            PlayerCache.setPreviousY(player.getUniqueID(), (int) Math.round(player.posY));
        } else if (chunkClan == null
            && ClansModContainer.getConfig().shouldProtectWilderness()
            && ClansModContainer.getConfig().getMinWildernessY() > 0
            && player.getEntityWorld().getTotalWorldTime() % 20 == 0
            && !PermissionManager.hasPermission(player, PermissionManager.PROTECTION_PREFIX+"break.protected_wilderness", false)
            && !PermissionManager.hasPermission(player, PermissionManager.PROTECTION_PREFIX+"build.protected_wilderness", false)
        )
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

    private static void handleTerritoryChangedMessage(EntityPlayer player, @Nullable UUID chunkClan, Collection<UUID> playerClans, boolean isBorderland) {
        PlayerCache.setPreviousChunkOwner(player.getUniqueID(), chunkClan, isBorderland);
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
            boolean canBuildInWilderness = PermissionManager.hasPermission(player, PermissionManager.PROTECTION_PREFIX+"break.protected_wilderness", false)
                && PermissionManager.hasPermission(player, PermissionManager.PROTECTION_PREFIX+"build.protected_wilderness", false);
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
            territoryName = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.borderland", ClanNames.get(chunkClan).getName());
            territoryDesc = "";
        } else {
            territoryName = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.clanterritory", ClanNames.get(chunkClan).getName());
            territoryDesc = ClanDescriptions.get(chunkClan).getDescription();
        }

        player.sendStatusMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entry", territoryName).setStyle(color), mode.isAction());
        if(!territoryDesc.isEmpty() && mode.showsDescription())
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entrydesc", territoryDesc).setStyle(color));
    }

    private static void checkAndResetClaimWarning(EntityPlayerMP player) {
        if(PlayerCache.getClaimWarning(player.getUniqueID()))
            PlayerCache.setClaimWarning(player.getUniqueID(), false);
    }

    private static void checkRaidAbandonmentTime(@Nullable UUID chunkClan, Collection<UUID> playerClans, EntityPlayer player) {
        for(UUID pc: playerClans)
            if (RaidingParties.hasActiveRaid(pc)) {
                Raid r = RaidingParties.getActiveRaid(pc);
                assert r != null;
                if(r.getDefenders().contains(player.getUniqueID()))
                    if (pc.equals(chunkClan))
                        r.resetDefenderAbandonmentTime(player);
                    else
                        r.incrementDefenderAbandonmentTime(player);
            }
        if (RaidingParties.getRaidingPlayers().contains(player.getUniqueID())) {
            Raid r = RaidingParties.getRaid(player);
            assert r != null;
            if (r.isActive()) {
                if(r.getAttackers().contains(player.getUniqueID()))
                    if (r.getTarget().equals(chunkClan))
                        r.resetAttackerAbandonmentTime(player);
                    else
                        r.incrementAttackerAbandonmentTime(player);
            }
        }
    }
}
