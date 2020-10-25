package the_fireplace.clans.legacy.util.translation;

import com.google.common.collect.Lists;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class TranslationUtil {

    public static List<UUID> clansClients = Lists.newArrayList();

    /**
     * Gets the translation for the given key and arguments and returns the unformatted string.
     */
    public static String getStringTranslation(String translationKey, Object... args) {
        return getTranslation(translationKey, args).getUnformattedText();
    }

    /**
     * Gets the translation for the given key and arguments and returns the unformatted string.
     */
    public static String getStringTranslation(ICommandSender sender, String translationKey, Object... args) {
        return getTranslation(sender, translationKey, args).getUnformattedText();
    }

    /**
     * Gets the translation for the given key and arguments and returns the unformatted string.
     */
    public static String getStringTranslation(@Nullable UUID target, String translationKey, Object... args) {
        return getTranslation(target, translationKey, args).getUnformattedText();
    }

    /**
     * Returns the translation key if the sender is able to translate it, or the translated string otherwise.
     */
    public static String getRawTranslationString(@Nullable ICommandSender sender, String translationKey) {
        return getRawTranslationString(sender instanceof EntityPlayerMP ? ((EntityPlayerMP) sender).getUniqueID() : null, translationKey);
    }

    /**
     * Returns the translation key if the target is able to translate it, or the translated string otherwise.
     */
    public static String getRawTranslationString(@Nullable UUID target, String translationKey) {
        if(target == null || !clansClients.contains(target))
            return I18n.translateToLocalFormatted(translationKey);
        else
            return translationKey;
    }

    /**
     * Returns the translated TextComponentString for the supplied key and arguments
     */
    public static ITextComponent getTranslation(String translationKey, Object... args) {
        return getTranslation((UUID)null, translationKey, args);
    }

    /**
     * Returns the TextComponentTranslation if the target is able to translate it, or the translated TextComponentString otherwise.
     */
    public static ITextComponent getTranslation(ICommandSender target, String translationKey, Object... args) {
        return getTranslation(target instanceof EntityPlayerMP ? ((EntityPlayerMP) target).getUniqueID() : null, translationKey, args);
    }

    /**
     * Returns the TextComponentTranslation if the target is able to translate it, or the translated TextComponentString otherwise.
     */
    public static ITextComponent getTranslation(@Nullable UUID target, String translationKey, Object... args) {
        if(target == null || !clansClients.contains(target))
            return new TextComponentString(I18n.translateToLocalFormatted(translationKey, args));
        else
            return new TextComponentTranslation(translationKey, args);
    }
}
