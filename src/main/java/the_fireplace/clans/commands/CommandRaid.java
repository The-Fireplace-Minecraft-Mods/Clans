package the_fireplace.clans.commands;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.MinecraftColors;
import the_fireplace.clans.commands.raiding.*;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.HashMap;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandRaid extends CommandBase {
    private static final HashMap<String, ClanSubCommand> commands = new HashMap<String, ClanSubCommand>() {{
        //raiding parties
        put("create", new CommandCreateRaid());
        put("join", new CommandJoinRaid());
        put("leave", new CommandLeaveRaid());
        put("invite", new CommandInviteRaid());
	    put("start", new CommandStartRaid());
	}};

    @Override
    public String getName() {
        return "raid";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/raid <command> [parameters]";
    }

    @Override
    public void execute(@Nullable MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length <= 0)
            throw new WrongUsageException("/raid <command> [parameters]");
        String tag = args[0];
        if(args.length > 1)
            args = Arrays.copyOfRange(args, 1, args.length);
        else
            args = new String[]{};
        switch(tag){
            //Commands for raiding parties
            case "create":
            case "form":
            case "c":
                commands.get("create").execute(server, sender, args);
                return;
            case "join":
            case "j":
                commands.get("join").execute(server, sender, args);
                return;
            case "leave":
            case "l":
                commands.get("leave").execute(server, sender, args);
                return;
            case "invite":
            case "i":
                commands.get("invite").execute(server, sender, args);
                return;
	        case "start":
		        commands.get("start").execute(server, sender, args);
		        return;
            //Help command
            case "help":
                StringBuilder commandsHelp = new StringBuilder(MinecraftColors.YELLOW+"/raid commands:\n" +
                        "help");
                CommandClan.buildHelpCommand(sender, commandsHelp, commands);
                sender.sendMessage(new TextComponentString(commandsHelp.toString()));
                return;
        }
        throw new WrongUsageException("/raid <command> [parameters]");
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
}
