package the_fireplace.clans.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.Clans;
import the_fireplace.clans.commands.raiding.*;
import the_fireplace.clans.util.PermissionManager;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandRaid extends CommandBase {
    public static final HashMap<String, ClanSubCommand> commands = new HashMap<String, ClanSubCommand>() {{
        //raiding parties
        put("join", new CommandJoinRaid());
        put("leave", new CommandLeaveRaid());
        put("invite", new CommandInviteRaid());
	    put("start", new CommandStartRaid());
        put("collect", new CommandCollect());
        //help
        put("help", new CommandRaidHelp());
	}};

    public static final Map<String, String> aliases = Maps.newHashMap();

    static {
        aliases.put("j", "join");
        aliases.put("l", "leave");
        aliases.put("i", "invite");
        aliases.put("c", "collect");
    }

    public static String processAlias(String subCommand) {
        return aliases.getOrDefault(subCommand, subCommand);
    }

    @Override
    public String getName() {
        return "raid";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return TranslationUtil.getRawTranslationString(sender, "commands.raid.usage");
    }

    @Override
    public void execute(@Nullable MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length <= 0)
            throw new WrongUsageException(getUsage(sender));
        String tag = args[0].toLowerCase();
        if(Clans.getConfig().getMaxRaidDuration() <= 0 && !"collect".equals(processAlias(tag)))
            throw new CommandException(TranslationUtil.getRawTranslationString(sender, "commands.raid.disabled"));
        if(!PermissionManager.permissionManagementExists() || PermissionManager.hasPermission(sender, PermissionManager.RAID_COMMAND_PREFIX+processAlias(tag))) {
            if(commands.containsKey(processAlias(tag)))
                commands.get(processAlias(tag)).execute(server, sender, args);
            else
                throw new WrongUsageException(getUsage(sender));
        } else if(commands.containsKey(tag) || aliases.containsKey(tag))
            throw new CommandException("commands.generic.permission");
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
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
        return Lists.newArrayList("r");
    }
}
