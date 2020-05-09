package the_fireplace.clans.util;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.ClansHelper;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.cache.PlayerCache;
import the_fireplace.clans.data.ClaimData;
import the_fireplace.clans.model.ChunkPositionWithData;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.CoordNodeTree;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;

public class ChunkUtils {
	@Nullable
	public static UUID getChunkOwner(Chunk c){
		return ClaimData.getChunkClanId(c.x, c.z, c.getWorld().provider.getDimension());
	}

	@Nullable
	public static Clan getChunkOwnerClan(Chunk c) {
		UUID chunkOwner = ChunkUtils.getChunkOwner(c);
		if (chunkOwner != null) {
			Clan chunkClan = ClanCache.getClanById(chunkOwner);
			if (chunkClan != null)
				return chunkClan;
			else
				//Remove the uuid as the chunk owner since the uuid is not associated with a clan.
				clearChunkOwner(c);
		}
		return null;
	}

	public static void clearChunkOwner(Chunk c){
		ClaimData.delChunk(getChunkOwner(c), new ChunkPositionWithData(c));
	}

	public static boolean isBorderland(Chunk c) {
		return ClaimData.isBorderland(c.x, c.z, c.getWorld().provider.getDimension());
	}

	public static boolean hasConnectedClaim(Chunk c, @Nullable UUID checkOwner) {
		if(checkOwner == null)
			checkOwner = getChunkOwner(c);
		if(checkOwner == null)
			return false;
		return !getConnectedClaimChunks(c, checkOwner).isEmpty();
	}

	public static boolean hasConnectedClaim(ChunkPositionWithData c, @Nullable UUID checkOwner) {
		if(checkOwner == null)
			checkOwner = ClaimData.getChunkClanId(c);
		if(checkOwner == null)
			return false;
		return !getConnectedClaimPositions(c, checkOwner).isEmpty();
	}

	public static boolean canBeAbandoned(Chunk c, @Nullable UUID checkOwner) {
		if(checkOwner == null)
			checkOwner = getChunkOwner(c);
		if(checkOwner == null)
			return false;
		ChunkPos cPos = c.getPos();
		switch (ClansHelper.getConfig().getConnectedClaimCheck().toLowerCase()) {
			case "quicker":
				ArrayList<Chunk> conn = getConnectedClaimChunks(c, checkOwner);
				for(Chunk chunk: conn) {
					ArrayList<Chunk> connected = getConnectedClaimChunks(chunk, checkOwner);
					connected.remove(c);
					if(connected.isEmpty())
						return false;
				}
				return true;
			case "quick":
				boolean north = checkOwner.equals(getChunkOwner(c.getWorld().getChunk(cPos.x, cPos.z - 1)));
				boolean northeast = checkOwner.equals(getChunkOwner(c.getWorld().getChunk(cPos.x + 1, cPos.z - 1)));
				boolean east = checkOwner.equals(getChunkOwner(c.getWorld().getChunk(cPos.x + 1, cPos.z)));
				boolean southeast = checkOwner.equals(getChunkOwner(c.getWorld().getChunk(cPos.x + 1, cPos.z + 1)));
				boolean south = checkOwner.equals(getChunkOwner(c.getWorld().getChunk(cPos.x, cPos.z + 1)));
				boolean southwest = checkOwner.equals(getChunkOwner(c.getWorld().getChunk(cPos.x - 1, cPos.z + 1)));
				boolean west = checkOwner.equals(getChunkOwner(c.getWorld().getChunk(cPos.x - 1, cPos.z)));
				boolean northwest = checkOwner.equals(getChunkOwner(c.getWorld().getChunk(cPos.x - 1, cPos.z - 1)));

				if(!north && !east && !south && !west)
					return true;
				if(!northeast && !northwest && !southeast && !southwest
						|| north && !northeast && !northwest
						|| east && !northeast && !southeast
						|| south && !southeast && !southwest
						|| west && !southwest && !northwest)
					return false;
				int nullcount = 0;
				if(!north)
					nullcount++;
				if(!east)
					nullcount++;
				if(!south)
					nullcount++;
				if(!west)
					nullcount++;
				if(!northeast)
					nullcount++;
				if(!northwest)
					nullcount++;
				if(!southeast)
					nullcount++;
				if(!southwest)
					nullcount++;
				switch(nullcount) {
					case 0:
					case 1:
					default:
						return true;
					case 2:
						return !north && !northeast
								|| !northeast && !east
								|| !east && !southeast
								|| !southeast && !south
								|| !south && !southwest
								|| !southwest && !west
								|| !west && !northwest
								|| !northwest && !north;
					case 3:
						return !northwest && !north && !northeast
								|| !north && !northeast && !east
								|| !northeast && !east && !southeast
								|| !east && !southeast && !south
								|| !southeast && !south && !southwest
								|| !south && !southwest && !west
								|| !southwest && !west && !northwest
								|| !west && !northwest && !north;
					case 4:
						return north == !south
								&& northeast == !southwest
								&& east == !west
								&& southeast == !northwest;
					case 5:
						return northwest && north && northeast
								|| north && northeast && east
								|| northeast && east && southeast
								|| east && southeast && south
								|| southeast && south && southwest
								|| south && southwest && west
								|| southwest && west && northwest
								|| west && northwest && north;
					case 6:
						return north && northeast
								|| northeast && east
								|| east && southeast
								|| southeast && south
								|| south && southwest
								|| southwest && west
								|| west && northwest
								|| northwest && north;
				}
				
			case "smart":
			default:
				return !new CoordNodeTree(c.x, c.z, c.getWorld().provider.getDimension(), checkOwner).forDisconnectionCheck().hasDetachedNodes();
		}
	}

    public static ArrayList<Chunk> getConnectedClaimChunks(Chunk c, @Nullable UUID checkOwner) {
	    ArrayList<Chunk> adjacent = Lists.newArrayList();
        if(checkOwner == null)
            checkOwner = getChunkOwner(c);
        if(checkOwner == null)
            return adjacent;
        final UUID checkOwnerFinal = checkOwner;
        ChunkPos cPos = c.getPos();
        adjacent.add(c.getWorld().getChunk(cPos.x + 1, cPos.z));
		adjacent.add(c.getWorld().getChunk(cPos.x - 1, cPos.z));
		adjacent.add(c.getWorld().getChunk(cPos.x, cPos.z + 1));
		adjacent.add(c.getWorld().getChunk(cPos.x, cPos.z - 1));
		adjacent.removeIf(c2 -> !checkOwnerFinal.equals(getChunkOwner(c2)) || isBorderland(c2));
        return adjacent;
    }

	public static ArrayList<ChunkPositionWithData> getConnectedClaimPositions(ChunkPositionWithData c, @Nullable UUID checkOwner) {
		ArrayList<ChunkPositionWithData> adjacent = Lists.newArrayList();
		if(checkOwner == null)
			checkOwner = ClaimData.getChunkClanId(c);
		if(checkOwner == null)
			return adjacent;
		final UUID checkOwnerFinal = checkOwner;
		adjacent.add(c.offset(1, 0));
		adjacent.add(c.offset(-1, 0));
		adjacent.add(c.offset(0, 1));
		adjacent.add(c.offset(0, -1));
		adjacent.removeIf(c2 -> !checkOwnerFinal.equals(ClaimData.getChunkClanId(c2)) || ClaimData.getChunkPositionData(c2).isBorderland());
		return adjacent;
	}

	public static void showChunkBounds(Chunk c, EntityPlayerMP player) {
		NetHandlerPlayServer conn = player.connection;
		if(conn == null)
			return;
		World w = player.getEntityWorld();
		int xStart = c.getPos().getXStart();
		int xEnd = c.getPos().getXEnd();
		int zStart = c.getPos().getZStart();
		int zEnd = c.getPos().getZEnd();

		sendGlowStoneToPositions(player, w,
				//Corners
				w.getTopSolidOrLiquidBlock(new BlockPos(xStart, 64, zStart)).down(),
				w.getTopSolidOrLiquidBlock(new BlockPos(xStart+(xEnd > xStart ? 1 : -1), 64, zStart)).down(),
				w.getTopSolidOrLiquidBlock(new BlockPos(xStart, 64, zStart+(zEnd > zStart ? 1 : -1))).down(),

				w.getTopSolidOrLiquidBlock(new BlockPos(xStart, 64, zEnd)).down(),
				w.getTopSolidOrLiquidBlock(new BlockPos(xStart+(xEnd > xStart ? 1 : -1), 64, zEnd)).down(),
				w.getTopSolidOrLiquidBlock(new BlockPos(xStart, 64, zEnd+(zEnd > zStart ? -1 : 1))).down(),

				w.getTopSolidOrLiquidBlock(new BlockPos(xEnd, 64, zEnd)).down(),
				w.getTopSolidOrLiquidBlock(new BlockPos(xEnd+(xEnd > xStart ? -1 : 1), 64, zEnd)).down(),
				w.getTopSolidOrLiquidBlock(new BlockPos(xEnd, 64, zEnd+(zEnd > zStart ? -1 : 1))).down(),

				w.getTopSolidOrLiquidBlock(new BlockPos(xEnd, 64, zStart)).down(),
				w.getTopSolidOrLiquidBlock(new BlockPos(xEnd+(xEnd > xStart ? -1 : 1), 64, zStart)).down(),
				w.getTopSolidOrLiquidBlock(new BlockPos(xEnd, 64, zStart+(zEnd > zStart ? 1 : -1))).down(),

				//Midpoints
				w.getTopSolidOrLiquidBlock(new BlockPos((xStart+xEnd)/2, 64, zStart)).down(),
				w.getTopSolidOrLiquidBlock(new BlockPos((xStart+xEnd)/2, 64, zEnd)).down(),
				w.getTopSolidOrLiquidBlock(new BlockPos(xStart, 64, (zStart+zEnd)/2)).down(),
				w.getTopSolidOrLiquidBlock(new BlockPos(xEnd, 64, (zStart+zEnd)/2)).down()
		);
	}

	public static void sendUpdatesToPositions(NetHandlerPlayServer conn, World w, Iterable<BlockPos> positions) {
		for(BlockPos pos: positions)
			conn.sendPacket(NetworkUtils.createFakeBlockChange(w, pos, w.getBlockState(pos)));
	}

	private static void sendGlowStoneToPositions(EntityPlayerMP player, World w, BlockPos... positions) {
		PlayerCache.updateBorderDisplayCache(player, positions);
		for(BlockPos pos: positions)
			player.connection.sendPacket(NetworkUtils.createFakeBlockChange(w, pos, Blocks.GLOWSTONE.getDefaultState()));
	}
}
