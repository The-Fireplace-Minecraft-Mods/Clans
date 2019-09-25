package the_fireplace.clans.commands;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import the_fireplace.clans.Clans;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumLockType;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.PermissionManager;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ClanSubCommand extends CommandBase {

	@Override
	public String getUsage(ICommandSender sender) {
		return TranslationUtil.getRawTranslationString(sender, "commands.clan."+getName()+".usage");
	}

	protected Clan selectedClan;

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		if(PermissionManager.permissionManagementExists() && !PermissionManager.hasPermission(sender, PermissionManager.CLAN_COMMAND_PREFIX + getName()))
			return false;
		if(getRequiredClanRank() == EnumRank.ANY || getRequiredClanRank() == EnumRank.NOCLAN && allowConsoleUsage() && !(sender instanceof Entity))
			return true;
		if(sender instanceof Entity) {
			if(selectedClan != null) {
				EnumRank playerRank = ClanCache.getPlayerRank(Objects.requireNonNull(sender.getCommandSenderEntity()).getUniqueID(), selectedClan);
				switch (getRequiredClanRank()) {
					case LEADER:
					case ADMIN:
						return selectedClan.hasPerm(getName(), ((Entity) sender).getUniqueID());
					case MEMBER:
						return playerRank.greaterOrEquals(EnumRank.MEMBER);
					case NOCLAN:
						return playerRank.equals(EnumRank.NOCLAN);
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
			if(args.length >= getMinArgs()+1 && args.length <= (greedyArgs ? getMaxArgs() : getMaxArgs()+1)) {
				if(args.length > 0) {
					Clan playerClan = ClanCache.getClanByName(args[0]);
					if (sender instanceof EntityPlayerMP) {
						List<Clan> playerClans = ClanCache.getPlayerClans(((EntityPlayerMP) sender).getUniqueID());
						if (playerClan != null && !playerClans.contains(playerClan)) {
							sender.sendMessage(TranslationUtil.getTranslation(((EntityPlayerMP) sender).getUniqueID(), "commands.clan.common.player_not_in_clan", playerClan.getName()).setStyle(TextStyles.RED));
							return;
						}
					}
					//noinspection ConstantConditions
					this.selectedClan = playerClan;
				}
				String[] args2;
				//If this is a clan command, remove clan name from the args, and if the command is greedy, remove the subcommand tag as well
				//Otherwise, this removes the subcommand name
				if (greedyArgs) {
					if (args.length > 2)
						args2 = Arrays.copyOfRange(args, 2, args.length);
					else
						args2 = new String[]{};
				} else if (args.length > 1)
					args2 = Arrays.copyOfRange(args, 1, args.length);
				else
					args2 = new String[]{};
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

	private static final List<String> onStrings = Lists.newArrayList("on", "true", "t");
	private static final List<String> offStrings = Lists.newArrayList("off", "false", "f");

	public static boolean parseBool(String arg) throws CommandException {
		return Objects.requireNonNull(parseBool(arg, false));
	}

	@Nullable
	public static Boolean parseBool(String arg, boolean nullOnFail) throws CommandException {
		if(arg.matches("\\d+") && parseInt(arg) == 1 || onStrings.contains(arg.toLowerCase()))
			return true;
		else if(arg.matches("\\d+") && parseInt(arg) == 0 || offStrings.contains(arg.toLowerCase()))
			return false;
		if(nullOnFail)
			return null;
		else
			throw new CommandException("commands.clan.common.not_boolean", arg);
	}

	public static GameProfile parsePlayerName(MinecraftServer server, String username) throws CommandException {
		GameProfile result = server.getPlayerProfileCache().getGameProfileForUsername(username);
		if(result == null)
			throw new PlayerNotFoundException("commands.generic.player.notFound", username);
		return result;
	}

	public static EnumLockType parseLockType(@Nullable String type) throws WrongUsageException {
		if(type == null)
			return EnumLockType.PRIVATE;
		else
			switch(type.toLowerCase()) {
				case "private":
				case "p":
					return EnumLockType.PRIVATE;
				case "clan":
				case "c":
					return EnumLockType.CLAN;
				case "open":
				case "public":
				case "o":
					return EnumLockType.OPEN;
				default:
					throw new WrongUsageException("commands.clan.common.invalid_lock_type");
			}
	}
}
