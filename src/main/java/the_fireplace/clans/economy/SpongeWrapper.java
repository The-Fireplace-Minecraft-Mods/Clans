package the_fireplace.clans.economy;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import the_fireplace.clans.legacy.ClansModContainer;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.UUID;

class SpongeWrapper implements ExternalEconomy
{

    @Nullable
    private EconomyService spongeEcon;

    @Nullable
    private EconomyService getEcon() {
        if (spongeEcon == null) {
            spongeEcon = Sponge.getServiceManager().provide(EconomyService.class).isPresent() ? Sponge.getServiceManager().provide(EconomyService.class).get() : null;
        }
        return spongeEcon;
    }

    @Override
    public double getBalance(UUID uuid) {
        if (hasAccount(uuid)) {
            return getAccount(uuid).getBalance(getDefaultCurrency()).doubleValue();
        }
        return 0;
    }

    @Override
    public boolean addAmount(double amount, UUID uuid) {
        if (hasAccount(uuid)) {
            return getAccount(uuid).deposit(getDefaultCurrency(), BigDecimal.valueOf(amount), Cause.of(EventContext.empty(), ClansModContainer.MODID)).getResult().equals(ResultType.SUCCESS);
        }
        return !isPresent();
    }

    @Override
    public boolean deductAmount(double amount, UUID uuid) {
        if (hasAccount(uuid)) {
            return getAccount(uuid).withdraw(getDefaultCurrency(), BigDecimal.valueOf(amount), Cause.of(EventContext.empty(), ClansModContainer.MODID)).getResult().equals(ResultType.SUCCESS);
        }
        return !isPresent();
    }

    private boolean hasAccount(UUID uuid) {
        return getEcon() != null && getEcon().getOrCreateAccount(uuid).isPresent();
    }

    private UniqueAccount getAccount(UUID uuid) {
        return getEcon().getOrCreateAccount(uuid).get();
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
    public String getFormattedCurrency(double amount) {
        return getEcon() == null ? "" : getDefaultCurrency().format(BigDecimal.valueOf(amount)).toPlain();
    }

    @Override
    public boolean isPresent() {
        return getEcon() != null;
    }

    private Currency getDefaultCurrency() {
        return getEcon().getDefaultCurrency();
    }
}
