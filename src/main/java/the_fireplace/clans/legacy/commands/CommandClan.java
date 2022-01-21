package the_fireplace.clans.legacy.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.clan.membership.PlayerClans;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.commands.config.clan.*;
import the_fireplace.clans.legacy.commands.config.player.CommandSetDefault;
import the_fireplace.clans.legacy.commands.config.player.CommandTerritoryMessageMode;
import the_fireplace.clans.legacy.commands.config.player.CommandUndergroundMessages;
import the_fireplace.clans.legacy.commands.details.*;
import the_fireplace.clans.legacy.commands.finance.CommandAddFunds;
import the_fireplace.clans.legacy.commands.finance.CommandBalance;
import the_fireplace.clans.legacy.commands.finance.CommandSetRent;
import the_fireplace.clans.legacy.commands.finance.CommandTakeFunds;
import the_fireplace.clans.legacy.commands.invites.CommandAccept;
import the_fireplace.clans.legacy.commands.invites.CommandAutoDecline;
import the_fireplace.clans.legacy.commands.invites.CommandDecline;
import the_fireplace.clans.legacy.commands.invites.CommandInvite;
import the_fireplace.clans.legacy.commands.land.*;
import the_fireplace.clans.legacy.commands.lock.*;
import the_fireplace.clans.legacy.commands.members.CommandDemote;
import the_fireplace.clans.legacy.commands.members.CommandKick;
import the_fireplace.clans.legacy.commands.members.CommandLeave;
import the_fireplace.clans.legacy.commands.members.CommandPromote;
import the_fireplace.clans.legacy.commands.teleportation.CommandHome;
import the_fireplace.clans.legacy.commands.teleportation.CommandTrapped;
import the_fireplace.clans.legacy.util.PermissionManager;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;
import the_fireplace.clans.player.PlayerClanSettings;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandClan extends CommandBase
{
    public static final Map<String, ClanSubCommand> COMMANDS = new HashMap<String, ClanSubCommand>()
    {{
        //land claiming
        put("claim", new CommandClaim());
        put("abandonclaim", new CommandAbandonClaim());
        put("abandonall", new CommandAbandonAll());
        put("autoclaim", new CommandAutoClaim());
        put("autoabandon", new CommandAutoAbandon());
        put("map", new CommandMap());
        put("fancymap", new CommandFancyMap());
        put("seechunk", new CommandSeeChunk());
        //invites
        put("invite", new CommandInvite());
        put("accept", new CommandAccept());
        put("decline", new CommandDecline());
        put("autodecline", new CommandAutoDecline());
        //managing members
        put("kick", new CommandKick());
        put("leave", new CommandLeave());
        put("promote", new CommandPromote());
        put("demote", new CommandDemote());
        //clan constants/details/other
        put("form", new CommandForm());
        put("disband", new CommandDisband());
        put("banner", new CommandBanner());
        put("details", new CommandDetails());
        put("playerinfo", new CommandPlayerInfo());
        put("list", new CommandList());
        put("clanchat", new CommandClanChat());
        //clan config
        put("setname", new CommandSetName());
        put("setdescription", new CommandSetDescription());
        put("setcolor", new CommandSetColor());
        put("sethome", new CommandSetHome());
        put("unsethome", new CommandUnsetHome());
        put("setbanner", new CommandSetBanner());
        //player config
        put("setdefault", new CommandSetDefault());
        put("territorymessagemode", new CommandTerritoryMessageMode());
        if (ClansModContainer.getConfig().shouldProtectWilderness()) {
            put("undergroundmessages", new CommandUndergroundMessages());
        }
        //teleportation related
        put("home", new CommandHome());
        put("trapped", new CommandTrapped());
        //clan finances
        if (Economy.isPresent()) {
            put("balance", new CommandBalance());
            put("addfunds", new CommandAddFunds());
            put("takefunds", new CommandTakeFunds());
            put("setrent", new CommandSetRent());
        }
        //help
        put("help", new CommandClanHelp());
        //permissions
        put("permissions", new CommandPermissions());
        put("set", new CommandSetPermission());
        //locks
        put("lock", new CommandLock());
        put("lockchunk", new CommandLockChunk());
        put("unlock", new CommandUnlock());
        put("unlockchunk", new CommandUnlockChunk());
        put("grantaccess", new CommandGrantAccess());
        put("denyaccess", new CommandDenyAccess());
        put("lockinfo", new CommandLockInfo());
    }};

    public static final Map<String, String> COMMAND_ALIASES = Maps.newHashMap();
    private static final List<String> FINANCE_COMMANDS = Lists.newArrayList("balance", "addfunds", "takefunds", "setrent", "finances");

    static {
        //land
        COMMAND_ALIASES.put("c", "claim");
        COMMAND_ALIASES.put("cc", "clanchat");
        COMMAND_ALIASES.put("chat", "clanchat");
        COMMAND_ALIASES.put("abc", "abandonclaim");
        COMMAND_ALIASES.put("unclaim", "abandonclaim");
        COMMAND_ALIASES.put("uc", "abandonclaim");
        COMMAND_ALIASES.put("ac", "autoclaim");
        COMMAND_ALIASES.put("aa", "autoabandon");
        COMMAND_ALIASES.put("m", "map");
        COMMAND_ALIASES.put("fm", "fancymap");
        COMMAND_ALIASES.put("sc", "seechunk");
        //members
        COMMAND_ALIASES.put("ad", "autodecline");
        COMMAND_ALIASES.put("block", "autodecline");
        COMMAND_ALIASES.put("i", "invite");
        COMMAND_ALIASES.put("inv", "invite");
        COMMAND_ALIASES.put("join", "accept");
        //clan constants/details/other
        COMMAND_ALIASES.put("create", "form");
        COMMAND_ALIASES.put("b", "banner");
        COMMAND_ALIASES.put("clan", "details");
        COMMAND_ALIASES.put("info", "details");
        COMMAND_ALIASES.put("d", "details");
        COMMAND_ALIASES.put("pi", "playerinfo");
        COMMAND_ALIASES.put("player", "playerinfo");
        //clan config
        COMMAND_ALIASES.put("setcolour", "setcolor");
        COMMAND_ALIASES.put("setdesc", "setdescription");
        //player config
        COMMAND_ALIASES.put("tmm", "territorymessagemode");
        if (ClansModContainer.getConfig().shouldProtectWilderness()) {
            COMMAND_ALIASES.put("um", "undergroundmessages");
        }
        //teleportation related
        COMMAND_ALIASES.put("h", "home");
        COMMAND_ALIASES.put("t", "trapped");
        COMMAND_ALIASES.put("unstuck", "trapped");
        //finances
        COMMAND_ALIASES.put("deposit", "addfunds");
        COMMAND_ALIASES.put("af", "addfunds");
        COMMAND_ALIASES.put("withdraw", "takefunds");
        //permissions
        COMMAND_ALIASES.put("perms", "permissions");
        COMMAND_ALIASES.put("setperm", "set");
        //locks
        COMMAND_ALIASES.put("l", "lock");
        COMMAND_ALIASES.put("lc", "lockchunk");
        COMMAND_ALIASES.put("ul", "unlock");
        COMMAND_ALIASES.put("ulc", "unlockchunk");
        COMMAND_ALIASES.put("ga", "grantaccess");
        COMMAND_ALIASES.put("da", "denyaccess");
        COMMAND_ALIASES.put("li", "lockinfo");
    }

    public static String processAlias(String subCommand) {
        return COMMAND_ALIASES.getOrDefault(subCommand, subCommand);
    }

    @Override
    public String getName() {
        return "clan";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return TranslationUtil.getRawTranslationString(sender, "commands.clan.usage");
    }

    static final Set<String> GREEDY_COMMANDS = Sets.newHashSet("setdesc", "setdescription");

    @Override
    public void execute(@Nullable MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length <= 0) {
            throw new WrongUsageException(getUsage(sender));
        }
        String tag = args[0].toLowerCase();
        //Remove the subcommand from the args
        if (ClanNames.isClanNameUsed(tag) && args.length >= 2) {
            //Skip to the next arg because the first is a clan name
            tag = args[1];
            if (args.length > 2) {
                String[] commArgs = Arrays.copyOfRange(args, 2, args.length);
                //If the command is greedy, we leave the subcommand in the args so it can be identified as greedy when the subcommand handler executes.
                args = ArrayUtils.addAll(new String[]{args[0]}, GREEDY_COMMANDS.contains(tag) ? ArrayUtils.addAll(new String[]{tag}, commArgs) : commArgs);
            } else {
                args = new String[]{args[0]};
            }
        } else {
            //Skip greedy commands because this would cause part of the description to be cut off
            if (!GREEDY_COMMANDS.contains(tag)) {
                if (args.length > 1) {
                    args = Arrays.copyOfRange(args, 1, args.length);
                } else {
                    args = new String[]{};
                }
            }
            //Make the first arg the default clan name because a clan name was not specified
            UUID defaultClan = sender instanceof EntityPlayerMP ? PlayerClanSettings.getDefaultClan(((EntityPlayerMP) sender).getUniqueID()) : null;
            args = ArrayUtils.addAll(new String[]{defaultClan != null ? ClanNames.get(defaultClan).getName() : ClanNames.NULL_CLAN_NAME}, args);
        }
        //Check permissions and run command
        if (PermissionManager.hasPermission(sender, PermissionManager.CLAN_COMMAND_PREFIX + processAlias(tag), true)) {
            if (Economy.isPresent() || !FINANCE_COMMANDS.contains(processAlias(tag))) {
                if (COMMANDS.containsKey(processAlias(tag))) {
                    COMMANDS.get(processAlias(tag)).execute(server, sender, args);
                } else {
                    throw new WrongUsageException(getUsage(sender));
                }
                return;
            }
        } else if (COMMANDS.containsKey(tag) || COMMAND_ALIASES.containsKey(tag)) {
            throw new CommandException("commands.generic.permission");
        }
        throw new WrongUsageException(getUsage(sender));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> getAliases() {
        return Lists.newArrayList("c");
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        String[] args2;
        if (args.length > 1) {
            args2 = Arrays.copyOfRange(args, 1, args.length);
        } else {
            args2 = new String[]{};
        }
        if (args.length >= 1) {
            if (args.length == 1) {
                List<String> arg1List = Lists.newArrayList(COMMANDS.keySet());
                if (sender instanceof EntityPlayerMP) {
                    for (UUID c : PlayerClans.getClansPlayerIsIn(((EntityPlayerMP) sender).getUniqueID())) {
                        arg1List.add(ClanNames.get(c).getName());
                    }
                }
                return getListOfStringsMatchingLastWord(args, arg1List);
            } else if (COMMANDS.containsKey(processAlias(args[0]))) {
                return COMMANDS.get(processAlias(args[0])).getTabCompletions(server, sender, args2, targetPos);
            } else if (ClanNames.getClanNames().contains(args[0])) {
                if (args.length == 2) {
                    return getListOfStringsMatchingLastWord(args, COMMANDS.keySet());
                } else if (COMMANDS.get(args[1]) != null) {
                    return COMMANDS.get(args[1]).getTabCompletions(server, sender, args2, targetPos);
                }
            }
        }
        return Collections.emptyList();
    }
}
