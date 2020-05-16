package the_fireplace.clans.abstraction.dummy;

import the_fireplace.clans.abstraction.IChatCensorCompat;

public class ChatCensorCompatDummy implements IChatCensorCompat {
    @Override
    public String getCensoredString(String input) {
        return input;
    }
}
