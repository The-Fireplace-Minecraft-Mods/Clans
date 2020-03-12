package the_fireplace.clans.commands.invites;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.ClansHelper;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.data.PlayerData;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandDecline extends ClanSubCommand {
	@Override
	public String getName() {
		return "decline";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return ClansHelper.getConfig().isAllowMultiClanMembership() ? EnumRank.ANY : EnumRank.NOCLAN;
	}

	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public int getMaxArgs() {
		return 2;
	}

	@Override
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Clan declineClan = ClanCache.getClanByName(args[0]);
		if(declineClan != null) {
			if(PlayerData.getInvites(sender.getUniqueID()).contains(declineClan.getId())) {
				PlayerData.removeInvite(sender.getUniqueID(), declineClan.getId());
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.decline.success", declineClan.getName()).setStyle(TextStyles.GREEN));
				declineClan.messageAllOnline(EnumRank.ADMIN, TextStyles.YELLOW, "commands.clan.decline.declined", sender.getDisplayNameString(), declineClan.getName());
			} else if(args.length < 2)
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.invite.not_invited", args[0]).setStyle(TextStyles.RED));
			else// if(args[1].equalsIgnoreCase("block"))//TODO add error message if they put an invalid argument, instead of accepting anything
				CommandAutoDecline.toggleClanInviteBlock(sender, declineClan.getId());
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.common.notfound", args[0]).setStyle(TextStyles.RED));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		if(args.length == 1 && sender instanceof EntityPlayerMP) {
			List<String> clanNames = Lists.newArrayList();
			for(UUID c: PlayerData.getInvites(((EntityPlayerMP) sender).getUniqueID()))
				clanNames.add(Objects.requireNonNull(ClanCache.getClanById(c)).getName());
			return clanNames;
		} else if(args.length == 2)
			return Collections.singletonList("block");
		return Collections.emptyList();
	}
}
