package the_fireplace.clans.commands;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.Clans;
import the_fireplace.clans.util.MinecraftColors;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.details.*;
import the_fireplace.clans.commands.finance.*;
import the_fireplace.clans.commands.land.CommandAbandonClaim;
import the_fireplace.clans.commands.land.CommandClaim;
import the_fireplace.clans.commands.members.*;
import the_fireplace.clans.payment.PaymentHandlerDummy;

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
        put("abandonclaim", new CommandAbandonClaim());
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
        put("banner", new CommandBanner());
        put("setname", new CommandSetName());
        put("details", new CommandDetails());
        put("setdescription", new CommandSetDescription());
        //clan finances
        if(!(Clans.getPaymentHandler() instanceof PaymentHandlerDummy)){
            put("balance", new CommandBalance());
            put("addfunds", new CommandAddFunds());
            if(Clans.cfg.leaderWithdrawFunds)
                put("takefunds", new CommandTakeFunds());
            if(Clans.cfg.chargeRentDays > 0)
                put("setrent", new CommandSetRent());
            if(Clans.cfg.clanUpkeepDays > 0 || Clans.cfg.chargeRentDays > 0)
                put("finances", new CommandFinances());
        }
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
            case "details":
            case "info":
            case "d":
                commands.get("details").execute(server, sender, args);
                return;
            case "setdescription":
            case "setdesc":
                commands.get("setdescription").execute(server, sender, args);
                return;
            //Help command
            case "help":
                StringBuilder commandsHelp = new StringBuilder(MinecraftColors.YELLOW+"/clan commands:\n" +
                        "help");
                buildHelpCommand(sender, commandsHelp, commands);
                sender.sendMessage(new TextComponentString(commandsHelp.toString()));
                return;
        }
        //Payment commands
        if(!(Clans.getPaymentHandler() instanceof PaymentHandlerDummy)){
            switch(tag){
                case "balance":
                    commands.get("balance").execute(server, sender, args);
                    return;
                case "addfunds":
                case "deposit":
                case "af":
                    commands.get("addfunds").execute(server, sender, args);
                    return;
                case "takefunds":
                case "withdraw":
                    if(Clans.cfg.leaderWithdrawFunds)
                        commands.get("takefunds").execute(server, sender, args);
                    else
                        throw new CommandException("/clan takefunds is disabled on this server.");
                    return;
                case "setrent":
                    if(Clans.cfg.chargeRentDays > 0)
                        commands.get("setrent").execute(server, sender, args);
                    else
                        throw new CommandException("/clan setrent is disabled on this server.");
                    return;
                case "finances":
                    if(Clans.cfg.clanUpkeepDays > 0 || Clans.cfg.chargeRentDays > 0)
                        commands.get("finances").execute(server, sender, args);
                    else
                        throw new CommandException("/clan finances is disabled on this server.");
                    return;
            }
        }
        throw new WrongUsageException("/clan <command> [parameters]");
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    static void buildHelpCommand(ICommandSender sender, StringBuilder commandsHelp, HashMap<String, ClanSubCommand> commands) {
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
    }
}
