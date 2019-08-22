package the_fireplace.clans.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;
import the_fireplace.clans.Clans;
import the_fireplace.clans.abstraction.dummy.PaymentHandlerDummy;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.details.*;
import the_fireplace.clans.commands.finance.CommandAddFunds;
import the_fireplace.clans.commands.finance.CommandBalance;
import the_fireplace.clans.commands.finance.CommandSetRent;
import the_fireplace.clans.commands.finance.CommandTakeFunds;
import the_fireplace.clans.commands.land.*;
import the_fireplace.clans.commands.members.*;
import the_fireplace.clans.commands.teleportation.CommandHome;
import the_fireplace.clans.commands.teleportation.CommandSetHome;
import the_fireplace.clans.commands.teleportation.CommandTrapped;
import the_fireplace.clans.data.PlayerData;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.util.PermissionManager;
import the_fireplace.clans.util.translation.TranslationUtil;

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
        put("autoclaim", new CommandAutoClaim());
        put("autoabandon", new CommandAutoAbandon());
	    put("map", new CommandMap());
        put("fancymap", new CommandFancyMap());
        put("seechunk", new CommandSeeChunk());
        //managing members
        put("invite", new CommandInvite());
        put("kick", new CommandKick());
        put("accept", new CommandAccept());
        put("decline", new CommandDecline());
        put("leave", new CommandLeave());
        put("promote", new CommandPromote());
        put("demote", new CommandDemote());
        //clan constants/details/other
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
        put("clanchat", new CommandClanChat());
        //teleportation related
        put("home", new CommandHome());
        put("trapped", new CommandTrapped());
        //clan finances
        if(!(Clans.getPaymentHandler() instanceof PaymentHandlerDummy)){
            put("balance", new CommandBalance());
            put("addfunds", new CommandAddFunds());
            put("takefunds", new CommandTakeFunds());
            put("setrent", new CommandSetRent());
        }
        //help
        put("help", new CommandClanHelp());
	}};

    public static final Map<String, String> aliases = Maps.newHashMap();
    private static final List<String> financeCommands = Lists.newArrayList("balance", "addfunds", "takefunds", "setrent", "finances");

    static {
        aliases.put("c", "claim");
        aliases.put("cc", "clanchat");
        aliases.put("chat", "clanchat");
        aliases.put("abc", "abandonclaim");
        aliases.put("unclaim", "abandonclaim");
        aliases.put("uc", "abandonclaim");
        aliases.put("ac", "autoclaim");
        aliases.put("aa", "autoabandon");
        aliases.put("m", "map");
        aliases.put("fm", "fancymap");
        aliases.put("sc", "seechunk");
        aliases.put("i", "invite");
        aliases.put("inv", "invite");
        aliases.put("create", "form");
        aliases.put("b", "banner");
        aliases.put("clan", "details");
        aliases.put("info", "details");
        aliases.put("d", "details");
        aliases.put("setdesc", "setdescription");
        aliases.put("pi", "playerinfo");
        aliases.put("player", "playerinfo");
        aliases.put("setcolour", "setcolor");
        aliases.put("h", "home");
        aliases.put("t", "trapped");
        aliases.put("unstuck", "trapped");
        aliases.put("deposit", "addfunds");
        aliases.put("af", "addfunds");
        aliases.put("withdraw", "takefunds");
    }

    public static String processAlias(String subCommand) {
        return aliases.getOrDefault(subCommand, subCommand);
    }

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
        String tag = args[0].toLowerCase();
        //Remove the subcommand from the args
        if(ClanCache.clanNameTaken(tag) && !ClanCache.forbiddenClanNames.contains(tag) && args.length >= 2) {
            //Skip to the next arg because the first is a clan name
            tag = args[1];
            if (args.length > 2) {
                String[] commArgs = Arrays.copyOfRange(args, 2, args.length);
                //If the command is greedy, we leave the subcommand in the args so it can be identified as greedy when the subcommand handler executes.
                args = ArrayUtils.addAll(new String[]{args[0]}, greedyCommands.contains(tag) ? ArrayUtils.addAll(new String[]{tag}, commArgs) : commArgs);
            } else
                args = new String[]{args[0]};
        } else {
            if (args.length > 1)
                args = Arrays.copyOfRange(args, 1, args.length);
            else
                args = new String[]{};
            //Make the first arg the default clan name because a clan name was not specified
            Clan defaultClan = sender instanceof EntityPlayerMP ? ClanCache.getClanById(PlayerData.getDefaultClan(((EntityPlayerMP) sender).getUniqueID())) : null;
            args = ArrayUtils.addAll(new String[]{defaultClan != null ? defaultClan.getClanName() : "null"}, args);
        }
        //Check permissions and run command
        if(!PermissionManager.permissionManagementExists() || PermissionManager.hasPermission(sender, PermissionManager.CLAN_COMMAND_PREFIX+processAlias(tag))) {
            if (!(Clans.getPaymentHandler() instanceof PaymentHandlerDummy) || !financeCommands.contains(processAlias(tag))) {
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
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> getAliases() {
        return Lists.newArrayList("c");
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        String[] args2;
        if(args.length > 1)
            args2 = Arrays.copyOfRange(args, 1, args.length);
        else
            args2 = new String[]{};
        return args.length >= 1 ? args.length == 1 ? Lists.newArrayList(commands.keySet()) : commands.get(args[0]) != null ? commands.get(args[0]).getTabCompletions(server, sender, args2, targetPos) : Collections.emptyList() : Collections.emptyList();
    }
}
