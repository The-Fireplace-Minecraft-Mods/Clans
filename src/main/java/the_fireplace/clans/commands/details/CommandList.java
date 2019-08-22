package the_fireplace.clans.commands.details;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.data.ClanDatabase;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.ChatPageUtil;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
		return 2;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return TranslationUtil.getRawTranslationString(sender, "commands.clan.list.usage");
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.list.clans").setStyle(TextStyles.GREEN));
		if(!ClanDatabase.getClans().isEmpty()) {
			ArrayList<Clan> clans = Lists.newArrayList(ClanDatabase.getClans());
			if(args.length > 0)
				switch (args[0]) {
					case "money":
						clans.sort(Comparator.comparingLong(clan -> Clans.getPaymentHandler().getBalance(clan.getClanId())));
						break;
					case "land":
					case "claims":
						clans.sort(Comparator.comparingInt(Clan::getClaimCount));
						break;
					case "members":
						clans.sort(Comparator.comparingInt(Clan::getMemberCount));
				}
			else
				clans.sort(Comparator.comparing(Clan::getClanName));
			ArrayList<ITextComponent> listItems = Lists.newArrayList();
			for (Clan clan : clans)
				listItems.add(TranslationUtil.getTranslation(sender, "commands.clan.list.listitem", clan.getClanName(), clan.getDescription()).setStyle(TextStyles.GREEN));
			int page;
			if(args.length > 1)
				page = parseInt(args[1]);
			else
				page = 1;
			ChatPageUtil.showPaginatedChat(sender, String.format("/clan list %s", args.length > 0 ? args[0] : "abc") + " %s", listItems, page);
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.list.noclans").setStyle(TextStyles.YELLOW));
	}

	@Override
	protected boolean allowConsoleUsage() {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		List<String> ret = Lists.newArrayList();
		if(args.length == 1)
			ret.addAll(Lists.newArrayList("money", "land", "members", "abc"));
		else if(args.length == 2)
			for(int i = 1; i < ClanDatabase.getClans().size()/ChatPageUtil.RESULTS_PER_PAGE; i++)
				ret.add(String.valueOf(i));
		return ret;
	}
}
