package the_fireplace.clans.legacy.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.clan.membership.PlayerClans;
import the_fireplace.clans.clan.metadata.ClanNames;
import the_fireplace.clans.economy.DummyEconomy;
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
public class CommandClan extends CommandBase {
    public static final HashMap<String, ClanSubCommand> commands = new HashMap<String, ClanSubCommand>() {{
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
        if(ClansModContainer.getConfig().shouldProtectWilderness())
            put("undergroundmessages", new CommandUndergroundMessages());
        //teleportation related
        put("home", new CommandHome());
        put("trapped", new CommandTrapped());
        //clan finances
        if(!(Economy instanceof DummyEconomy)){
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

    public static final Map<String, String> aliases = Maps.newHashMap();
    private static final List<String> financeCommands = Lists.newArrayList("balance", "addfunds", "takefunds", "setrent", "finances");

    static {
        //land
        aliases.put("c", "claim");
        aliases.put("cc", "clanchat");
        aliases.put("chat", "clanchat");
        aliases.put("abc", "abandonclaim");
        aliases.put("unclaim", "abandonclaim");
        aliases.put("uc", "abandonclaim");
        aliases.put("ac", "autoclaim");
        aliases.put("aa", "autoabandon");
        aliases.put("m", "map");
        aliases.put("fm", "fancymap");
        aliases.put("sc", "seechunk");
        //members
        aliases.put("ad", "autodecline");
        aliases.put("block", "autodecline");
        aliases.put("i", "invite");
        aliases.put("inv", "invite");
        aliases.put("join", "accept");
        //clan constants/details/other
        aliases.put("create", "form");
        aliases.put("b", "banner");
        aliases.put("clan", "details");
        aliases.put("info", "details");
        aliases.put("d", "details");
        aliases.put("pi", "playerinfo");
        aliases.put("player", "playerinfo");
        //clan config
        aliases.put("setcolour", "setcolor");
        aliases.put("setdesc", "setdescription");
        //player config
        aliases.put("tmm", "territorymessagemode");
        if(ClansModContainer.getConfig().shouldProtectWilderness())
            aliases.put("um", "undergroundmessages");
        //teleportation related
        aliases.put("h", "home");
        aliases.put("t", "trapped");
        aliases.put("unstuck", "trapped");
        //finances
        aliases.put("deposit", "addfunds");
        aliases.put("af", "addfunds");
        aliases.put("withdraw", "takefunds");
        //permissions
        aliases.put("perms", "permissions");
        aliases.put("setperm", "set");
        //locks
        aliases.put("l", "lock");
        aliases.put("lc", "lockchunk");
        aliases.put("ul", "unlock");
        aliases.put("ulc", "unlockchunk");
        aliases.put("ga", "grantaccess");
        aliases.put("da", "denyaccess");
        aliases.put("li", "lockinfo");
    }

    public static String processAlias(String subCommand) {
        return aliases.getOrDefault(subCommand, subCommand);
    }

    @Override
    public String getName() {
        return "clan";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return TranslationUtil.getRawTranslationString(sender, "commands.clan.usage");
    }

    public static final ArrayList<String> greedyCommands = Lists.newArrayList("setdesc", "setdescription");

    @Override
    public void execute(@Nullable MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length <= 0)
            throw new WrongUsageException(getUsage(sender));
        String tag = args[0].toLowerCase();
        //Remove the subcommand from the args
        if(ClanNames.isClanNameUsed(tag) && args.length >= 2) {
            //Skip to the next arg because the first is a clan name
            tag = args[1];
            if (args.length > 2) {
                String[] commArgs = Arrays.copyOfRange(args, 2, args.length);
                //If the command is greedy, we leave the subcommand in the args so it can be identified as greedy when the subcommand handler executes.
                args = ArrayUtils.addAll(new String[]{args[0]}, greedyCommands.contains(tag) ? ArrayUtils.addAll(new String[]{tag}, commArgs) : commArgs);
            } else
                args = new String[]{args[0]};
        } else {
            //Skip greedy commands because this would cause part of the description to be cut off
            if(!greedyCommands.contains(tag))
                if (args.length > 1)
                    args = Arrays.copyOfRange(args, 1, args.length);
                else
                    args = new String[]{};
            //Make the first arg the default clan name because a clan name was not specified
            Clan defaultClan = sender instanceof EntityPlayerMP ? ClanDatabase.getClanById(PlayerClanSettings.getDefaultClan(((EntityPlayerMP) sender).getUniqueID())) : null;
            args = ArrayUtils.addAll(new String[]{defaultClan != null ? defaultClan.getClanMetadata().getClanName() : "null"}, args);
        }
        //Check permissions and run command
        if(!PermissionManager.permissionManagementExists() || PermissionManager.hasPermission(sender, PermissionManager.CLAN_COMMAND_PREFIX+processAlias(tag))) {
            if (!(Economy instanceof DummyEconomy) || !financeCommands.contains(processAlias(tag))) {
                if(commands.containsKey(processAlias(tag)))
                    commands.get(processAlias(tag)).execute(server, sender, args);
                else
                    throw new WrongUsageException(getUsage(sender));
                return;
            }
        } else if(commands.containsKey(tag) || aliases.containsKey(tag))
            throw new CommandException("commands.generic.permission");
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
        if(args.length > 1)
            args2 = Arrays.copyOfRange(args, 1, args.length);
        else
            args2 = new String[]{};
        if(args.length >= 1) {
            if(args.length == 1) {
                List<String> arg1List = Lists.newArrayList(commands.keySet());
                if (sender instanceof EntityPlayerMP)
                    for (Clan c : PlayerClans.getClansPlayerIsIn(((EntityPlayerMP) sender).getUniqueID()))
                        arg1List.add(c.getClanMetadata().getClanName());
                return getListOfStringsMatchingLastWord(args, arg1List);
            } else if(commands.containsKey(processAlias(args[0]))) {
                return commands.get(processAlias(args[0])).getTabCompletions(server, sender, args2, targetPos);
            } else if(ClanNames.getClanNames().contains(args[0])) {
                if(args.length == 2)
                    return getListOfStringsMatchingLastWord(args, commands.keySet());
                else if(commands.get(args[1]) != null)
                    return commands.get(args[1]).getTabCompletions(server, sender, args2, targetPos);
            }
        }
        return Collections.emptyList();
    }
}
