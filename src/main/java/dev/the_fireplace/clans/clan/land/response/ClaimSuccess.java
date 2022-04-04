package dev.the_fireplace.clans.clan.land.response;

import dev.the_fireplace.clans.api.clan.interfaces.ClaimResponse;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;

public final class ClaimSuccess implements ClaimResponse
{
    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public List<Text> getMessages() {
        return Collections.emptyList();
    }
}
