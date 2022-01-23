package dev.the_fireplace.clans.economy;

import dev.the_fireplace.grandeconomy.api.injectables.CurrencyAPI;

import javax.inject.Inject;
import java.util.UUID;

public final class GrandEconomyWrapper implements ExternalEconomy
{
    private final CurrencyAPI currencyAPI;

    @Inject
    public GrandEconomyWrapper(CurrencyAPI currencyAPI) {
        this.currencyAPI = currencyAPI;
    }

    @Override
    public boolean deductAmount(double amount, UUID account) {
        return currencyAPI.takeFromBalance(account, amount, null);
    }

    @Override
    public double deductPartialAmount(double amount, UUID account) {
        double balance = getBalance(account);
        if (balance > amount) {
            deductAmount(amount, account);
            return 0;
        } else if (deductAmount(balance, account)) {
            return amount - balance;
        } else {
            return amount;
        }
    }

    @Override
    public boolean addAmount(double amount, UUID account) {
        return currencyAPI.addToBalance(account, amount, null);
    }

    @Override
    public double getBalance(UUID account) {
        return currencyAPI.getBalance(account, null);
    }

    @Override
    public String getFormattedCurrency(double amount) {
        return currencyAPI.formatCurrency(amount);
    }

    @Override
    public boolean isPresent() {
        return true;
    }
}
