package the_fireplace.clans.util;

import the_fireplace.clans.Clans;
import the_fireplace.clans.ClansHelper;
import the_fireplace.clans.cache.RaidingParties;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.Raid;

import javax.annotation.Nullable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public final class FormulaParser {
    public static double eval(String formula, Clan clan, double min) {
        return eval(formula, clan, RaidingParties.getActiveRaid(clan), min);
    }

    public static double eval(String formula, Clan clan, @Nullable Raid raid, double min) {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByExtension("js");
        formula = getFilteredFormula(formula, clan, raid);
        if(engine == null) {
            Clans.getMinecraftHelper().getLogger().error("Script engine was not found! Formula-based options will be 0!");
            return 0;
        }
        try {
            return Math.max(min, Double.parseDouble(String.valueOf(engine.eval(formula))));
        } catch(ScriptException e) {
            Clans.getMinecraftHelper().getLogger().error("Problem with the configured formula: {}: {}", formula, e.getMessage());
            e.printStackTrace();
        } catch(ClassCastException|NullPointerException|NumberFormatException e) {
            Clans.getMinecraftHelper().getLogger().error("Problem with the configured formula: {} - does not evaluate to a decimal value", formula);
            e.printStackTrace();
        }
        return 0;
    }

    public static String getFilteredFormula(String formula, Clan clan) {
        return getFilteredFormula(formula, clan, RaidingParties.getActiveRaid(clan));
    }

    /*
     * Don't forget to update this link when updating the formula parser
     * https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505
     */
    private static String getFilteredFormula(String formula, Clan clan, @Nullable Raid raid) {
        //noinspection RegExpRedundantEscape
        formula = formula.replaceAll("[^cdmfpw\\.\\+\\-\\*\\/\\(\\)0-9]", "");
        //Deal with multiplication that doesn't use a sign
        formula = formula.replaceAll("([0-9cdmfpw])([cdmfpw])|([cdmfpw])([0-9cdmfpw])", "\\1*\\2");
        formula = formula.replaceAll("c", String.valueOf(clan.getClaimCount()));
        formula = formula.replaceAll("m", String.valueOf(ClansHelper.getConfig().isIncreasingRewards() ? clan.getRaidRewardMultiplier() : 1));
        formula = formula.replaceAll("f", String.valueOf(ClansHelper.getPaymentHandler().getBalance(clan.getId())));
        formula = formula.replaceAll("p", String.valueOf(clan.getMemberCount()));
        formula = formula.replaceAll("d", String.valueOf(raid != null ? raid.getInitDefenders().size() : clan.getOnlineSurvivalMembers().size()));
        formula = formula.replaceAll("w", String.valueOf(raid != null ? raid.getPartyWlr() : 1));
        return formula;
    }
}
