package the_fireplace.clans.commands.details;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.commands.CommandClan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.ChatPageUtil;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
public class CommandClanHelp extends ClanSubCommand {
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void runFromAnywhere(@Nullable MinecraftServer server, @Nonnull ICommandSender sender, @Nullable String[] args) throws CommandException {
        if(args == null || args.length == 0 || args[0].matches("\\d+")) {
            int page = args == null || args.length < 1 ? 1 : parseInt(args[0]);
            List<ITextComponent> helps = Lists.newArrayList();
            for (Map.Entry<String, ClanSubCommand> command : CommandClan.commands.entrySet())
                helps.add(TranslationUtil.getTranslation(sender, "commands.clan.common.help_format",
                        TranslationUtil.getStringTranslation(sender, "commands.clan." + command.getKey() + ".usage"),
                        TranslationUtil.getStringTranslation(sender, "commands.clan." + command.getKey() + ".description")));
            helps.sort(Comparator.comparing(ITextComponent::getUnformattedText));

            ChatPageUtil.showPaginatedChat(sender, "/clan help %s", helps, page);
        } else if(CommandClan.aliases.containsKey(args[0]) || CommandClan.commands.containsKey(args[0])) {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.help_format",
                    TranslationUtil.getStringTranslation(sender, "commands.clan." + CommandClan.processAlias(args[0]) + ".usage"),
                    TranslationUtil.getStringTranslation(sender, "commands.clan." + CommandClan.processAlias(args[0]) + ".description")));
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.invalid_help", args[0]));
        }
    }

    @Override
    public boolean checkPermission(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender) {
        return true;
    }

    @Override
    public EnumRank getRequiredClanRank() {
        return EnumRank.ANY;
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
        if(args.length != 1)
            return comp;
        for(int i=1;i<CommandClan.commands.size()/ChatPageUtil.RESULTS_PER_PAGE;i++)
            comp.add(String.valueOf(i));
        comp.addAll(CommandClan.aliases.keySet());
        comp.addAll(CommandClan.commands.keySet());
        return getListOfStringsMatchingLastWord(args, comp);
    }
}
