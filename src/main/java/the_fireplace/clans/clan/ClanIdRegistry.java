package the_fireplace.clans.clan;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.util.internal.ConcurrentSet;
import the_fireplace.clans.io.*;
import the_fireplace.clans.multithreading.ThreadedSaveHandler;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public final class ClanIdRegistry implements ThreadedJsonSerializable, JsonWritable, JsonReadable {
    private static final File REGISTRY_FILE = new File(Directories.CLAN_DATA_LOCATION, "registry.json");
    private static ClanIdRegistry instance = null;

    private static ClanIdRegistry getInstance() {
        if(instance == null)
            instance = new ClanIdRegistry();
        return instance;
    }

    public static UUID createAndRegisterClanId() {
        UUID clanId;
        do {
            clanId = UUID.randomUUID();
        } while(getInstance().hasId(clanId));
        getInstance().addId(clanId);
        return clanId;
    }

    @Deprecated
    static void addLegacy(UUID uuid) {
        getInstance().addId(uuid);
    }

    public static Collection<UUID> getIds() {
        return Collections.unmodifiableCollection(getInstance().clanIds);
    }

    public static boolean isValidClan(UUID uuid) {
        return getInstance().clanIds.contains(uuid);
    }

    public static void deleteClanId(UUID uuid) {
        getInstance().clanIds.remove(uuid);
    }

    private final ThreadedSaveHandler<ClanIdRegistry> saveHandler = ThreadedSaveHandler.create(this);
    private final Set<UUID> clanIds = new ConcurrentSet<>();

    private ClanIdRegistry() {
        if(REGISTRY_FILE.exists())
            load(REGISTRY_FILE);
    }

    private boolean hasId(UUID uuid) {
        return clanIds.contains(uuid);
    }

    private void addId(UUID uuid) {
        clanIds.add(uuid);
    }

    @Override
    public void readFromJson(JsonReader reader) {
        for(JsonElement entry: reader.readArray("ids"))
            clanIds.add(UUID.fromString(entry.getAsString()));
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();

        JsonArray ids = new JsonArray();
        clanIds.forEach(id -> ids.add(id.toString()));
        obj.add("ids", ids);

        return obj;
    }

    @Override
    public void blockingSave() {
        writeToJson(REGISTRY_FILE);
    }

    @Override
    public ThreadedSaveHandler<?> getSaveHandler() {
        return saveHandler;
    }
}
