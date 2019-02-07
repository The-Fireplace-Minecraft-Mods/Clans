package the_fireplace.clans.event;

import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.raid.Raid;
import the_fireplace.clans.raid.RaidingParties;
import the_fireplace.clans.util.MinecraftColors;

import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Clans.MODID)
public class Timer {
	private static byte ticks = 0;
	private static boolean executing = false;
	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if(!executing && ++ticks >= 20) {
			executing = true;
			ticks -= 20;

			for (Raid raid: RaidingParties.getActiveRaids()) {
				if(raid.checkRaidEndTimer()) {
					RaidingParties.endRaid(raid.getTarget());
					//Reward the defenders the cost of the raid
					Clans.getPaymentHandler().addAmount(raid.getCost(), raid.getTarget().getClanId());
					//TODO give the defenders their shield
					//TODO record this as a successful defense
				}
			}

			if(Clans.cfg.clanUpkeepDays > 0 || Clans.cfg.chargeRentDays > 0)
				for(Clan clan: ClanDatabase.getClans()) {
					if(Clans.cfg.chargeRentDays > 0 && Clans.cfg.chargeRentDays * 86400000L < clan.getRentTimeStamp()) {
						for(Map.Entry<UUID, EnumRank> member: clan.getMembers().entrySet()) {
							if(Clans.getPaymentHandler().deductAmount(clan.getRent(), member.getKey()))
								Clans.getPaymentHandler().addAmount(clan.getRent(), clan.getClanId());
							else if(Clans.cfg.evictNonpayers)
								if(member.getValue() != EnumRank.LEADER && (Clans.cfg.evictNonpayerAdmins || member.getValue() == EnumRank.MEMBER))
									clan.removeMember(member.getKey());//TODO: If member is online, send the member a message saying they were evicted due to not being able to pay rent.
						}
						clan.updateRentTimeStamp();
					}
					if(Clans.cfg.clanUpkeepDays > 0 && Clans.cfg.clanUpkeepDays * 86400000L < clan.getUpkeepTimeStamp()) {
						int upkeep = Clans.cfg.clanUpkeepCost;
						if(Clans.cfg.multiplyUpkeepMembers)
							upkeep *= clan.getMemberCount();
						if(Clans.cfg.multiplyUpkeepClaims)
							upkeep *= clan.getClaimCount();
						if(Clans.getPaymentHandler().deductPartialAmount(upkeep, clan.getClanId()) > 0 && Clans.cfg.disbandNoUpkeep) {
							long distFunds = Clans.getPaymentHandler().getBalance(clan.getClanId());
							distFunds += Clans.cfg.claimChunkCost * clan.getClaimCount();
							if(Clans.cfg.leaderRecieveDisbandFunds) {
								//TODO pay leaders
								//Clans.getPaymentHandler().addAmount(distFunds, clan.getMembers().getUniqueID());
								distFunds = 0;
							} else {
								//TODO pay leaders
								//Clans.getPaymentHandler().addAmount(distFunds % clan.getMemberCount(), sender.getUniqueID());
								distFunds /= clan.getMemberCount();
							}
							for(UUID member: clan.getMembers().keySet()) {
								Clans.getPaymentHandler().ensureAccountExists(member);
								if(!Clans.getPaymentHandler().addAmount(distFunds, member));//TODO pay leaders
									//Clans.getPaymentHandler().addAmount(distFunds, sender.getUniqueID());
								//TODO send message to member saying it was disbanded.
							}
						}
						clan.updateUpkeepTimeStamp();
					}
				}

			executing = false;
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if(event.player.getEntityWorld().getTotalWorldTime() % 10 == 0) {
			//noinspection ConstantConditions
			assert Clans.CLAIMED_LAND != null;
			if(event.player.hasCapability(Clans.CLAIMED_LAND, null)) {
				UUID chunkClan = event.player.getEntityWorld().getChunk(event.player.getPosition()).getCapability(Clans.CLAIMED_LAND, null).getClan();
				UUID playerStoredClaimId = event.player.getCapability(Clans.CLAIMED_LAND, null).getClan();
				if((chunkClan != null && !chunkClan.equals(playerStoredClaimId)) || (chunkClan == null && playerStoredClaimId != null)) {
					event.player.getCapability(Clans.CLAIMED_LAND, null).setClan(chunkClan);
					String color = MinecraftColors.GREEN;
					Clan playerClan = ClanCache.getPlayerClan(event.player.getUniqueID());
					if((playerClan != null && !playerClan.getClanId().equals(chunkClan)) || (playerClan == null && chunkClan != null))
						color = MinecraftColors.YELLOW;
					if(chunkClan == null)
						color = MinecraftColors.DARK_GREEN;

					event.player.sendMessage(new TextComponentString(color + "You are now entering " + (chunkClan == null ? "Wilderness." : ClanCache.getClan(chunkClan).getClanName()+"'s territory.")));
				}
			}
		}
	}
}
