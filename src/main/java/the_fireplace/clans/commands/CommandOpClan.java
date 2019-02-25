package the_fireplace.clans.commands;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.commands.op.*;
import the_fireplace.clans.util.MinecraftColors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandOpClan extends CommandBase {
    private static final HashMap<String, ClanSubCommand> commands = new HashMap<String, ClanSubCommand>() {{
        //land claiming
        put("claim", new OpCommandClaim());
        put("abandonclaim", new OpCommandAbandomClaim());
        //clan constants
        put("setname", new OpCommandSetName());
        put("setdescription", new OpCommandSetDescription());
        //Adjust other clans
        put("setshield", new OpCommandSetShield());
        put("buildadmin", new OpCommandBuildAdmin());
	}};

    @Override
    public String getName() {
        return "opclan";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/opclan <command> [parameters]";
    }

    @Override
    public void execute(@Nullable MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length <= 0)
            throw new WrongUsageException("/opclan <command> [parameters]");
        String tag = args[0];
        if(args.length > 1)
            args = Arrays.copyOfRange(args, 1, args.length);
        else
            args = new String[]{};
        switch(tag){
            //Land Claiming
            case "claim":
            case "c":
                commands.get("claim").execute(server, sender, args);
                return;
            case "abandonclaim":
            case "ac":
                commands.get("abandonclaim").execute(server, sender, args);
                return;
            //Setting opclan details
            case "setname":
                commands.get("setname").execute(server, sender, args);
                return;
            case "setdescription":
            case "setdesc":
                commands.get("setdescription").execute(server, sender, args);
                return;
            //Adjust other clans
            case "setshield":
            case "shield":
                commands.get("setshield").execute(server, sender, args);
                return;
            case "buildadmin":
            case "ba":
                commands.get("buildadmin").execute(server, sender, args);
                return;
            //Help command
            case "help":
                StringBuilder commandsHelp = new StringBuilder(MinecraftColors.YELLOW+"/opclan commands:\n" +
                        "help");
                for (String command : commands.keySet()) {
                    if(commands.get(command) == null)
                        continue;
                    commandsHelp.append("\n").append(command);
                }
                sender.sendMessage(new TextComponentString(commandsHelp.toString()));
                return;
        }
        throw new WrongUsageException("/opclan <command> [parameters]");
    }

    private static final ArrayList<String> aliases = Lists.newArrayList();
    static {
        aliases.add("oc");
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        String[] args2;
        if(args.length > 1)
            args2 = Arrays.copyOfRange(args, 1, args.length);
        else
            args2 = new String[]{};
        return args.length >= 1 ? args.length == 1 ? Lists.newArrayList(commands.keySet()) : commands.get(args[0]).getTabCompletions(server, sender, args2, targetPos) : Collections.emptyList();
    }
}
