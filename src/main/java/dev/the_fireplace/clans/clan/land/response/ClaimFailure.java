package dev.the_fireplace.clans.clan.land.response;

import dev.the_fireplace.clans.api.clan.interfaces.ClaimResponse;
import net.minecraft.text.Text;

import java.util.List;

public final class ClaimFailure implements ClaimResponse
{
    private final List<Text> messages;

    public ClaimFailure(List<Text> messages) {
        this.messages = messages;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public List<Text> getMessages() {
        return messages;
    }
}
