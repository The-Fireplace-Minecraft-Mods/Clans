package the_fireplace.clans.legacy.commands;

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
import the_fireplace.clans.clan.accesscontrol.ClanPermissions;
import the_fireplace.clans.clan.membership.PlayerClans;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.model.EnumLockType;
import the_fireplace.clans.legacy.model.EnumRank;
import the_fireplace.clans.legacy.util.PermissionManager;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ClanSubCommand extends CommandBase {

	@Override
	public String getUsage(ICommandSender sender) {
		return TranslationUtil.getRawTranslationString(sender, "commands.clan."+getName()+".usage");
	}

	protected UUID selectedClan;
	protected String selectedClanName;

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		if (!PermissionManager.hasPermission(sender, PermissionManager.CLAN_COMMAND_PREFIX + getName(), true)) {
			return false;
		}
		EnumRank requiredRank = getRequiredClanRank();

		boolean isConsole = !(sender instanceof Entity);
		boolean allowsClanlessUsage = requiredRank == EnumRank.ANY || requiredRank == EnumRank.NOCLAN;
		if (isConsole) {
			return allowsClanlessUsage && allowConsoleUsage();
		}

		if (selectedClan == null) {
			return allowsClanlessUsage;
		}

		EnumRank playerRank = PlayerClans.getPlayerRank(Objects.requireNonNull(sender.getCommandSenderEntity()).getUniqueID(), selectedClan);
		switch (requiredRank) {
			case LEADER:
			case ADMIN:
				return ClanPermissions.get(selectedClan).hasPerm(getName(), ((Entity) sender).getUniqueID());
			case MEMBER:
				return playerRank.greaterOrEquals(EnumRank.MEMBER);
			case NOCLAN:
				return playerRank.equals(EnumRank.NOCLAN);
			default:
				return false;
		}
	}

	public abstract EnumRank getRequiredClanRank();
	public abstract int getMinArgs();
	public abstract int getMaxArgs();

	protected final void throwWrongUsage(ICommandSender sender) throws WrongUsageException{
		throw new WrongUsageException(getUsage(sender));
	}

	public final void execute(@Nullable MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (server == null) {
			throw new WrongUsageException(TranslationUtil.getRawTranslationString(sender, "clans.error.nullserver"));
		}
		if (!allowConsoleUsage() && !(sender instanceof EntityPlayerMP)) {
			throw new WrongUsageException(TranslationUtil.getRawTranslationString(sender, "commands.clan.common.player"));
		}
		int maxArgumentCount = getMaxArgs();
		boolean greedyArgs = maxArgumentCount == Integer.MAX_VALUE;
		if (!greedyArgs) {
			// Allow the clan name to be tacked on to the beginning, since the subcommands don't consider it in their setup
			maxArgumentCount++;
		}
		// Add 1 to account for clan name
		int minimumArguments = getMinArgs() + 1;
		if (args.length < minimumArguments || args.length > maxArgumentCount) {
			throwWrongUsage(sender);
		}
		boolean hasClanName = !Objects.equals(args[0], ClanNames.NULL_CLAN_NAME);
		if (hasClanName) {
			UUID playerClan = ClanNames.getClanByName(args[0]);
			if (sender instanceof EntityPlayerMP) {
				Collection<UUID> playerClans = PlayerClans.getClansPlayerIsIn(((EntityPlayerMP) sender).getUniqueID());
				if (playerClan != null && !playerClans.contains(playerClan)) {
					sender.sendMessage(TranslationUtil.getTranslation(((EntityPlayerMP) sender).getUniqueID(), "commands.clan.common.player_not_in_clan", sender.getName(), ClanNames.get(playerClan).getName()).setStyle(TextStyles.RED));
					return;
				}
			}
			//noinspection ConstantConditions
			this.selectedClan = playerClan;
			this.selectedClanName = args[0];
		}
		String[] args2;
		//If this is a clan command, remove clan name from the args, and if the command is greedy, remove the subcommand tag as well
		//Otherwise, this removes the subcommand name
		int removeArgCount = greedyArgs ? 2 : 1;
		if (args.length > removeArgCount) {
			args2 = Arrays.copyOfRange(args, removeArgCount, args.length);
		} else {
			args2 = new String[]{};
		}
		if (checkPermission(server, sender)) {
			if (sender instanceof EntityPlayerMP) {
				run(server, (EntityPlayerMP) sender, args2);
			} else {
				runFromAnywhere(server, sender, args2);
			}
		} else {
			sender.sendMessage(new TextComponentTranslation("commands.generic.permission").setStyle(new Style().setColor(TextFormatting.RED)));
		}
	}

	protected void run(MinecraftServer server, EntityPlayerMP sender, String[] args) throws CommandException {
        runFromAnywhere(server, sender, args);
    }

	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		ClansModContainer.getMinecraftHelper().getLogger().error("This point should not have been reached. Command sender is a {}.", sender.getClass().getCanonicalName());
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
