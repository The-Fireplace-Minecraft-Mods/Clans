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
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.details.*;
import the_fireplace.clans.commands.finance.*;
import the_fireplace.clans.commands.land.CommandAbandonClaim;
import the_fireplace.clans.commands.land.CommandClaim;
import the_fireplace.clans.commands.land.CommandFancyMap;
import the_fireplace.clans.commands.land.CommandMap;
import the_fireplace.clans.commands.members.*;
import the_fireplace.clans.commands.teleportation.CommandHome;
import the_fireplace.clans.commands.teleportation.CommandSetHome;
import the_fireplace.clans.commands.teleportation.CommandTrapped;
import the_fireplace.clans.compat.payment.PaymentHandlerDummy;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandClan extends CommandBase {
    public static final HashMap<String, ClanSubCommand> commands = new HashMap<String, ClanSubCommand>() {{
        //land claiming
        put("claim", new CommandClaim());
        put("abandonclaim", new CommandAbandonClaim());
	    put("map", new CommandMap());
        put("fancymap", new CommandFancyMap());
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
        put("setbanner", new CommandSetBanner());
        put("banner", new CommandBanner());
        put("setname", new CommandSetName());
        put("details", new CommandDetails());
        put("setdescription", new CommandSetDescription());
        put("setdefault", new CommandSetDefault());
        put("playerinfo", new CommandPlayerInfo());
        put("setcolor", new CommandSetColor());
        put("list", new CommandList());
        //teleportation related
        put("home", new CommandHome());
        put("trapped", new CommandTrapped());
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
        return TranslationUtil.getRawTranslationString(sender, "commands.clan.usage");
    }

    public static final ArrayList<String> greedyCommands = Lists.newArrayList("setdesc", "setdescription");

    @Override
    public void execute(@Nullable MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length <= 0)
            throw new WrongUsageException(getUsage(sender));
        String tag = args[0];
        if(ClanCache.clanNameTaken(tag) && !ClanCache.forbiddenClanNames.contains(tag) && args.length >= 2) {
            tag = args[1];
            if (args.length > 2) {
                String[] commArgs = Arrays.copyOfRange(args, 2, args.length);
                args = ArrayUtils.addAll(new String[]{args[0]}, greedyCommands.contains(tag) ? ArrayUtils.addAll(new String[]{tag}, commArgs) : commArgs);
            } else
                args = new String[]{args[0]};
        } else
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
            case "map":
            case "m":
                commands.get("map").execute(server, sender, args);
                return;
            case "fancymap":
            case "fm":
                commands.get("fancymap").execute(server, sender, args);
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
            case "create":
                commands.get("form").execute(server, sender, args);
                return;
	        case "disband":
		        commands.get("disband").execute(server, sender, args);
		        return;
            case "sethome":
                commands.get("sethome").execute(server, sender, args);
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
            case "setdefault":
                commands.get("setdefault").execute(server, sender, args);
                return;
            case "playerinfo":
            case "pi":
                commands.get("playerinfo").execute(server, sender, args);
                return;
            case "setcolor":
            case "setcolour":
                commands.get("setcolor").execute(server, sender, args);
                return;
            case "list":
                commands.get("list").execute(server, sender, args);
                return;
            //Teleportation related
            case "home":
            case "h":
                if(Clans.cfg.clanHomeWarmupTime > -1) {
                    commands.get("home").execute(server, sender, args);
                    return;
                } else
                    throw new CommandException(TranslationUtil.getRawTranslationString(sender, "commands.clan.home.disabled"));
            case "trapped":
            case "t":
                commands.get("trapped").execute(server, sender, args);
                return;
            //Help command
            case "help":
                StringBuilder commandsHelp = new StringBuilder(TranslationUtil.getStringTranslation(sender, "commands.clan.help")+"\nhelp");
                buildHelpCommand(sender, commandsHelp, commands);
                sender.sendMessage(new TextComponentString(commandsHelp.toString()).setStyle(TextStyles.YELLOW));
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
                        throw new CommandException(TranslationUtil.getRawTranslationString(sender, "commands.clan.takefunds.disabled"));
                    return;
                case "setrent":
                    if(Clans.cfg.chargeRentDays > 0)
                        commands.get("setrent").execute(server, sender, args);
                    else
                        throw new CommandException(TranslationUtil.getRawTranslationString(sender, "commands.clan.setrent.disabled"));
                    return;
                case "finances":
                    if(Clans.cfg.clanUpkeepDays > 0 || Clans.cfg.chargeRentDays > 0)
                        commands.get("finances").execute(server, sender, args);
                    else
                        throw new CommandException(TranslationUtil.getRawTranslationString(sender, "commands.clan.finances.disabled"));
                    return;
            }
        }
        throw new WrongUsageException(getUsage(sender));
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    private static final ArrayList<String> aliases = Lists.newArrayList();
    static {
        aliases.add("c");
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender instanceof EntityPlayer;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        String[] args2;
        if(args.length > 1)
            args2 = Arrays.copyOfRange(args, 1, args.length);
        else
            args2 = new String[]{};
        return args.length >= 1 ? args.length == 1 ? Lists.newArrayList(commands.keySet()) : commands.get(args[0]) != null ? commands.get(args[0]).getTabCompletions(server, sender, args2, targetPos) : Collections.emptyList() : Collections.emptyList();
    }

    static void buildHelpCommand(ICommandSender sender, StringBuilder commandsHelp, HashMap<String, ClanSubCommand> commands) {
        if(sender instanceof EntityPlayer) {
            ArrayList<EnumRank> playerRanks = Lists.newArrayList();
            for(Clan c: ClanCache.getPlayerClans(((EntityPlayer) sender).getUniqueID()))
                playerRanks.add(ClanCache.getPlayerRank(((EntityPlayer) sender).getUniqueID(), c));
            //Only append commands the player can use.
            for (String command : commands.keySet()) {
                if(commands.get(command) == null)
                    continue;
                EnumRank commandRank = commands.get(command).getRequiredClanRank();
                if((commandRank == EnumRank.NOCLAN && !playerRanks.isEmpty())
                        || (commandRank != EnumRank.NOCLAN && commandRank != EnumRank.ANY && playerRanks.isEmpty())
                        || (commandRank == EnumRank.ADMIN && !playerRanks.contains(EnumRank.ADMIN) && !playerRanks.contains(EnumRank.LEADER))
                        || (commandRank == EnumRank.LEADER && !playerRanks.contains(EnumRank.LEADER)))
                    continue;
                commandsHelp.append("\n").append(command);
            }
        }
    }
}
