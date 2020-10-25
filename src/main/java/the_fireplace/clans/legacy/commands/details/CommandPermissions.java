package the_fireplace.clans.legacy.commands.details;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import the_fireplace.clans.legacy.commands.ClanSubCommand;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.ChatUtil;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandPermissions extends ClanSubCommand {
	@Override
	public String getName() {
		return "permissions";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.MEMBER;
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
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
		List<ITextComponent> texts = Lists.newArrayList();
		for(Map.Entry<String, EnumRank> entry: selectedClan.getPermissions().entrySet()) {
			ITextComponent add = TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.permissions.format", entry.getKey(), entry.getValue().name());
			for(Map.Entry<UUID, Boolean> overEntry: selectedClan.getPermissionOverrides().get(entry.getKey()).entrySet())
				add = add.appendText("\n").appendSibling(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.permissions.override", server.getPlayerProfileCache().getProfileByUUID(overEntry.getKey()).getName(), overEntry.getValue()));
			texts.add(add);
		}
		int page;
		if(args.length > 0)
			page = parseInt(args[0]);
		else
			page = 1;
		ChatUtil.showPaginatedChat(sender, "/clan "+selectedClan.getName()+" permissions %s", texts, page);
	}
}
