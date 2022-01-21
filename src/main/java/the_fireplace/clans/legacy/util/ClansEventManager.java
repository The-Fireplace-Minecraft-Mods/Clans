package the_fireplace.clans.legacy.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import the_fireplace.clans.api.event.IClansEventHandler;

import java.util.HashMap;
import java.util.List;

public final class ClansEventManager
{
    private static final HashMap<Class, List<IClansEventHandler>> events = Maps.newHashMap();

    public static <V> void registerEvent(Class<V> eventType, IClansEventHandler<V> handler) {
        if (!events.containsKey(eventType)) {
            events.put(eventType, Lists.newArrayList());
        }
        events.get(eventType).add(handler);
    }

    public static <V> void unregisterEvent(Class<V> eventType, IClansEventHandler<V> handler) {
        events.get(eventType).remove(handler);
    }

    @SuppressWarnings("unchecked")
    public static <V> V fireEvent(V event) {
        List<IClansEventHandler> handlers = events.get(event.getClass());
        if (handlers != null) {
            for (IClansEventHandler<V> eventHandler : handlers) {
                event = eventHandler.run(event);
            }
        }
        return event;
    }
}
