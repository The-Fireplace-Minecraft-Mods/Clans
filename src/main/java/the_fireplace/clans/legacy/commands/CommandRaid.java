package the_fireplace.clans.legacy.commands;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.commands.raiding.*;
import the_fireplace.clans.legacy.util.PermissionManager;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandRaid extends CommandBase
{
    public static final HashMap<String, ClanSubCommand> COMMANDS = new HashMap<String, ClanSubCommand>()
    {{
        //raiding parties
        put("join", new CommandJoinRaid());
        put("leave", new CommandLeaveRaid());
        put("invite", new CommandInviteRaid());
        put("start", new CommandStartRaid());
        put("collect", new CommandCollect());
        //Teleportation
        put("thru", new CommandThru());
        //help
        put("help", new CommandRaidHelp());
    }};

    public static final Map<String, String> aliases = new HashMap<String, String>()
    {{
        put("j", "join");
        put("form", "join");
        put("l", "leave");
        put("i", "invite");
        put("c", "collect");

        put("t", "thru");
    }};

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
        if (args.length <= 0) {
            throw new WrongUsageException(getUsage(sender));
        }
        String tag = args[0].toLowerCase();
        if (ClansModContainer.getConfig().getMaxRaidDuration() <= 0 && !"collect".equals(processAlias(tag))) {
            throw new CommandException(TranslationUtil.getRawTranslationString(sender, "commands.raid.disabled"));
        }
        if (PermissionManager.hasPermission(sender, PermissionManager.RAID_COMMAND_PREFIX + processAlias(tag), true)) {
            if (COMMANDS.containsKey(processAlias(tag))) {
                COMMANDS.get(processAlias(tag)).execute(server, sender, args);
            } else {
                throw new WrongUsageException(getUsage(sender));
            }
        } else if (COMMANDS.containsKey(tag) || aliases.containsKey(tag)) {
            throw new CommandException("commands.generic.permission");
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
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
        if (args.length > 1) {
            args2 = Arrays.copyOfRange(args, 1, args.length);
        } else {
            args2 = new String[]{};
        }
        return args.length >= 1 && COMMANDS.containsKey(processAlias(args[0])) ? args.length == 1 ? getListOfStringsMatchingLastWord(args, COMMANDS.keySet()) : COMMANDS.get(processAlias(args[0])).getTabCompletions(server, sender, args2, targetPos) : Collections.emptyList();
    }

    @Override
    public List<String> getAliases() {
        return Lists.newArrayList("r");
    }
}
