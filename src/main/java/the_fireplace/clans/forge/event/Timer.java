package the_fireplace.clans.forge.event;

import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.Style;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.cache.PlayerDataCache;
import the_fireplace.clans.cache.RaidingParties;
import the_fireplace.clans.commands.land.CommandAbandonClaim;
import the_fireplace.clans.commands.land.CommandClaim;
import the_fireplace.clans.commands.op.land.OpCommandAbandonClaim;
import the_fireplace.clans.commands.op.land.OpCommandClaim;
import the_fireplace.clans.commands.teleportation.CommandHome;
import the_fireplace.clans.data.ClanChunkData;
import the_fireplace.clans.data.ClanDatabase;
import the_fireplace.clans.data.RaidBlockPlacementDatabase;
import the_fireplace.clans.data.RaidRestoreDatabase;
import the_fireplace.clans.forge.ClansForge;
import the_fireplace.clans.forge.legacy.CapHelper;
import the_fireplace.clans.forge.legacy.PlayerClanCapability;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.model.OrderedPair;
import the_fireplace.clans.model.Raid;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Clans.MODID)
public class Timer {
	private static byte ticks = 0;
	private static int minuteCounter = 0;
	private static int fiveMinuteCounter = 0;
	private static boolean executing = false;

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if(!executing) {
			if(++fiveMinuteCounter >= 20*60*5) {
				executing = true;
				fiveMinuteCounter -= 20*60*5;
				ClanChunkData.save();
				ClanDatabase.save();
				RaidBlockPlacementDatabase.save();
				RaidRestoreDatabase.save();
				executing = false;
			}
			if(++minuteCounter >= 20*60) {
				executing = true;
				minuteCounter -= 20*60;
				for (Clan clan : ClanDatabase.getClans())
					clan.decrementShield();
				executing = false;
			}
			if(++ticks >= 20) {
				executing = true;
				ticks -= 20;

				RaidingParties.decrementBuffers();
				for(Map.Entry<EntityPlayerMP, OrderedPair<Integer, Integer>> entry : Sets.newHashSet(PlayerDataCache.clanHomeWarmups.entrySet()))
					if (entry.getValue().getValue1() == 1 && entry.getKey() != null && entry.getKey().isEntityAlive()) {
						Clan c = ClanCache.getPlayerClans(entry.getKey().getUniqueID()).get(entry.getValue().getValue2());
						if(c != null && c.getHome() != null)
							CommandHome.teleportHome(entry.getKey(), c, c.getHome(), entry.getKey().dimension);
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
											PlayerClanCapability.updateDefaultClan(player, clan);
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

				executing = false;
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if(!event.player.getEntityWorld().isRemote) {
			if (event.player.getEntityWorld().getTotalWorldTime() % 20 == 0) {
				//noinspection ConstantConditions
				PlayerClanCapability c = event.player.getCapability(ClansForge.CLAN_DATA_CAP, null);
				if(c != null && c.getCooldown() > 0)
					c.setCooldown(c.getCooldown() - 1);
			}
			if (event.player.getEntityWorld().getTotalWorldTime() % 10 == 0) {
				//noinspection ConstantConditions
				assert ClansForge.CLAIMED_LAND != null;
				Chunk c = event.player.getEntityWorld().getChunk(event.player.getPosition());
				UUID chunkClan = ChunkUtils.getChunkOwner(c);
				ArrayList<Clan> playerClans = ClanCache.getPlayerClans(event.player.getUniqueID());
				if (event.player.hasCapability(ClansForge.CLAIMED_LAND, null)) {
					UUID playerStoredClaimId = event.player.getCapability(ClansForge.CLAIMED_LAND, null).getClan();
					if (chunkClan != null && ClanCache.getClanById(chunkClan) == null) {
						ChunkUtils.clearChunkOwner(c);
						chunkClan = null;
					}

					if ((chunkClan != null && !chunkClan.equals(playerStoredClaimId)) || (chunkClan == null && playerStoredClaimId != null)) {

						if(ClanCache.getOpAutoAbandonClaims().containsKey(event.player.getUniqueID()))
							OpCommandAbandonClaim.checkAndAttemptOpAbandon((EntityPlayerMP)event.player, ClanCache.getOpAutoAbandonClaims().get(event.player.getUniqueID()) ? new String[]{"force"} : new String[]{});
						if(ClanCache.getAutoAbandonClaims().containsKey(event.player.getUniqueID()))
							CommandAbandonClaim.checkAndAttemptAbandon((EntityPlayerMP)event.player, ClanCache.getAutoAbandonClaims().get(event.player.getUniqueID()));
						if(ClanCache.getOpAutoClaimLands().containsKey(event.player.getUniqueID()))
							OpCommandClaim.checkAndAttemptOpClaim((EntityPlayerMP)event.player, ClanCache.getOpAutoClaimLands().get(event.player.getUniqueID()).getValue2() ? new String[]{"force"} : new String[]{}, ClanCache.getOpAutoClaimLands().get(event.player.getUniqueID()).getValue1());
						if(ClanCache.getAutoClaimLands().containsKey(event.player.getUniqueID()))
							CommandClaim.checkAndAttemptClaim((EntityPlayerMP) event.player, ClanCache.getAutoClaimLands().get(event.player.getUniqueID()));

						handleTerritoryChangedMessage(event, chunkClan, playerClans);
					} else if (Clans.getConfig().isProtectWilderness() && Clans.getConfig().getMinWildernessY() != 0 && event.player.getEntityWorld().getTotalWorldTime() % 20 == 0) {
						handleDepthChangedMessage(event);
					}
				}
				EntityPlayerMP player = event.player instanceof EntityPlayerMP ? (EntityPlayerMP) event.player : null;
				if (player != null) {
					checkRaidAbandonmentTime(chunkClan, playerClans, player);

					handleClaimWarning(player);
				}
			}
		}
	}

	private static void handleDepthChangedMessage(TickEvent.PlayerTickEvent event) {
		int curY = (int) Math.round(event.player.posY);
		int prevY = PlayerDataCache.prevYs.get(event.player) != null ? PlayerDataCache.prevYs.get(event.player) : curY;
		int yBound = (Clans.getConfig().getMinWildernessY() < 0 ? event.player.world.getSeaLevel() : Clans.getConfig().getMinWildernessY());
		if (curY >= yBound && prevY < yBound) {
			event.player.sendMessage(TranslationUtil.getTranslation(event.player.getUniqueID(), "clans.territory.entry", TranslationUtil.getStringTranslation(event.player.getUniqueID(), "clans.wilderness")).setStyle(TextStyles.YELLOW));
			event.player.sendMessage(TranslationUtil.getTranslation(event.player.getUniqueID(), "clans.territory.entrydesc", TranslationUtil.getStringTranslation(event.player.getUniqueID(), "clans.territory.protected")).setStyle(TextStyles.YELLOW));
		} else if (prevY >= yBound && curY < yBound) {
			event.player.sendMessage(TranslationUtil.getTranslation(event.player.getUniqueID(), "clans.territory.entry", TranslationUtil.getStringTranslation(event.player.getUniqueID(), "clans.underground")).setStyle(TextStyles.DARK_GREEN));
			event.player.sendMessage(TranslationUtil.getTranslation(event.player.getUniqueID(), "clans.territory.entrydesc", TranslationUtil.getStringTranslation(event.player.getUniqueID(), "clans.territory.unclaimed")).setStyle(TextStyles.DARK_GREEN));
		}
		PlayerDataCache.prevYs.put(event.player, curY);
	}

	private static void handleTerritoryChangedMessage(TickEvent.PlayerTickEvent event, UUID chunkClanId, ArrayList<Clan> playerClans) {
		CapHelper.getClaimedLandCapability(event.player).setClan(chunkClanId);
		Style color = TextStyles.GREEN;
		Clan chunkClan = ClanCache.getClanById(chunkClanId);
		if ((!playerClans.isEmpty() && !playerClans.contains(chunkClan)) || (playerClans.isEmpty() && chunkClanId != null))
			color = TextStyles.YELLOW;
		if (chunkClanId == null)
			color = TextStyles.DARK_GREEN;
		String territoryName;
		String territoryDesc;
		if (chunkClanId == null) {
			if (Clans.getConfig().isProtectWilderness() && (Clans.getConfig().getMinWildernessY() < 0 ? event.player.posY < event.player.world.getSeaLevel() : event.player.posY < Clans.getConfig().getMinWildernessY())) {
				territoryName = TranslationUtil.getStringTranslation(event.player.getUniqueID(), "clans.underground");
				territoryDesc = TranslationUtil.getStringTranslation(event.player.getUniqueID(), "clans.territory.unclaimed");
			} else {
				territoryName = TranslationUtil.getStringTranslation(event.player.getUniqueID(), "clans.wilderness");
				if(Clans.getConfig().isProtectWilderness()) {
					color = TextStyles.YELLOW;
					territoryDesc = TranslationUtil.getStringTranslation(event.player.getUniqueID(), "clans.territory.protected");
				} else
					territoryDesc = TranslationUtil.getStringTranslation(event.player.getUniqueID(), "clans.territory.unclaimed");
			}
		} else {
			assert chunkClan != null;
			territoryName = TranslationUtil.getStringTranslation(event.player.getUniqueID(), "clans.territory.clanterritory", chunkClan.getClanName());
			territoryDesc = chunkClan.getDescription();
		}

		event.player.sendMessage(TranslationUtil.getTranslation(event.player.getUniqueID(), "clans.territory.entry", territoryName).setStyle(color));
		event.player.sendMessage(TranslationUtil.getTranslation(event.player.getUniqueID(), "clans.territory.entrydesc", territoryDesc).setStyle(color));
	}

	private static void handleClaimWarning(EntityPlayerMP player) {
		if(CapHelper.getPlayerClanCapability(player).getClaimWarning()) {
			if(!PlayerDataCache.prevChunkXs.containsKey(player))
				PlayerDataCache.prevChunkXs.put(player, player.getServerWorld().getChunk(player.getPosition()).x);
			if(!PlayerDataCache.prevChunkZs.containsKey(player))
				PlayerDataCache.prevChunkZs.put(player, player.getServerWorld().getChunk(player.getPosition()).z);
			if(PlayerDataCache.prevChunkXs.get(player) != player.getServerWorld().getChunk(player.getPosition()).x || PlayerDataCache.prevChunkZs.get(player) != player.getServerWorld().getChunk(player.getPosition()).z) {
				CapHelper.getPlayerClanCapability(player).setClaimWarning(false);
				PlayerDataCache.prevChunkXs.remove(player);
				PlayerDataCache.prevChunkZs.remove(player);
			}
		}
	}

	private static void checkRaidAbandonmentTime(UUID chunkClan, ArrayList<Clan> playerClans, EntityPlayerMP player) {
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
