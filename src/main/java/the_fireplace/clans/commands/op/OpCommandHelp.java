package the_fireplace.clans.commands.op;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.commands.CommandOpClan;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.util.ChatPageUtil;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class OpCommandHelp extends OpClanSubCommand {

    @Override
    @Nonnull
    public String getUsage(@Nullable ICommandSender sender) {
        return TranslationUtil.getRawTranslationString(sender, "commands.opclan.help.usage");
    }

    @Override
    public void runFromAnywhere(@Nullable MinecraftServer server, @Nonnull ICommandSender sender, @Nullable String[] args) throws CommandException {
        if(args == null || args.length == 0 || args[0].matches("\\d+")) {
            int page = args == null || args.length < 1 ? 1 : parseInt(args[0]);
            List<ITextComponent> helps = Lists.newArrayList();
            for (Map.Entry<String, ClanSubCommand> command : CommandOpClan.commands.entrySet())
                helps.add(TranslationUtil.getTranslation(sender, "commands.clan.common.help_format",
                        TranslationUtil.getStringTranslation(sender, "commands.opclan." + command.getKey() + ".usage"),
                        TranslationUtil.getStringTranslation(sender, "commands.opclan." + command.getKey() + ".description")));
            helps.sort(Comparator.comparing(ITextComponent::getUnformattedText));

            ChatPageUtil.showPaginatedChat(sender, "/opclan help %s", helps, page);
        } else if(CommandOpClan.aliases.containsKey(args[0]) || CommandOpClan.commands.containsKey(args[0])) {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.help_format",
                    TranslationUtil.getStringTranslation(sender, "commands.opclan." + CommandOpClan.processAlias(args[0]) + ".usage"),
                    TranslationUtil.getStringTranslation(sender, "commands.opclan." + CommandOpClan.processAlias(args[0]) + ".description")));
        } else {
            sender.sendMessage(TranslationUtil.getTranslation(sender, "commands.clan.common.invalid_help", args[0]));
        }
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
        for(int i = 1; i < CommandOpClan.commands.size()/ChatPageUtil.RESULTS_PER_PAGE; i++)
            comp.add(String.valueOf(i));
        comp.addAll(CommandOpClan.aliases.keySet());
        comp.addAll(CommandOpClan.commands.keySet());
        return comp;
    }
}
