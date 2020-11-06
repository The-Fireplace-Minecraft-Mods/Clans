package the_fireplace.clans.clan.metadata;

import com.google.gson.JsonObject;
import net.minecraft.util.text.TextFormatting;
import the_fireplace.clans.clan.ClanData;
import the_fireplace.clans.io.JsonReader;
import the_fireplace.clans.legacy.util.TextStyles;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClanColors extends ClanData {
    private static final Map<UUID, ClanColors> COLORS = new ConcurrentHashMap<>();

    public static ClanColors get(UUID clan) {
        COLORS.putIfAbsent(clan, new ClanColors(clan));
        return COLORS.get(clan);
    }

    public static void delete(UUID clan) {
        ClanColors colors = COLORS.remove(clan);
        if(colors != null)
            colors.delete();
    }

    private int color = new Random().nextInt(0xffffff);
    private int textFormattingColorIndex = TextStyles.getNearestTextColor(color).getColorIndex();

    private ClanColors(UUID clan) {
        super(clan, "color");
        if(!loadSavedData()) {
            markChanged();
        }
    }

    public int getColor() {
        return color;
    }

    public TextFormatting getColorFormatting() {
        TextFormatting colorFormatting = TextFormatting.fromColorIndex(textFormattingColorIndex);
        if(colorFormatting == null)
            colorFormatting = TextFormatting.RESET;
        return colorFormatting;
    }

    public void setColor(int color) {
        if(this.color != color) {
            this.color = color;
            this.textFormattingColorIndex = TextStyles.getNearestTextColor(color).getColorIndex();
            markChanged();
        }
    }

    @Override
    public void readFromJson(JsonReader reader) {
        setColor(reader.readInt("color", color));
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();

        obj.addProperty("color", color);

        return obj;
    }
}