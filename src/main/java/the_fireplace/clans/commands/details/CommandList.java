package the_fireplace.clans.commands.details;

import com.google.common.collect.Maps;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.clan.NewClanDatabase;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandList extends ClanSubCommand {
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
		return 0;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/clan list";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		assert server != null;
		sender.sendMessage(new TextComponentString("Clans on this server:").setStyle(TextStyles.GREEN));
		if(!NewClanDatabase.getClans().isEmpty()) {
			for (NewClan clan : NewClanDatabase.getClans())
				sender.sendMessage(new TextComponentString(clan.getClanName() + " - " + clan.getDescription()).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(new TextComponentString("There are no clans on this server.").setStyle(TextStyles.YELLOW));
	}
}
