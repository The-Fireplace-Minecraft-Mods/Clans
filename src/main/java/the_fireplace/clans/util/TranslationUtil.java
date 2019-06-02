package the_fireplace.clans.util;

import com.google.common.collect.Lists;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class TranslationUtil {

    public static List<UUID> clansClients = Lists.newArrayList();

    public static ITextComponent getTranslation(String message, Object... args) {
        return getTranslation(null, message, args);
    }

    public static ITextComponent getTranslation(@Nullable UUID target, String message, Object... args) {
        if(target == null || !clansClients.contains(target))
            return new TextComponentString(I18n.translateToLocalFormatted(message, args));
        else
            return new TextComponentTranslation(message, args);
    }
}
