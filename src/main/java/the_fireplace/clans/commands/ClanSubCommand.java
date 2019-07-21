package the_fireplace.clans.commands;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.data.ClanDatabase;
import the_fireplace.clans.data.PlayerDataManager;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ClanSubCommand extends CommandBase {
	@Override
	public String getName() {
		return "clan";
	}

	protected Clan selectedClan;
	protected Clan opSelectedClan;

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		if(sender instanceof Entity) {
			if(getRequiredClanRank() == EnumRank.ANY)
				return true;
			if(selectedClan != null) {
				EnumRank playerRank = ClanCache.getPlayerRank(Objects.requireNonNull(sender.getCommandSenderEntity()).getUniqueID(), selectedClan);
				switch (playerRank) {
					case LEADER:
						return !getRequiredClanRank().equals(EnumRank.NOCLAN);
					case ADMIN:
						return !getRequiredClanRank().equals(EnumRank.LEADER) && !getRequiredClanRank().equals(EnumRank.NOCLAN);
					case MEMBER:
						return getRequiredClanRank().equals(EnumRank.MEMBER);
					case NOCLAN:
						return getRequiredClanRank().equals(EnumRank.NOCLAN);
					default:
						return false;
				}
			} else
				return getRequiredClanRank().equals(EnumRank.NOCLAN);
		}
		return false;
	}

	public abstract EnumRank getRequiredClanRank();
	public abstract int getMinArgs();
	public abstract int getMaxArgs();

	protected final void throwWrongUsage(ICommandSender sender) throws WrongUsageException{
		throw new WrongUsageException(getUsage(sender));
	}

	public final void execute(@Nullable MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(server == null)
			throw new WrongUsageException(TranslationUtil.getRawTranslationString(sender, "clans.error.nullserver"));
		if(allowConsoleUsage() || sender instanceof EntityPlayerMP) {
			boolean greedyArgs = getMaxArgs() == Integer.MAX_VALUE;
			if(args.length >= getMinArgs() && args.length <= (greedyArgs ? getMaxArgs() : getMaxArgs()+1)) {
				Clan playerClan = null;
				if(greedyArgs ? args.length > 1 && CommandClan.greedyCommands.contains(args[1]) : args.length == getMaxArgs()+1) {
					playerClan = ClanCache.getClanByName(args[0]);
					opSelectedClan = playerClan;
				} else if(sender instanceof EntityPlayerMP)
					playerClan = ClanCache.getClanById(PlayerDataManager.getDefaultClan(((EntityPlayerMP) sender).getUniqueID()));
				if(sender instanceof EntityPlayerMP) {
					ArrayList<Clan> playerClans = ClanCache.getPlayerClans(((EntityPlayerMP) sender).getUniqueID());
					if (playerClan != null && !playerClans.contains(playerClan) && !(this instanceof OpClanSubCommand)) {
						sender.sendMessage(TranslationUtil.getTranslation(((EntityPlayerMP) sender).getUniqueID(), "commands.clan.common.not_in_clan", playerClan.getClanName()).setStyle(TextStyles.RED));
						return;
					}
				}
				this.selectedClan = playerClan;
				if(this.opSelectedClan == null)
					this.opSelectedClan = ClanDatabase.getOpClan();
				String[] args2 = args;
				if(args.length == getMaxArgs()+1) {
					if (args.length > 1)
						args2 = Arrays.copyOfRange(args, 1, args.length);
					else
						args2 = new String[]{};
				}
				if(checkPermission(server, sender)) {
					if (sender instanceof EntityPlayerMP)
						run(server, (EntityPlayerMP) sender, args2);
					else
						runFromAnywhere(server, sender, args2);
				} else
					sender.sendMessage(new TextComponentTranslation("commands.generic.permission").setStyle(new Style().setColor(TextFormatting.RED)));
			} else
				throwWrongUsage(sender);
		} else
			throw new WrongUsageException(TranslationUtil.getRawTranslationString(sender, "commands.clan.common.player"));
	}

	protected void run(MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
        runFromAnywhere(server, sender, args);
    }

	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		Clans.getMinecraftHelper().getLogger().error("This point should not have been reached. Command sender is a {}.", sender.getClass().getCanonicalName());
		throw new WrongUsageException(TranslationUtil.getRawTranslationString(sender, "commands.clan.common.player"));
	}

	protected boolean allowConsoleUsage() {
		return false;
	}
}
