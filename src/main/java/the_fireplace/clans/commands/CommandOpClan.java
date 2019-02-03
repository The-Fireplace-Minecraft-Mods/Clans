package the_fireplace.clans.commands;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.MinecraftColors;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.details.*;
import the_fireplace.clans.commands.land.CommandAbandonclaim;
import the_fireplace.clans.commands.land.CommandClaim;
import the_fireplace.clans.commands.members.*;
import the_fireplace.clans.commands.op.OpCommandAbandomClaim;
import the_fireplace.clans.commands.op.OpCommandClaim;
import the_fireplace.clans.commands.op.OpCommandSetDescription;
import the_fireplace.clans.commands.op.OpCommandSetName;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.HashMap;

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
}
