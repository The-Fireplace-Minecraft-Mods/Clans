package the_fireplace.clans.commands;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.commands.raiding.*;
import the_fireplace.clans.util.MinecraftColors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandRaid extends CommandBase {
    private static final HashMap<String, ClanSubCommand> commands = new HashMap<String, ClanSubCommand>() {{
        //raiding parties
        put("form", new CommandCreateRaid());
        put("join", new CommandJoinRaid());
        put("leave", new CommandLeaveRaid());
        put("invite", new CommandInviteRaid());
	    put("start", new CommandStartRaid());
        put("collect", new CommandCollect());
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
            case "f":
                commands.get("form").execute(server, sender, args);
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
            case "collect":
            case "c":
                commands.get("collect").execute(server, sender, args);
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
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender instanceof EntityPlayer;
    }

    private static final ArrayList<String> aliases = Lists.newArrayList();
    static {
        aliases.add("r");
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

    @Override
    public List<String> getAliases() {
        return aliases;
    }
}
