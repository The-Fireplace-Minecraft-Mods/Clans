package the_fireplace.clans.economy;

import java.util.UUID;

public class DummyEconomy implements ExternalEconomy
{
    @Override
    public boolean deductAmount(double amount, UUID account) {
        return true;
    }

    @Override
    public double deductPartialAmount(double amount, UUID account) {
        return 0;
    }

    @Override
    public boolean addAmount(double amount, UUID account) {
        return true;
    }

    @Override
    public double getBalance(UUID account) {
        return 0;
    }

    @Override
    public String getFormattedCurrency(double amount) {
        return "";
    }

    @Override
    public boolean isPresent() {
        return false;
    }
}
