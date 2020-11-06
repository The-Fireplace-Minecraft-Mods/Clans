package the_fireplace.clans.clan.metadata;

import com.google.gson.JsonObject;
import the_fireplace.clans.clan.ClanData;
import the_fireplace.clans.io.JsonReader;
import the_fireplace.clans.legacy.util.TextStyles;
import the_fireplace.clans.legacy.util.translation.TranslationUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClanDescriptions extends ClanData {
    private static final Map<UUID, ClanDescriptions> DESCRIPTIONS = new ConcurrentHashMap<>();

    public static ClanDescriptions get(UUID clan) {
        DESCRIPTIONS.putIfAbsent(clan, new ClanDescriptions(clan));
        return DESCRIPTIONS.get(clan);
    }

    public static void delete(UUID clan) {
        ClanDescriptions description = DESCRIPTIONS.remove(clan);
        if(description != null)
            description.delete();
    }

    private String description = TranslationUtil.getStringTranslation("clan.default_description");

    private ClanDescriptions(UUID clan) {
        super(clan, "description");
        loadSavedData();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if(!this.description.equals(description)) {
            this.description = description;
            markChanged();
        }
    }

    @Override
    public void readFromJson(JsonReader reader) {
        description = reader.readString("clanDescription", description);
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();

        obj.addProperty("clanDescription", TextStyles.stripFormatting(description));

        return obj;
    }
}
