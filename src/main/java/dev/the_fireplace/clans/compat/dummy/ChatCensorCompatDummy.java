package dev.the_fireplace.clans.compat.dummy;

import dev.the_fireplace.clans.domain.compat.ChatCensorCompat;

public class ChatCensorCompatDummy implements ChatCensorCompat
{
    @Override
    public String getCensoredString(String input) {
        return input;
    }
}
