package the_fireplace.clans.commands.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.NetworkUtils;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSeeChunk extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.ANY;
	}

	@Override
	public int getMinArgs() {
		return 0;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.seechunk.usage");
	}

	@SuppressWarnings("Duplicates")
    @Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		assert server != null;
		World w = sender.getEntityWorld();
		Chunk c = w.getChunk(sender.getPosition());
		if(args.length == 1) {
            if(parseBool(args[0])) {

            } else {

            }
		}
		showBounds(c, sender);
	}

	public static void showBounds(Chunk c, EntityPlayerMP player) {
		NetHandlerPlayServer conn = player.connection;
		if(conn == null)
			return;
		World w = player.getEntityWorld();
		int xStart = c.getPos().getXStart();
		int xEnd = c.getPos().getXEnd();
		int zStart = c.getPos().getZStart();
		int zEnd = c.getPos().getZEnd();

		sendGlowStoneToPositions(conn, w,
				//Corners
				w.getTopSolidOrLiquidBlock(new BlockPos(xStart, 64, zStart)),
				w.getTopSolidOrLiquidBlock(new BlockPos(xStart+(xEnd > xStart ? 1 : -1), 64, zStart)),
				w.getTopSolidOrLiquidBlock(new BlockPos(xStart, 64, zStart+(zEnd > zStart ? 1 : -1))),

				w.getTopSolidOrLiquidBlock(new BlockPos(xStart, 64, zEnd)),
				w.getTopSolidOrLiquidBlock(new BlockPos(xStart+(xEnd > xStart ? 1 : -1), 64, zEnd)),
				w.getTopSolidOrLiquidBlock(new BlockPos(xStart, 64, zEnd+(zEnd > zStart ? -1 : 1))),

				w.getTopSolidOrLiquidBlock(new BlockPos(xEnd, 64, zEnd)),
				w.getTopSolidOrLiquidBlock(new BlockPos(xEnd+(xEnd > xStart ? -1 : 1), 64, zEnd)),
				w.getTopSolidOrLiquidBlock(new BlockPos(xEnd, 64, zEnd+(zEnd > zStart ? -1 : 1))),

				w.getTopSolidOrLiquidBlock(new BlockPos(xEnd, 64, zStart)),
				w.getTopSolidOrLiquidBlock(new BlockPos(xEnd+(xEnd > xStart ? -1 : 1), 64, zStart)),
				w.getTopSolidOrLiquidBlock(new BlockPos(xEnd, 64, zStart+(zEnd > zStart ? 1 : -1))),
		
				//Midpoints
				w.getTopSolidOrLiquidBlock(new BlockPos((xStart+xEnd)/2, 64, zStart)),
				w.getTopSolidOrLiquidBlock(new BlockPos((xStart+xEnd)/2, 64, zEnd)),
				w.getTopSolidOrLiquidBlock(new BlockPos(xStart, 64, (zStart+zEnd)/2)),
				w.getTopSolidOrLiquidBlock(new BlockPos(xEnd, 64, (zStart+zEnd)/2))
		);
	}
	
	private static void sendGlowStoneToPositions(NetHandlerPlayServer conn, World w, BlockPos... positions) {
	    //TODO track positions so we can clear this when the player changes chunks?
		for(BlockPos pos: positions)
			conn.sendPacket(NetworkUtils.createFakeBlockChange(w, pos, Blocks.GLOWSTONE.getDefaultState()));
	}
}
