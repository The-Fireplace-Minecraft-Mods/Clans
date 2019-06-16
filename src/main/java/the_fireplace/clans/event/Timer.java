package the_fireplace.clans.event;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.Style;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.*;
import the_fireplace.clans.commands.land.CommandAbandonClaim;
import the_fireplace.clans.commands.land.CommandClaim;
import the_fireplace.clans.commands.members.CommandLeave;
import the_fireplace.clans.commands.teleportation.CommandHome;
import the_fireplace.clans.raid.Raid;
import the_fireplace.clans.raid.RaidBlockPlacementDatabase;
import the_fireplace.clans.raid.RaidRestoreDatabase;
import the_fireplace.clans.raid.RaidingParties;
import the_fireplace.clans.util.*;
import the_fireplace.clans.util.translation.TranslationUtil;

import java.util.*;

@Mod.EventBusSubscriber(modid = Clans.MODID)
public class Timer {
	private static byte ticks = 0;
	private static int minuteCounter = 0;
	private static int fiveMinuteCounter = 0;
	private static boolean executing = false;
	public static HashMap<EntityPlayerMP, Pair<Integer, Integer>> clanHomeWarmups = Maps.newHashMap();
	@SuppressWarnings("Duplicates")
	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if(!executing) {
			if(++fiveMinuteCounter >= 20*60*5) {
				executing = true;
				fiveMinuteCounter -= 20*60*5;
				ClanChunkCache.save();
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
				for(Map.Entry<EntityPlayerMP, Pair<Integer, Integer>> entry : Sets.newHashSet(clanHomeWarmups.entrySet()))
					if (entry.getValue().getValue1() == 1 && entry.getKey() != null && entry.getKey().isEntityAlive()) {
						Clan c = ClanCache.getPlayerClans(entry.getKey().getUniqueID()).get(entry.getValue().getValue2());
						if(c != null && c.getHome() != null)
							CommandHome.teleportHome(entry.getKey(), c, c.getHome(), entry.getKey().dimension);
					}
				for(EntityPlayerMP player: Sets.newHashSet(clanHomeWarmups.keySet()))
					if(clanHomeWarmups.get(player).getValue1() > 0)
						clanHomeWarmups.put(player, new Pair<>(clanHomeWarmups.get(player).getValue1() - 1, clanHomeWarmups.get(player).getValue2()));
					else
						clanHomeWarmups.remove(player);

				for (Raid raid : RaidingParties.getActiveRaids())
					if (raid.checkRaidEndTimer())
						raid.defenderVictory();

				if (Clans.cfg.clanUpkeepDays > 0 || Clans.cfg.chargeRentDays > 0)
					for (Clan clan : ClanDatabase.getClans()) {
						if (Clans.cfg.chargeRentDays > 0 && System.currentTimeMillis() >= clan.getNextRentTimestamp()) {
							Clans.LOGGER.debug("Charging rent for {}.", clan.getClanName());
							for (Map.Entry<UUID, EnumRank> member : clan.getMembers().entrySet()) {
								if (Clans.getPaymentHandler().deductAmount(clan.getRent(), member.getKey()))
									Clans.getPaymentHandler().addAmount(clan.getRent(), clan.getClanId());
								else if (Clans.cfg.evictNonpayers)
									if (member.getValue() != EnumRank.LEADER && (Clans.cfg.evictNonpayerAdmins || member.getValue() == EnumRank.MEMBER)) {
										clan.removeMember(member.getKey());
										EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(member.getKey());
										//noinspection ConstantConditions
										if (player != null) {
											CommandLeave.updateDefaultClan(player, clan);
											player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.rent.kicked", clan.getClanName()).setStyle(TextStyles.YELLOW));
										}
									}
							}
							clan.updateNextRentTimeStamp();
						}
						if (Clans.cfg.clanUpkeepDays > 0 && System.currentTimeMillis() >= clan.getNextUpkeepTimestamp()) {
							Clans.LOGGER.debug("Charging upkeep for {}.", clan.getClanName());
							int upkeep = Clans.cfg.clanUpkeepCost;
							if (Clans.cfg.multiplyUpkeepMembers)
								upkeep *= clan.getMemberCount();
							if (Clans.cfg.multiplyUpkeepClaims)
								upkeep *= clan.getClaimCount();
							if (Clans.getPaymentHandler().deductPartialAmount(upkeep, clan.getClanId()) > 0 && Clans.cfg.disbandNoUpkeep) {
								long distFunds = Clans.getPaymentHandler().getBalance(clan.getClanId());
								long rem;
								distFunds += Clans.cfg.claimChunkCost * clan.getClaimCount();
								if (Clans.cfg.leaderRecieveDisbandFunds) {
									distFunds = clan.payLeaders(distFunds);
									rem = distFunds % clan.getMemberCount();
									distFunds /= clan.getMemberCount();
								} else {
									rem = clan.payLeaders(distFunds % clan.getMemberCount());
									distFunds /= clan.getMemberCount();
								}
								for (UUID member : clan.getMembers().keySet()) {
									Clans.getPaymentHandler().ensureAccountExists(member);
									if (!Clans.getPaymentHandler().addAmount(distFunds + (rem-- > 0 ? 1 : 0), member))
										rem += clan.payLeaders(distFunds);
									EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(member);
									//noinspection ConstantConditions
									if (player != null) {
										CommandLeave.updateDefaultClan(player, clan);
										player.sendMessage(TranslationUtil.getTranslation(player.getUniqueID(), "clans.upkeep.disbanded", clan.getClanName()).setStyle(TextStyles.YELLOW));
									}
								}
							}
							clan.updateNextUpkeepTimeStamp();
						}
					}

				executing = false;
			}
		}
	}

	//These three are used for the chunk claim warning
	private static HashMap<EntityPlayer, Integer> prevYs = Maps.newHashMap();
	static HashMap<EntityPlayer, Integer> prevChunkXs = Maps.newHashMap();
	static HashMap<EntityPlayer, Integer> prevChunkZs = Maps.newHashMap();
	//Maps of (Player Unique ID) -> (Clan)
	public static HashMap<UUID, Clan> autoAbandonClaims = Maps.newHashMap();
	public static HashMap<UUID, Clan> autoClaimLands = Maps.newHashMap();

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if(!event.player.getEntityWorld().isRemote) {
			if (event.player.getEntityWorld().getTotalWorldTime() % 20 == 0) {
				//noinspection ConstantConditions
				PlayerClanCapability c = event.player.getCapability(Clans.CLAN_DATA_CAP, null);
				if(c != null && c.getCooldown() > 0)
					c.setCooldown(c.getCooldown() - 1);
			}
			if (event.player.getEntityWorld().getTotalWorldTime() % 10 == 0) {
				//noinspection ConstantConditions
				assert Clans.CLAIMED_LAND != null;
				Chunk c = event.player.getEntityWorld().getChunk(event.player.getPosition());
				UUID chunkClan = ChunkUtils.getChunkOwner(c);
				ArrayList<Clan> playerClans = ClanCache.getPlayerClans(event.player.getUniqueID());
				if (event.player.hasCapability(Clans.CLAIMED_LAND, null)) {
					UUID playerStoredClaimId = event.player.getCapability(Clans.CLAIMED_LAND, null).getClan();
					if (chunkClan != null && ClanCache.getClanById(chunkClan) == null) {
						ChunkUtils.clearChunkOwner(c);
						chunkClan = null;
					}

					LegacyCompatEvents.checkPre120Compat(c);

					if ((chunkClan != null && !chunkClan.equals(playerStoredClaimId)) || (chunkClan == null && playerStoredClaimId != null)) {

						if(autoAbandonClaims.containsKey(event.player.getUniqueID()))
							CommandAbandonClaim.checkAndAttemptAbandon((EntityPlayerMP)event.player, autoAbandonClaims.get(event.player.getUniqueID()));
						if(autoClaimLands.containsKey(event.player.getUniqueID()))
							CommandClaim.checkAndAttemptClaim((EntityPlayerMP) event.player, autoClaimLands.get(event.player.getUniqueID()));

						handleTerritoryChangedMessage(event, chunkClan, playerClans);
					} else if (Clans.cfg.protectWilderness && Clans.cfg.minWildernessY != 0 && event.player.getEntityWorld().getTotalWorldTime() % 20 == 0) {
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
		int prevY = prevYs.get(event.player) != null ? prevYs.get(event.player) : curY;
		int yBound = (Clans.cfg.minWildernessY < 0 ? event.player.world.getSeaLevel() : Clans.cfg.minWildernessY);
		if (curY >= yBound && prevY < yBound) {
			event.player.sendMessage(TranslationUtil.getTranslation(event.player.getUniqueID(), "clans.territory.entry", TranslationUtil.getStringTranslation(event.player.getUniqueID(), "clans.wilderness")).setStyle(TextStyles.YELLOW));
			event.player.sendMessage(TranslationUtil.getTranslation(event.player.getUniqueID(), "clans.territory.entrydesc", TranslationUtil.getStringTranslation(event.player.getUniqueID(), "clans.territory.protected")).setStyle(TextStyles.YELLOW));
		} else if (prevY >= yBound && curY < yBound) {
			event.player.sendMessage(TranslationUtil.getTranslation(event.player.getUniqueID(), "clans.territory.entry", TranslationUtil.getStringTranslation(event.player.getUniqueID(), "clans.underground")).setStyle(TextStyles.DARK_GREEN));
			event.player.sendMessage(TranslationUtil.getTranslation(event.player.getUniqueID(), "clans.territory.entrydesc", TranslationUtil.getStringTranslation(event.player.getUniqueID(), "clans.territory.unclaimed")).setStyle(TextStyles.DARK_GREEN));
		}
		prevYs.put(event.player, curY);
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
			if (Clans.cfg.protectWilderness && (Clans.cfg.minWildernessY < 0 ? event.player.posY < event.player.world.getSeaLevel() : event.player.posY < Clans.cfg.minWildernessY)) {
				territoryName = TranslationUtil.getStringTranslation(event.player.getUniqueID(), "clans.underground");
				territoryDesc = TranslationUtil.getStringTranslation(event.player.getUniqueID(), "clans.territory.unclaimed");
			} else {
				territoryName = TranslationUtil.getStringTranslation(event.player.getUniqueID(), "clans.wilderness");
				if(Clans.cfg.protectWilderness) {
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
			if(!prevChunkXs.containsKey(player))
				prevChunkXs.put(player, player.getServerWorld().getChunk(player.getPosition()).x);
			if(!prevChunkZs.containsKey(player))
				prevChunkZs.put(player, player.getServerWorld().getChunk(player.getPosition()).z);
			if(prevChunkXs.get(player) != player.getServerWorld().getChunk(player.getPosition()).x || prevChunkZs.get(player) != player.getServerWorld().getChunk(player.getPosition()).z) {
				CapHelper.getPlayerClanCapability(player).setClaimWarning(false);
				prevChunkXs.remove(player);
				prevChunkZs.remove(player);
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
