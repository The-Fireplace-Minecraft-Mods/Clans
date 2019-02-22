package the_fireplace.clans.event;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.details.CommandHome;
import the_fireplace.clans.raid.Raid;
import the_fireplace.clans.raid.RaidingParties;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.Pair;
import the_fireplace.clans.util.PlayerClanCapability;
import the_fireplace.clans.util.MinecraftColors;

import java.util.*;

@Mod.EventBusSubscriber(modid = Clans.MODID)
public class Timer {
	private static byte ticks = 0;
	private static int minuteCounter = 0;
	private static boolean executing = false;
	public static HashMap<EntityPlayerMP, Pair<Integer, Integer>> clanHomeWarmups = Maps.newHashMap();
	@SuppressWarnings("Duplicates")
	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if(!executing) {
			if(++minuteCounter >= 20*60)
				for(Clan clan: ClanDatabase.getClans())
					clan.decrementShield();
			if (++ticks >= 20) {
				executing = true;
				ticks -= 20;

				for(Map.Entry<EntityPlayerMP, Pair<Integer, Integer>> entry : clanHomeWarmups.entrySet())
					if (entry.getValue().getValue1() == 1) {
						Clan c = ClanCache.getPlayerClans(entry.getKey().getUniqueID()).get(entry.getValue().getValue2());
						if(c != null)
							CommandHome.teleportHome(entry.getKey(), c, c.getHome(), entry.getKey().dimension);
					}
				Set<EntityPlayerMP> players = clanHomeWarmups.keySet();
				for(EntityPlayerMP player: players)
					if(clanHomeWarmups.get(player).getValue1() > 0)
						clanHomeWarmups.put(player, new Pair<>(clanHomeWarmups.get(player).getValue1() - 1, clanHomeWarmups.get(player).getValue2()));

				for (Raid raid : RaidingParties.getActiveRaids())
					if (raid.checkRaidEndTimer())
						raid.defenderVictory();

				if (Clans.cfg.clanUpkeepDays > 0 || Clans.cfg.chargeRentDays > 0)
					for (Clan clan : ClanDatabase.getClans()) {
						if (Clans.cfg.chargeRentDays > 0 && Clans.cfg.chargeRentDays * 86400000L < clan.getRentTimeStamp()) {
							for (Map.Entry<UUID, EnumRank> member : clan.getMembers().entrySet()) {
								if (Clans.getPaymentHandler().deductAmount(clan.getRent(), member.getKey()))
									Clans.getPaymentHandler().addAmount(clan.getRent(), clan.getClanId());
								else if (Clans.cfg.evictNonpayers)
									if (member.getValue() != EnumRank.LEADER && (Clans.cfg.evictNonpayerAdmins || member.getValue() == EnumRank.MEMBER))
										clan.removeMember(member.getKey());//TODO: If member is online, send the member a message saying they were evicted due to not being able to pay rent.
							}
							clan.updateRentTimeStamp();
						}
						if (Clans.cfg.clanUpkeepDays > 0 && Clans.cfg.clanUpkeepDays * 86400000L < clan.getUpkeepTimeStamp()) {
							int upkeep = Clans.cfg.clanUpkeepCost;
							if (Clans.cfg.multiplyUpkeepMembers)
								upkeep *= clan.getMemberCount();
							if (Clans.cfg.multiplyUpkeepClaims)
								upkeep *= clan.getClaimCount();
							if (Clans.getPaymentHandler().deductPartialAmount(upkeep, clan.getClanId()) > 0 && Clans.cfg.disbandNoUpkeep) {
								long distFunds = Clans.getPaymentHandler().getBalance(clan.getClanId());
								distFunds += Clans.cfg.claimChunkCost * clan.getClaimCount();
								if (Clans.cfg.leaderRecieveDisbandFunds) {
									clan.payLeaders(distFunds);
									distFunds = 0;
								} else {
									clan.payLeaders(distFunds % clan.getMemberCount());
									distFunds /= clan.getMemberCount();
								}
								for (UUID member : clan.getMembers().keySet()) {
									Clans.getPaymentHandler().ensureAccountExists(member);
									if (!Clans.getPaymentHandler().addAmount(distFunds, member))
										clan.payLeaders(distFunds);
									//TODO send message to member saying it was disbanded.
								}
							}
							clan.updateUpkeepTimeStamp();
						}
					}

				executing = false;
			}
		}
	}

	private static HashMap<EntityPlayer, Integer> prevYs = Maps.newHashMap();

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
					if (chunkClan != null && ClanCache.getClan(chunkClan) == null) {
						ChunkUtils.clearChunkOwner(c);
						chunkClan = null;
					}
					if ((chunkClan != null && !chunkClan.equals(playerStoredClaimId)) || (chunkClan == null && playerStoredClaimId != null)) {
						event.player.getCapability(Clans.CLAIMED_LAND, null).setClan(chunkClan);
						String color = MinecraftColors.GREEN;
						if ((!playerClans.isEmpty() && !playerClans.contains(ClanCache.getClan(chunkClan))) || (playerClans.isEmpty() && chunkClan != null))
							color = MinecraftColors.YELLOW;
						if (chunkClan == null)
							color = MinecraftColors.DARK_GREEN;
						String endMsg;
						if (chunkClan == null) {
							if (Clans.cfg.protectWilderness && (Clans.cfg.minWildernessY < 0 ? event.player.posY < event.player.world.getSeaLevel() : event.player.posY < Clans.cfg.minWildernessY))
								endMsg = "Underground.";
							else
								endMsg = "Wilderness.";
						} else
							endMsg = ClanCache.getClan(chunkClan).getClanName() + "'s territory.";

						event.player.sendMessage(new TextComponentString(color + "You are now entering " + endMsg));
					} else if (Clans.cfg.protectWilderness && Clans.cfg.minWildernessY != 0 && event.player.getEntityWorld().getTotalWorldTime() % 15 == 0) {
						int curY = (int) Math.round(event.player.posY);
						int prevY = prevYs.get(event.player) != null ? prevYs.get(event.player) : curY;
						int yBound = (Clans.cfg.minWildernessY < 0 ? event.player.world.getSeaLevel() : Clans.cfg.minWildernessY);
						if (curY >= yBound && prevY < yBound)
							event.player.sendMessage(new TextComponentString(MinecraftColors.DARK_GREEN + "You are now entering Wilderness."));
						else if (prevY >= yBound && curY < yBound)
							event.player.sendMessage(new TextComponentString(MinecraftColors.DARK_GREEN + "You are now entering Underground."));
						prevYs.put(event.player, curY);
					}
				}
				EntityPlayerMP player = event.player instanceof EntityPlayerMP ? (EntityPlayerMP) event.player : null;
				if (player != null) {
					for(Clan pc: playerClans)
						if (RaidingParties.hasActiveRaid(pc)) {
							Raid r = RaidingParties.getActiveRaid(pc);
							if (pc.getClanId().equals(chunkClan))
								r.resetDefenderAbandonmentTime(player);
							else
								r.incrementDefenderAbandonmentTime(player);
						}
					if (RaidingParties.getRaidingPlayers().contains(player)) {
						Raid r = RaidingParties.getRaid(player);
						if (r.isActive()) {
							if (r.getTarget().getClanId().equals(chunkClan))
								r.resetAttackerAbandonmentTime(player);
							else
								r.incrementAttackerAbandonmentTime(player);
						}
					}
				}
			}
		}
	}
}
