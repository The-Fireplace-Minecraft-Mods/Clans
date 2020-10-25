package the_fireplace.clans.legacy.commands.config.player;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanMemberCache;
import the_fireplace.clans.clan.ClanNameCache;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.player.PlayerClanSettings;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSetDefault extends ClanSubCommand {
	@Override
	public String getName() {
		return "setdefault";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.MEMBER;
	}

	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}

	@Override
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Clan def = ClanNameCache.getClanByName(args[0]);
		if(def != null) {
			if(def.getMembers().containsKey(sender.getUniqueID())) {
				PlayerClanSettings.setDefaultClan(sender.getUniqueID(), def.getId());
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.setdefault.success", def.getName()).setStyle(TextStyles.GREEN));
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(),"commands.clan.common.not_in_clan", def.getName()).setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(),"commands.clan.common.notfound").setStyle(TextStyles.RED));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		List<String> ret = Lists.newArrayList();
		if(args.length == 1 && sender instanceof EntityPlayerMP)
			for(Clan c: ClanMemberCache.getClansPlayerIsIn(((EntityPlayerMP) sender).getUniqueID()))
				ret.add(c.getName());
		return getListOfStringsMatchingLastWord(args, ret);
	}
}
