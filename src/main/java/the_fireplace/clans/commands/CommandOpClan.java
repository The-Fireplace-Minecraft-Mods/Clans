package the_fireplace.clans.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.Clans;
import the_fireplace.clans.abstraction.dummy.PaymentHandlerDummy;
import the_fireplace.clans.commands.op.OpCommandHelp;
import the_fireplace.clans.commands.op.land.*;
import the_fireplace.clans.commands.op.management.*;
import the_fireplace.clans.commands.op.teleportation.OpCommandTeleport;
import the_fireplace.clans.util.PermissionManager;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandOpClan extends CommandBase {
    public static final HashMap<String, ClanSubCommand> commands = new HashMap<String, ClanSubCommand>() {{
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
        if(!(Clans.getPaymentHandler() instanceof PaymentHandlerDummy))
            put("addfunds", new OpCommandAddFunds());
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

    public static final Map<String, String> aliases = Maps.newHashMap();

    static {
        aliases.put("c", "claim");
        aliases.put("ac", "autoclaim");
        aliases.put("abc", "abandonclaim");
        aliases.put("unclaim", "abandonclaim");
        aliases.put("uc", "abandonclaim");
        aliases.put("aa", "autoabandon");
        aliases.put("shield", "setshield");
        aliases.put("setdesc", "setdescription");
        aliases.put("ba", "buildadmin");
        aliases.put("admin", "buildadmin");
        aliases.put("setcolour", "setcolor");
        aliases.put("deposit", "addfunds");
        aliases.put("af", "addfunds");
        aliases.put("set", "setoption");
        aliases.put("so", "setoption");
        aliases.put("teleport", "tp");
    }

    public static String processAlias(String subCommand) {
        return aliases.getOrDefault(subCommand, subCommand);
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
        if(args.length <= 0)
            throw new WrongUsageException(getUsage(sender));
        String tag = args[0].toLowerCase();
        //Attach an extra arg to greedy commands because ClanSubCommand takes two args off of those.
        if(args.length > 1 && CommandClan.greedyCommands.contains(tag))
            args = ArrayUtils.addAll(new String[]{"opclan"}, args);
        if(!PermissionManager.permissionManagementExists() || PermissionManager.hasPermission(sender, PermissionManager.OPCLAN_COMMAND_PREFIX+processAlias(tag))) {
            if (!(Clans.getPaymentHandler() instanceof PaymentHandlerDummy) || !"addfunds".equals(processAlias(tag))) {
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
    public List<String> getAliases() {
        return Lists.newArrayList("oc");
    }

    @SuppressWarnings("Duplicates")
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        String[] args2;
        if(args.length > 1)
            args2 = Arrays.copyOfRange(args, 1, args.length);
        else
            args2 = new String[]{};
        return args.length >= 1 && commands.containsKey(processAlias(args[0])) ? args.length == 1 ? getListOfStringsMatchingLastWord(args, commands.keySet()) : commands.get(processAlias(args[0])).getTabCompletions(server, sender, args2, targetPos) : Collections.emptyList();
    }
}
