package dev.the_fireplace.clans.api.clan.interfaces;

import net.minecraft.text.Text;

import java.util.List;

public interface ClaimResponse
{
    boolean isSuccess();

    List<Text> getMessages();
}
