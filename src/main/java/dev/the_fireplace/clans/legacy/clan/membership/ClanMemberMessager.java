package dev.the_fireplace.clans.legacy.clan.membership;

import dev.the_fireplace.clans.legacy.model.EnumRank;
import dev.the_fireplace.clans.legacy.util.translation.TranslationUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.text.Style;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public class ClanMemberMessager
{
    public static ClanMemberMessager get(UUID clan) {
        return new ClanMemberMessager(clan);
    }

    private final UUID clan;

    private ClanMemberMessager(UUID clan) {
        this.clan = clan;
    }

    public void messageAllOnline(Style textStyle, String translationKey, Object... args) {
        messageAllOnline(false, textStyle, translationKey, args);
    }

    public void messageAllOnline(@Nullable ServerPlayerEntity excluded, Style textStyle, String translationKey, Object... args) {
        messageAllOnline(false, excluded, textStyle, translationKey, args);
    }

    public void messageAllOnline(EnumRank minRank, Style textStyle, String translationKey, Object... args) {
        messageAllOnline(false, minRank, textStyle, translationKey, args);
    }

    public void messageAllOnline(boolean actionBar, Style textStyle, String translationKey, Object... args) {
        messageAllOnline(actionBar, EnumRank.ANY, textStyle, translationKey, args);
    }

    public void messageAllOnline(boolean actionBar, @Nullable ServerPlayerEntity excluded, Style textStyle, String translationKey, Object... args) {
        messageAllOnline(actionBar, EnumRank.ANY, excluded, textStyle, translationKey, args);
    }

    public void messageAllOnline(boolean actionBar, EnumRank minRank, Style textStyle, String translationKey, Object... args) {
        messageAllOnline(actionBar, minRank, null, textStyle, translationKey, args);
    }

    public void messageAllOnline(EnumRank minRank, @Nullable ServerPlayerEntity excluded, Style textStyle, String translationKey, Object... args) {
        messageAllOnline(false, minRank, excluded, textStyle, translationKey, args);
    }

    public void messageAllOnline(boolean actionBar, EnumRank minRank, @Nullable ServerPlayerEntity excluded, Style textStyle, String translationKey, Object... args) {
        Map<ServerPlayerEntity, EnumRank> online = ClanMembers.get(clan).getOnlineMemberRanks();
        for (ServerPlayerEntity member : online.keySet()) {
            if (online.get(member).greaterOrEquals(minRank) && (excluded == null || !member.getUniqueID().equals(excluded.getUniqueID()))) {
                member.sendStatusMessage(TranslationUtil.getTranslation(member.getUniqueID(), translationKey, args).setStyle(textStyle), actionBar && member.isEntityAlive());
            }
        }
    }
}