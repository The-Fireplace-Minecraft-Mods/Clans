package the_fireplace.clans.util;

import com.google.common.collect.Lists;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import the_fireplace.clans.util.translation.TranslationUtil;

import java.util.Collection;
import java.util.List;

public final class ChatPageUtil {
    public static final int RESULTS_PER_PAGE = 7;

    public static void showPaginatedChat(ICommandSender sender, String command, Collection<ITextComponent> items, int page) {

        int current = page;
        int totalPageCount = items.size() % RESULTS_PER_PAGE > 0 ? (items.size()/ RESULTS_PER_PAGE)+1 : items.size()/ RESULTS_PER_PAGE;

        ITextComponent counter = TranslationUtil.getTranslation(sender, "clans.chat.page.num", current, totalPageCount);
        ITextComponent top = new TextComponentString("-----------------").setStyle(TextStyles.GREEN).appendSibling(counter).appendText("-------------------").setStyle(TextStyles.GREEN);

        //Expand page to be the first entry on the page
        page *= RESULTS_PER_PAGE;
        //Subtract result count because the first page starts with entry 0
        page -= RESULTS_PER_PAGE;
        int termLength = RESULTS_PER_PAGE;
        List<ITextComponent> printItems = Lists.newArrayList();

        for (ITextComponent item: items) {
            if (page-- > 0)
                continue;
            if (termLength-- <= 0)
                break;
            printItems.add(item);
        }

        ITextComponent nextButton = current < totalPageCount ? TranslationUtil.getTranslation(sender, "clans.chat.page.next").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(command, current+1)))) : new TextComponentString("-----");
        ITextComponent prevButton = current > 1 ? TranslationUtil.getTranslation(sender, "clans.chat.page.prev").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(command, current-1)))) : new TextComponentString("------");
        ITextComponent bottom = new TextComponentString("---------------").setStyle(TextStyles.GREEN).appendSibling(prevButton).appendText("---").setStyle(TextStyles.GREEN).appendSibling(nextButton).appendText("-------------").setStyle(TextStyles.GREEN);

        sender.sendMessage(top);

        for(ITextComponent item: printItems)
            sender.sendMessage(item);

        sender.sendMessage(bottom);
    }

    public static void showPaginatedChat(ICommandSender target, String command, Collection<ITextComponent> items) {
        showPaginatedChat(target, command, items, 1);
    }
}
