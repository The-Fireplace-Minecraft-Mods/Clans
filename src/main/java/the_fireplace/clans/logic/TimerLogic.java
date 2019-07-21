package the_fireplace.clans.logic;

import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.Style;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.cache.PlayerDataCache;
import the_fireplace.clans.cache.RaidingParties;
import the_fireplace.clans.commands.teleportation.CommandHome;
import the_fireplace.clans.data.*;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.model.OrderedPair;
import the_fireplace.clans.model.Raid;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.ClanManagementUtil;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class TimerLogic {
    public static void runFiveMinuteLogic() {
        ClaimDataManager.save();
        ClanDatabase.save();
        RaidBlockPlacementDatabase.save();
        RaidRestoreDatabase.save();
        PlayerDataManager.save();
    }

    public static void runOneMinuteLogic() {
        for (Clan clan : ClanDatabase.getClans())
            clan.decrementShield();
    }

    public static void runOneSecondLogic() {
        RaidingParties.decrementBuffers();
        for(Map.Entry<EntityPlayerMP, OrderedPair<Integer, Integer>> entry : Sets.newHashSet(PlayerDataCache.clanHomeWarmups.entrySet()))
            if (entry.getValue().getValue1() == 1 && entry.getKey() != null && entry.getKey().isEntityAlive()) {
                Clan c = ClanCache.getPlayerClans(entry.getKey().getUniqueID()).get(entry.getValue().getValue2());
                if(c != null && c.getHome() != null)
                    CommandHome.teleportHome(entry.getKey(), c, c.getHome(), entry.getKey().dimension, false);
            }
        for(EntityPlayerMP player: Sets.newHashSet(PlayerDataCache.clanHomeWarmups.keySet()))
            if(PlayerDataCache.clanHomeWarmups.get(player).getValue1() > 0)
                PlayerDataCache.clanHomeWarmups.put(player, new OrderedPair<>(PlayerDataCache.clanHomeWarmups.get(player).getValue1() - 1, PlayerDataCache.clanHomeWarmups.get(player).getValue2()));
            else
                PlayerDataCache.clanHomeWarmups.remove(player);

        for (Raid raid : RaidingParties.getActiveRaids())
            if (raid.checkRaidEndTimer())
                raid.defenderVictory();

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
                                    PlayerDataManager.updateDefaultClan(player.getUniqueID(), clan.getClanId());
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

    public static void runPlayerSecondLogic(EntityPlayer player) {
        int cooldown = PlayerDataManager.getCooldown(player.getUniqueID());
        if(cooldown > 0)
            PlayerDataManager.setCooldown(player.getUniqueID(), cooldown - 1);
        checkRaidAbandonmentTime(ClaimDataManager.getChunkClanId(player.chunkCoordX, player.chunkCoordZ, player.dimension), ClanCache.getPlayerClans(player.getUniqueID()), player);
    }

    public static void runPlayerHalfSecondLogic(EntityPlayer player) {
        Chunk c = player.getEntityWorld().getChunk(player.getPosition());
        UUID chunkClanId = ChunkUtils.getChunkOwner(c);
        ArrayList<Clan> playerClans = ClanCache.getPlayerClans(player.getUniqueID());
        UUID playerStoredClaimId = PlayerDataManager.getPreviousChunkOwner(player.getUniqueID());
        Clan chunkClan = ClanCache.getClanById(chunkClanId);
        if (chunkClanId != null && chunkClan == null) {
            ChunkUtils.clearChunkOwner(c);
            chunkClanId = null;
        }

        if ((chunkClanId != null && !chunkClanId.equals(playerStoredClaimId)) || (chunkClanId == null && playerStoredClaimId != null)) {
            if(ClanCache.getOpAutoAbandonClaims().containsKey(player.getUniqueID()))
                ClanManagementUtil.checkAndAttemptAbandon((EntityPlayerMP)player, ClanDatabase.getOpClan(), true, ClanCache.getOpAutoAbandonClaims().get(player.getUniqueID()));
            if(ClanCache.getAutoAbandonClaims().containsKey(player.getUniqueID()))
                ClanManagementUtil.checkAndAttemptAbandon((EntityPlayerMP)player, ClanCache.getAutoAbandonClaims().get(player.getUniqueID()), false, false);
            if(ClanCache.getOpAutoClaimLands().containsKey(player.getUniqueID()))
                ClanManagementUtil.checkAndAttemptClaim((EntityPlayerMP)player, ClanCache.getOpAutoClaimLands().get(player.getUniqueID()).getValue1(), true, ClanCache.getOpAutoClaimLands().get(player.getUniqueID()).getValue2());
            if(ClanCache.getAutoClaimLands().containsKey(player.getUniqueID()))
                ClanManagementUtil.checkAndAttemptClaim((EntityPlayerMP) player, ClanCache.getAutoClaimLands().get(player.getUniqueID()), false, false);

            handleTerritoryChangedMessage(player, chunkClanId, playerClans);
        } else if (Clans.getConfig().isProtectWilderness() && Clans.getConfig().getMinWildernessY() != 0 && player.getEntityWorld().getTotalWorldTime() % 20 == 0) {
            handleDepthChangedMessage(player);
        }
        EntityPlayerMP playerMP = player instanceof EntityPlayerMP ? (EntityPlayerMP) player : null;
        if (playerMP != null) {
            handleClaimWarning(playerMP);

            PlayerDataManager.setPreviousChunkX(player.getUniqueID(), playerMP.getServerWorld().getChunk(player.getPosition()).x);
            PlayerDataManager.setPreviousChunkZ(player.getUniqueID(), playerMP.getServerWorld().getChunk(player.getPosition()).z);
        }
    }

    private static void handleDepthChangedMessage(EntityPlayer player) {
        int curY = (int) Math.round(player.posY);
        int prevY = PlayerDataManager.getPreviousY(player.getUniqueID());
        int yBound = (Clans.getConfig().getMinWildernessY() < 0 ? player.world.getSeaLevel() : Clans.getConfig().getMinWildernessY());
        if (curY >= yBound && prevY < yBound) {
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entry", TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.wilderness")).setStyle(TextStyles.YELLOW));
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entrydesc", TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.protected")).setStyle(TextStyles.YELLOW));
        } else if (prevY >= yBound && curY < yBound) {
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entry", TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.underground")).setStyle(TextStyles.DARK_GREEN));
            player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entrydesc", TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.unclaimed")).setStyle(TextStyles.DARK_GREEN));
        }
        PlayerDataManager.setPreviousY(player.getUniqueID(), curY);
    }

    private static void handleTerritoryChangedMessage(EntityPlayer player, @Nullable UUID chunkClanId, ArrayList<Clan> playerClans) {
        PlayerDataManager.setPreviousChunkOwner(player.getUniqueID(), chunkClanId);
        Style color = TextStyles.GREEN;
        Clan chunkClan = ClanCache.getClanById(chunkClanId);
        if ((!playerClans.isEmpty() && !playerClans.contains(chunkClan)) || (playerClans.isEmpty() && chunkClanId != null))
            color = TextStyles.YELLOW;
        if (chunkClanId == null)
            color = TextStyles.DARK_GREEN;
        String territoryName;
        String territoryDesc;
        if (chunkClanId == null) {
            if (Clans.getConfig().isProtectWilderness() && (Clans.getConfig().getMinWildernessY() < 0 ? player.posY < player.world.getSeaLevel() : player.posY < Clans.getConfig().getMinWildernessY())) {
                territoryName = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.underground");
                territoryDesc = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.unclaimed");
            } else {
                territoryName = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.wilderness");
                if(Clans.getConfig().isProtectWilderness()) {
                    color = TextStyles.YELLOW;
                    territoryDesc = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.protected");
                } else
                    territoryDesc = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.unclaimed");
            }
        } else {
            assert chunkClan != null;
            territoryName = TranslationUtil.getStringTranslation(player.getUniqueID(), "clans.territory.clanterritory", chunkClan.getClanName());
            territoryDesc = chunkClan.getDescription();
        }

        player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entry", territoryName).setStyle(color));
        player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.territory.entrydesc", territoryDesc).setStyle(color));
    }

    private static void handleClaimWarning(EntityPlayerMP player) {
        if(PlayerDataManager.getClaimWarning(player.getUniqueID())) {
            if(PlayerDataManager.getPreviousChunkX(player.getUniqueID()) != player.getServerWorld().getChunk(player.getPosition()).x || PlayerDataManager.getPreviousChunkZ(player.getUniqueID()) != player.getServerWorld().getChunk(player.getPosition()).z) {
                PlayerDataManager.setClaimWarning(player.getUniqueID(), false);
            }
        }
    }

    private static void checkRaidAbandonmentTime(@Nullable UUID chunkClan, ArrayList<Clan> playerClans, EntityPlayer player) {
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
