package dev.the_fireplace.clans.legacy.commands;

import com.google.common.collect.Lists;
import dev.the_fireplace.clans.economy.Economy;
import dev.the_fireplace.clans.legacy.commands.op.OpCommandHelp;
import dev.the_fireplace.clans.legacy.commands.op.land.*;
import dev.the_fireplace.clans.legacy.commands.op.management.*;
import dev.the_fireplace.clans.legacy.commands.op.teleportation.OpCommandTeleport;
import dev.the_fireplace.clans.legacy.util.PermissionManager;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.legacy.commands.op.land.*;
import the_fireplace.clans.legacy.commands.op.management.*;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandOpClan extends CommandBase
{
    public static final Map<String, ClanSubCommand> COMMANDS = new HashMap<String, ClanSubCommand>()
    {{
        //land claiming
        put("claim", new OpCommandClaim());
        put("abandonclaim", new OpCommandAbandonClaim());
        put("autoclaim", new OpCommandAutoClaim());
        put("autoabandon", new OpCommandAutoAbandon());
        //Adjust other clans
        put("setname", new OpCommandSetName());
        put("setdescription", new OpCommandSetDescription());
        put("setcolor", new OpCommandSetColor());
        put("setshield", new OpCommandSetShield());
        if (Economy.isPresent()) {
            put("addfunds", new OpCommandAddFunds());
        }
        put("setrank", new OpCommandSetRank());
        put("kick", new OpCommandKick());
        put("disband", new OpCommandDisband());
        put("setserver", new OpCommandSetServer());
        put("setoption", new OpCommandSetOption());
        //Op tools
        put("buildadmin", new OpCommandBuildAdmin());
        put("tp", new OpCommandTeleport());
        //help
        put("help", new OpCommandHelp());
    }};

    public static final Map<String, String> COMMAND_ALIASES = new HashMap<String, String>()
    {{
        put("c", "claim");
        put("ac", "autoclaim");
        put("abc", "abandonclaim");
        put("unclaim", "abandonclaim");
        put("uc", "abandonclaim");
        put("aa", "autoabandon");
        put("shield", "setshield");
        put("setdesc", "setdescription");
        put("ba", "buildadmin");
        put("admin", "buildadmin");
        put("setcolour", "setcolor");
        put("deposit", "addfunds");
        put("af", "addfunds");
        put("set", "setoption");
        put("so", "setoption");
        put("teleport", "tp");
    }};

    public static String processAlias(String subCommand) {
        return COMMAND_ALIASES.getOrDefault(subCommand, subCommand);
    }

    @Override
    public String getName() {
        return "opclan";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return TranslationUtil.getRawTranslationString(sender, "commands.opclan.usage");
    }

    @Override
    public void execute(@Nullable MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length <= 0) {
            throw new WrongUsageException(getUsage(sender));
        }
        String tag = args[0].toLowerCase();
        //Attach an extra arg to greedy commands because ClanSubCommand takes two args off of those.
        if (args.length > 1 && CommandClan.GREEDY_COMMANDS.contains(tag)) {
            args = ArrayUtils.addAll(new String[]{"opclan"}, args);
        }
        if (PermissionManager.hasPermission(sender, PermissionManager.OPCLAN_COMMAND_PREFIX + processAlias(tag), true)) {
            if (Economy.isPresent() || !"addfunds".equals(processAlias(tag))) {
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
    public List<String> getAliases() {
        return Lists.newArrayList("oc");
    }

    @SuppressWarnings("Duplicates")
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        String[] args2;
        if (args.length > 1) {
            args2 = Arrays.copyOfRange(args, 1, args.length);
        } else {
            args2 = new String[]{};
        }
        return args.length >= 1 && COMMANDS.containsKey(processAlias(args[0])) ? args.length == 1 ? getListOfStringsMatchingLastWord(args, COMMANDS.keySet()) : COMMANDS.get(processAlias(args[0])).getTabCompletions(server, sender, args2, targetPos) : Collections.emptyList();
    }
}
