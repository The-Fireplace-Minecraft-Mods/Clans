package the_fireplace.clans.legacy.sponge;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.transaction.ResultType;
import the_fireplace.clans.ClansModContainer;
import the_fireplace.clans.legacy.abstraction.IPaymentHandler;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.UUID;

public class PaymentHandlerSponge implements IPaymentHandler {

    @Nullable
    private EconomyService spongeEcon;

    @Nullable
    private EconomyService getEcon() {
        if(spongeEcon == null)
            spongeEcon = Sponge.getServiceManager().provide(EconomyService.class).isPresent() ? Sponge.getServiceManager().provide(EconomyService.class).get() : null;
        return spongeEcon;
    }

    @Override
    public double getBalance(UUID uuid) {
        if(getEcon() != null && getEcon().getOrCreateAccount(uuid).isPresent())
            return getEcon().getOrCreateAccount(uuid).get().getBalance(getEcon().getDefaultCurrency()).doubleValue();
        return 0;
    }

    @Override
    public boolean addAmount(double amount, UUID uuid) {
        if(getEcon() != null && getEcon().getOrCreateAccount(uuid).isPresent())
            return getEcon().getOrCreateAccount(uuid).get().deposit(getEcon().getDefaultCurrency(), BigDecimal.valueOf(amount), Cause.of(EventContext.empty(), ClansModContainer.MODID)).getResult().equals(ResultType.SUCCESS);
        return false;
    }

    @Override
    public boolean deductAmount(double amount, UUID uuid) {
        if(getEcon() != null && getEcon().getOrCreateAccount(uuid).isPresent())
            return getEcon().getOrCreateAccount(uuid).get().withdraw(getEcon().getDefaultCurrency(), BigDecimal.valueOf(amount), Cause.of(EventContext.empty(), ClansModContainer.MODID)).getResult().equals(ResultType.SUCCESS);
        return false;
    }

    @Override
    public double deductPartialAmount(double amount, UUID account) {
        double balance = getBalance(account);
        if(balance > amount) {
            deductAmount(amount, account);
            return 0;
        } else if(deductAmount(balance, account))
            return amount - balance;
        else
            return amount;
    }

    @Override
    public String getCurrencyName(double amount) {
        return getEcon() == null ? "" : (amount == 1 ? getEcon().getDefaultCurrency().getDisplayName().toPlain() : getEcon().getDefaultCurrency().getPluralDisplayName().toPlain());
    }

    @Override
    public String getFormattedCurrency(double amount) {
        return getEcon() == null ? "" : getEcon().getDefaultCurrency().format(BigDecimal.valueOf(amount)).toPlain();
    }
}
