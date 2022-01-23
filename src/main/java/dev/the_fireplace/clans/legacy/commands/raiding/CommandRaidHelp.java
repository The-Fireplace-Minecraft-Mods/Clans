package dev.the_fireplace.clans.legacy.commands.raiding;

import com.google.common.collect.Lists;
import dev.the_fireplace.clans.legacy.commands.ClanSubCommand;
import dev.the_fireplace.clans.legacy.commands.CommandRaid;
import dev.the_fireplace.clans.legacy.commands.RaidSubCommand;
import dev.the_fireplace.clans.legacy.util.ChatUtil;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandRaidHelp extends RaidSubCommand
{
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void runFromAnywhere(@Nullable MinecraftServer server, ICommandSender sender, @Nullable String[] args) throws CommandException {
        if (args == null || args.length == 0 || args[0].matches("\\d+")) {
            int page = args == null || args.length < 1 ? 1 : parseInt(args[0]);
            List<ITextComponent> helps = Lists.newArrayList();
            for (Map.Entry<String, ClanSubCommand> command : CommandRaid.COMMANDS.entrySet()) {
                helps.add(TranslationUtil.getTranslation(sender, "commands.clan.common.help_format",
                    TranslationUtil.getStringTranslation(sender, "commands.raid." + command.getKey() + ".usage"),
                    TranslationUtil.getStringTranslation(sender, "commands.raid." + command.getKey() + ".description")));
            }
            helps.sort(Comparator.comparing(ITextComponent::getUnformattedText));

            ChatUtil.showPaginatedChat(sender, "/raid help %s", helps, page);
        } else if (CommandRaid.aliases.containsKey(args[0]) || CommandRaid.COMMANDS.containsKey(args[0])) {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.help_format",
                TranslationUtil.getStringTranslation(sender, "commands.raid." + CommandRaid.processAlias(args[0]) + ".usage"),
                TranslationUtil.getStringTranslation(sender, "commands.raid." + CommandRaid.processAlias(args[0]) + ".description")));
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.invalid_help", args[0]));
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public int getMinArgs() {
        return 0;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    protected boolean allowConsoleUsage() {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> comp = Lists.newArrayList();
        if (args.length != 1) {
            return comp;
        }
        for (int i = 1; i < CommandRaid.COMMANDS.size() / ChatUtil.RESULTS_PER_PAGE; i++) {
            comp.add(String.valueOf(i));
        }
        comp.addAll(CommandRaid.aliases.keySet());
        comp.addAll(CommandRaid.COMMANDS.keySet());
        return getListOfStringsMatchingLastWord(args, comp);
    }
}
