package the_fireplace.clans.commands.lock;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.model.EnumLockType;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.EntityUtil;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandLock extends ClanSubCommand {
	@Override
	public String getName() {
		return "lock";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.ADMIN;
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
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		EnumLockType mode;
		if(args.length == 0)
			mode = EnumLockType.PRIVATE;
		else
			switch(args[0].toLowerCase()) {
				case "private":
				case "p":
					mode = EnumLockType.PRIVATE;
					break;
				case "clan":
				case "c":
					mode = EnumLockType.CLAN;
					break;
				case "open":
				case "public":
				case "o":
					mode = EnumLockType.OPEN;
					break;
				default:
					throw new WrongUsageException(getUsage(sender));
			}
		RayTraceResult lookRay = EntityUtil.getLookRayTrace(sender, 4);
		if(lookRay == null || lookRay.typeOfHit != RayTraceResult.Type.BLOCK) {
			sender.sendMessage(TranslationUtil.getTranslation("commands.clan.lock.not_block").setStyle(TextStyles.RED));
			return;
		}
		BlockPos targetBlockPos = lookRay.getBlockPos();
	}
}
