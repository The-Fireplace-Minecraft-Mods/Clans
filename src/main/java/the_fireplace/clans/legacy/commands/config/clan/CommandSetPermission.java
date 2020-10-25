package the_fireplace.clans.legacy.commands.config.clan;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSetPermission extends ClanSubCommand {
	@Override
	public String getName() {
		return "set";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.LEADER;
	}

	@Override
	public int getMinArgs() {
		return 2;
	}

	@Override
	public int getMaxArgs() {
		return 3;
	}

    @Override
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		String perm = args[0];
		if(!Clan.DEFAULT_PERMISSIONS.containsKey(perm))
			throw new IllegalArgumentException(TranslationUtil.getStringTranslation(sender.getUniqueID(), "commands.clan.set.invalid_perm", perm));
		if(args.length == 3) {
			GameProfile player = parsePlayerName(server, args[1]);
			boolean value = parseBool(args[2]);
			selectedClan.addPermissionOverride(perm, player.getId(), value);
		} else {
			EnumRank rank = EnumRank.valueOf(args[1]);
			selectedClan.setPerm(perm, rank);
		}
		sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.set.success").setStyle(TextStyles.GREEN));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		List<String> ret = Lists.newArrayList();
		if(args.length == 1)
			ret.addAll(Clan.DEFAULT_PERMISSIONS.keySet());
		else if(args.length == 2) {
			for(EnumRank rank: EnumRank.values())
				if(!rank.equals(EnumRank.NOCLAN))
					ret.add(rank.name());
			for(GameProfile profile: server.getOnlinePlayerProfiles())
				ret.add(profile.getName());
		} else {
			ret.add("true");
			ret.add("false");
		}
		return getListOfStringsMatchingLastWord(args, ret);
	}
}
