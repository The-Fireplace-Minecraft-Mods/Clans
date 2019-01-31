package the_fireplace.clans.commands;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.HashMap;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandClan extends CommandBase {
    private static final HashMap<String, CommandBase> commands = new HashMap<String, CommandBase>() {{
        put("claim", null);
    }};

    @Override
    public String getName() {
        return "clan";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/clan <command> [parameters]";
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void execute(@Nullable MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length <= 0)
            throw new WrongUsageException("/clan <command> [parameters]");
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
            case "unclaim":
            case "uc":
                commands.get("unclaim").execute(server, sender, args);
                return;
            //Adding and Removing members
            case "invite":
            case "i":
                commands.get("invite").execute(server, sender, args);
                return;
            case "kick":
                commands.get("kick").execute(server, sender, args);
                return;
            case "accept":
                commands.get("accept").execute(server, sender, args);
                return;
            case "decline":
                commands.get("decline").execute(server, sender, args);
                return;
            case "leave":
                commands.get("leave").execute(server, sender, args);
                return;
            //Setting and accessing clan constants: home and banner
            case "sethome":
                commands.get("sethome").execute(server, sender, args);
                return;
            case "home":
            case "h":
                commands.get("home").execute(server, sender, args);
                return;
            case "banner":
            case "b":
                commands.get("banner").execute(server, sender, args);
                return;
            case "setbanner":
                commands.get("setbanner").execute(server, sender, args);
                return;
            //Commands for wars and alliances
            case "war":
                commands.get("war").execute(server, sender, args);
                return;
            case "ally":
                commands.get("ally").execute(server, sender, args);
                return;
            case "removeally":
                commands.get("removeally").execute(server, sender, args);
                return;
            //Help command
            case "help":
                StringBuilder commandsHelp = new StringBuilder("/clan commands:\n" +
                        "help");
                for(String command: commands.keySet())
                    commandsHelp.append("\n").append(command);
                sender.sendMessage(new TextComponentString(commandsHelp.toString()));
                return;
        }
        throw new WrongUsageException("/clan <command> [parameters]");
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
}
