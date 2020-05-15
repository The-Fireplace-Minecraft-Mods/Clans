package the_fireplace.clans.commands.config.player;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.data.PlayerData;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

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
		Clan def = ClanCache.getClanByName(args[0]);
		if(def != null) {
			if(def.getMembers().containsKey(sender.getUniqueID())) {
				PlayerData.setDefaultClan(sender.getUniqueID(), def.getId());
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
			for(Clan c: ClanCache.getPlayerClans(((EntityPlayerMP) sender).getUniqueID()))
				ret.add(c.getName());
		return getListOfStringsMatchingLastWord(args, ret);
	}
}
