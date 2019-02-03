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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.HashMap;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandRaid extends CommandBase {
    private static final HashMap<String, ClanSubCommand> commands = new HashMap<String, ClanSubCommand>() {{
        //raiding parties
        put("makeparty", null);
        put("joinparty", null);
        put("inviteparty", null);
        put("disbandparty", null);
	    put("raid", null);
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
            case "makeparty":
            case "mp":
                commands.get("makeparty").execute(server, sender, args);
                return;
            case "joinparty":
            case "jp":
                commands.get("joinparty").execute(server, sender, args);
                return;
            case "inviteparty":
            case "ip":
                commands.get("inviteparty").execute(server, sender, args);
                return;
	        case "disbandparty":
		        commands.get("disbandparty").execute(server, sender, args);
		        return;
	        case "raid":
		        commands.get("raid").execute(server, sender, args);
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
