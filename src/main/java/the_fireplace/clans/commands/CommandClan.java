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
public class CommandClan extends CommandBase {
    private static final HashMap<String, ClanSubCommand> commands = new HashMap<String, ClanSubCommand>() {{
        //land claiming
        put("claim", new CommandClaim());
        put("abandonclaim", new CommandAbandonclaim());
	    put("map", null);
        //managing members
        put("invite", new CommandInvite());
        put("kick", new CommandKick());
        put("accept", new CommandAccept());
        put("decline", new CommandDecline());
        put("leave", new CommandLeave());
        put("promote", new CommandPromote());
        put("demote", new CommandDemote());
        //clan constants
        put("form", new CommandForm());
	    put("disband", new CommandDisband());
        put("sethome", new CommandSetHome());
        put("home", new CommandHome());
        put("setbanner", new CommandSetBanner());
        put("banner", null);
        put("setname", new CommandSetName());
        put("details", null);
        put("settagline", null);
        //raiding parties
        put("makeparty", null);
        put("joinparty", null);
        put("inviteparty", null);
        put("disbandparty", null);
	    put("raid", null);
	}};

    @Override
    public String getName() {
        return "clan";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/clan <command> [parameters]";
    }

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
            case "abandonclaim":
            case "ac":
                commands.get("abandonclaim").execute(server, sender, args);
                return;
            //Managing members
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
            case "promote":
                commands.get("promote").execute(server, sender, args);
                return;
            case "demote":
                commands.get("demote").execute(server, sender, args);
                return;
            //Setting clan details and home
            case "form":
                commands.get("form").execute(server, sender, args);
                return;
	        case "disband":
		        commands.get("disband").execute(server, sender, args);
		        return;
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
            case "setname":
                commands.get("setname").execute(server, sender, args);
                return;
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
                StringBuilder commandsHelp = new StringBuilder(MinecraftColors.YELLOW+"/clan commands:\n" +
                        "help");
                if(sender instanceof EntityPlayer) {
	                EnumRank playerRank = ClanCache.getPlayerRank(((EntityPlayer) sender).getUniqueID());
	                //Only append commands the player can use.
	                for (String command : commands.keySet()) {
	                    if(commands.get(command) == null)
	                        continue;
	                	EnumRank commandRank = commands.get(command).getRequiredClanRank();
	                	if((commandRank == EnumRank.NOCLAN && playerRank != EnumRank.NOCLAN)
			                || (commandRank != EnumRank.NOCLAN && commandRank != EnumRank.ANY && playerRank == EnumRank.NOCLAN)
			                || ((commandRank == EnumRank.ADMIN || commandRank == EnumRank.LEADER) && playerRank == EnumRank.MEMBER)
			                || (commandRank == EnumRank.LEADER && playerRank == EnumRank.ADMIN))
	                		continue;
		                commandsHelp.append("\n").append(command);
	                }
                }
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
