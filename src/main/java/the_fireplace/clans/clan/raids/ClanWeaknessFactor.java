package the_fireplace.clans.clan.raids;

import com.google.gson.JsonObject;
import the_fireplace.clans.clan.ClanData;
import the_fireplace.clans.io.JsonReader;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.util.FormulaParser;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClanWeaknessFactor extends ClanData {
    private static final Map<UUID, ClanWeaknessFactor> WEAKNESS_FACTOR_INSTANCES = new ConcurrentHashMap<>();

    public static ClanWeaknessFactor get(UUID clan) {
        WEAKNESS_FACTOR_INSTANCES.computeIfAbsent(clan, ClanWeaknessFactor::new);
        return WEAKNESS_FACTOR_INSTANCES.get(clan);
    }

    public static void delete(UUID clan) {
        ClanWeaknessFactor weaknessFactor = WEAKNESS_FACTOR_INSTANCES.remove(clan);
        if(weaknessFactor != null)
            weaknessFactor.delete();
    }

    private double weaknessFactor = 1.0;

    private ClanWeaknessFactor(UUID clan) {
        super(clan, "weaknessFactor");
    }

    public void decreaseWeaknessFactor() {
        setWeaknessFactor(ClansModContainer.getConfig().getDecreasedWeaknessFactorFormula());
    }

    public void increaseWeaknessFactor() {
        setWeaknessFactor(ClansModContainer.getConfig().getIncreasedWeaknessFactorFormula());
    }

    private void setWeaknessFactor(String formula) {
        double eval = FormulaParser.eval(formula, clan, 1.0);
        if (eval > 0.99)
            weaknessFactor = eval;
    }

    public double getWeaknessFactor() {
        return weaknessFactor;
    }

    @Override
    public void readFromJson(JsonReader reader) {
        weaknessFactor = reader.readDouble("multiplier", 1.0);
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();

        obj.addProperty("multiplier", weaknessFactor);

        return obj;
    }

    @Override
    protected boolean isDefaultData() {
        return Double.valueOf(weaknessFactor).equals(1.0);
    }
}