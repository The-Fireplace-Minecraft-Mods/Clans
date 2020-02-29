package the_fireplace.clans.sponge;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.transaction.ResultType;
import the_fireplace.clans.Clans;
import the_fireplace.clans.abstraction.IPaymentHandler;

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
    public long getBalance(UUID uuid) {
        if(getEcon() != null && getEcon().getOrCreateAccount(uuid).isPresent())
            return getEcon().getOrCreateAccount(uuid).get().getBalance(getEcon().getDefaultCurrency()).longValue();
        return 0;
    }

    @Override
    public boolean addAmount(long amount, UUID uuid) {
        if(getEcon() != null && getEcon().getOrCreateAccount(uuid).isPresent())
            return getEcon().getOrCreateAccount(uuid).get().deposit(getEcon().getDefaultCurrency(), BigDecimal.valueOf(amount), Cause.of(EventContext.empty(), Clans.MODID)).getResult().equals(ResultType.SUCCESS);
        return false;
    }

    @Override
    public boolean deductAmount(long amount, UUID uuid) {
        if(getEcon() != null && getEcon().getOrCreateAccount(uuid).isPresent())
            return getEcon().getOrCreateAccount(uuid).get().withdraw(getEcon().getDefaultCurrency(), BigDecimal.valueOf(amount), Cause.of(EventContext.empty(), Clans.MODID)).getResult().equals(ResultType.SUCCESS);
        return false;
    }

    @Override
    public long deductPartialAmount(long amount, UUID account) {
        long balance = getBalance(account);
        if(balance > amount) {
            deductAmount(amount, account);
            return 0;
        } else if(deductAmount(balance, account))
            return amount - balance;
        else
            return amount;
    }

    @Override
    public String getCurrencyName(long amount) {
        return getEcon() == null ? "" : (amount == 1 ? getEcon().getDefaultCurrency().getDisplayName().toPlain() : getEcon().getDefaultCurrency().getPluralDisplayName().toPlain());
    }

    @Override
    public String getCurrencyString(long amount) {
        return getEcon() == null ? "" : getEcon().getDefaultCurrency().format(BigDecimal.valueOf(amount)).toPlain();
    }

    @Override
    public void ensureAccountExists(UUID uuid) {
        if(getEcon() != null)
            getEcon().getOrCreateAccount(uuid);
    }
}
