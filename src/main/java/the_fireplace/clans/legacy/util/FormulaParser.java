package the_fireplace.clans.legacy.util;

import the_fireplace.clans.clan.land.ClanClaimCount;
import the_fireplace.clans.clan.membership.ClanMembers;
import the_fireplace.clans.clan.raids.ClanWeaknessFactor;
import the_fireplace.clans.economy.Economy;
import the_fireplace.clans.legacy.ClansModContainer;
import the_fireplace.clans.legacy.cache.RaidingParties;
import the_fireplace.clans.legacy.model.Raid;

import javax.annotation.Nullable;
import java.util.UUID;

public final class FormulaParser {
    public static double eval(String formula, UUID clan, double min) {
        return eval(formula, clan, RaidingParties.getActiveRaid(clan), min);
    }

    public static double eval(String formula, UUID clan, @Nullable Raid raid, double min) {
        formula = getFilteredFormula(formula, clan, raid);
        try {
            return Math.max(min, Double.parseDouble(String.valueOf(eval(formula))));
        } catch(ClassCastException|NullPointerException|NumberFormatException e) {
            ClansModContainer.getMinecraftHelper().getLogger().error("Problem with the configured formula: \"{}\" - does not evaluate to a decimal value", formula);
            e.printStackTrace();
        }
        return 0;
    }

    /*
     * Don't forget to update this link when updating the formula parser
     * https://gist.github.com/The-Fireplace/2b6e21b1892bc5eafc4c70ab49ed3505
     */
    private static String getFilteredFormula(String formula, UUID clan, @Nullable Raid raid) {
        //noinspection RegExpRedundantEscape
        formula = formula.replaceAll("[^cdmfpw\\.\\+\\-\\*\\/\\(\\)\\^0-9]", "");
        //Deal with multiplication that doesn't use a sign
        formula = formula.replaceAll("([0-9cdmfpw])([cdmfpw])|([cdmfpw])([0-9cdmfpw])", "\\1*\\2");
        //Deal with the old method of exponentiation
        formula = formula.replaceAll("\\*\\*", "\\^");
        formula = formula.replaceAll("c", String.valueOf(ClanClaimCount.get(clan).getClaimCount()));
        formula = formula.replaceAll("m", String.valueOf(getWeaknessFactor(clan)));
        formula = formula.replaceAll("f", String.valueOf(Economy.getBalance(clan)));
        formula = formula.replaceAll("p", String.valueOf(ClanMembers.get(clan).getMemberCount()));
        formula = formula.replaceAll("d", String.valueOf(getDefenderCount(clan, raid)));
        formula = formula.replaceAll("w", String.valueOf(getRaidersWLR(raid)));
        return formula;
    }

    private static double getRaidersWLR(@Nullable Raid raid) {
        return raid != null ? raid.getPartyWlr() : 1;
    }

    private static long getDefenderCount(UUID clan, @Nullable Raid raid) {
        return raid != null
            && raid.getInitDefenders() != null
            && !raid.getInitDefenders().isEmpty()
            ? raid.getInitDefenders().size()
            : ClanMembers.get(clan).getRaidDefenderCount();
    }

    private static double getWeaknessFactor(UUID clan) {
        return ClansModContainer.getConfig().isIncreasingRewards() ? ClanWeaknessFactor.get(clan).getWeaknessFactor() : 1;
    }

    private static double eval(final String formula) {
        return new MathParser(formula).parse();
    }

    /**
     * Formula parse code taken and modified from https://stackoverflow.com/a/26227947
     * Use this instead of javascript to avoid any problems with Minecraft's built in Java version not having a JavaScript engine.
     */
    private static class MathParser {
        int pos = -1, ch;
        private final String formula;

        MathParser(String formula) {
            this.formula = formula;
        }

        void nextChar() {
            ch = (++pos < formula.length()) ? formula.charAt(pos) : -1;
        }

        boolean eat(int charToEat) {
            while (ch == ' ')
                nextChar();
            if (ch == charToEat) {
                nextChar();
                return true;
            }
            return false;
        }

        double parse() {
            nextChar();
            double x = parseExpression();
            if (pos < formula.length())
                throw new RuntimeException("Unexpected: " + (char)ch);
            return x;
        }

        // Grammar:
        // expression = term | expression `+` term | expression `-` term
        // term = factor | term `*` factor | term `/` factor
        // factor = `+` factor | `-` factor | `(` expression `)`
        //        | number | functionName factor | factor `^` factor

        double parseExpression() {
            double x = parseTerm();
            for (;;) {
                if (eat('+'))
                    x += parseTerm(); // addition
                else if (eat('-'))
                    x -= parseTerm(); // subtraction
                else
                    return x;
            }
        }

        double parseTerm() {
            double x = parseFactor();
            for (;;) {
                if (eat('*'))
                    x *= parseFactor(); // multiplication
                else if (eat('/'))
                    x /= parseFactor(); // division
                else
                    return x;
            }
        }

        double parseFactor() {
            if (eat('+'))
                return parseFactor(); // unary plus
            if (eat('-'))
                return -parseFactor(); // unary minus

            double x;
            int startPos = this.pos;
            if (eat('(')) { // parentheses
                x = parseExpression();
                eat(')');
            } else if (isNumberCharacter()) { // numbers
                navigatePastNumber();
                x = Double.parseDouble(formula.substring(startPos, this.pos));
            } else if (isLetter()) { // functions
                navigatePastFunctionName();
                String func = formula.substring(startPos, this.pos);
                x = parseFactor();
                x = performFunction(x, func);
            } else
                throw new RuntimeException("Unexpected: " + (char)ch);

            if (eat('^'))
                x = Math.pow(x, parseFactor()); // exponentiation

            return x;
        }

        private boolean isNumberCharacter() {
            return isNumeric() || ch == '.';
        }

        private boolean isNumeric() {
            return ch >= '0' && ch <= '9';
        }

        private boolean isLetter() {
            return ch >= 'a' && ch <= 'z';
        }

        private void navigatePastNumber() {
            while (isNumberCharacter())
                nextChar();
        }

        private void navigatePastFunctionName() {
            while (isLetter())
                nextChar();
        }

        private double performFunction(double x, String func) {
            switch (func) {
                //TODO Make it possible to use these
                case "sqrt":
                    x = Math.sqrt(x);
                    break;
                case "sin":
                    x = Math.sin(Math.toRadians(x));
                    break;
                case "cos":
                    x = Math.cos(Math.toRadians(x));
                    break;
                case "tan":
                    x = Math.tan(Math.toRadians(x));
                    break;
                default:
                    throw new RuntimeException("Unknown function: " + func);
            }
            return x;
        }
    }
}
