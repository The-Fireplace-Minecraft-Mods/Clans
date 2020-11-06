package the_fireplace.clans.economy;

import java.util.Collection;
import java.util.UUID;

import static the_fireplace.clans.legacy.ClansModContainer.getMinecraftHelper;

public class Economy {
    private static ExternalEconomy externalEconomy = new DummyEconomy();

    public static void detectAndUseExternalEconomy() {
        if(getMinecraftHelper().isPluginLoaded("grandeconomy"))
            externalEconomy = new GrandEconomyWrapper();
        else if(getMinecraftHelper().isPluginLoaded("spongeapi"))
            externalEconomy = new SpongeWrapper();
    }

    /**
     * Deduct the full amount from the account, or return false if the account does not have that much money.
     * @param amount
     * The amount to deduct
     * @param account
     * The account to deduct funds from
     * @return
     * True if the amount was deducted from the account, false if the account doesn't exist or the account doesn't have enough money.
     */
    public static boolean deductAmount(double amount, UUID account) {
        return externalEconomy.deductAmount(amount, account);
    }

    /**
     * Deduct an amount from an account, allowing partial deduction
     * @param amount
     * The amount to try to deduct
     * @param account
     * The account to deduct from
     * @return
     * The amount not deducted from the account. This should be 0 unless the account did not have enough funds to pay the full amount.
     */
    public static double deductPartialAmount(double amount, UUID account) {
        return externalEconomy.deductPartialAmount(amount, account);
    }

    /**
     * Add an amount to an account
     * @param amount
     * The amount to add
     * @param account
     * The account to add it to
     * @return
     * True if the amount was added, or false if the account doesn't exist.
     */
    public static boolean addAmount(double amount, UUID account) {
        return externalEconomy.addAmount(amount, account);
    }

    /**
     * Get the balance of an account
     * @param account
     * The account to get the balance of
     * @return
     * The account balance. Returns -1 if account not found.
     */
    public static double getBalance(UUID account) {
        return externalEconomy.getBalance(account);
    }

    public static String getFormattedCurrency(double amount) {
        return externalEconomy.getFormattedCurrency(amount);
    }

    public static double divideFundsAmongAccounts(double totalAmountToPay, Collection<UUID> accounts) {
        if(accounts.isEmpty())
            return totalAmountToPay;
        double amountToPayPerAccount = totalAmountToPay / accounts.size();
        for(UUID account: accounts)
            addAmount(amountToPayPerAccount, account);
        return 0;
    }
}
