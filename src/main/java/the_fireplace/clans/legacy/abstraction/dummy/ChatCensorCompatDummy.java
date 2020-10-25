package the_fireplace.clans.legacy.abstraction.dummy;

import the_fireplace.clans.legacy.abstraction.IChatCensorCompat;

public class ChatCensorCompatDummy implements IChatCensorCompat {
    @Override
    public String getCensoredString(String input) {
        return input;
    }
}
