package the_fireplace.clans.forge.compat;

import the_fireplace.clans.Clans;
import the_fireplace.clans.abstraction.IPaymentHandler;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.grandeconomy.api.GrandEconomyApi;

import java.util.UUID;

public class PaymentHandlerGE implements IPaymentHandler {

	@Override
	public boolean deductAmount(long amount, UUID account) {
		boolean ret = GrandEconomyApi.takeFromBalance(account, amount, null);
		if(ret && ClanCache.getClanById(account) != null)
			if(Boolean.FALSE.equals(GrandEconomyApi.forceSave(account, false)))
				Clans.getLogger().warn("Unable to save clan account for clan with ID: {}", account.toString());
		return ret;
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
	public boolean addAmount(long amount, UUID account) {
		GrandEconomyApi.addToBalance(account, amount, null);
		if(ClanCache.getClanById(account) != null)
			if(Boolean.FALSE.equals(GrandEconomyApi.forceSave(account, false)))
				Clans.getLogger().warn("Unable to save clan account for clan with ID: {}", account.toString());
		return true;
	}

	@Override
	public void ensureAccountExists(UUID account) {
		GrandEconomyApi.ensureAccountExists(account, null);
	}

	@Override
	public long getBalance(UUID account) {
		return GrandEconomyApi.getBalance(account, null);
	}

	@Override
	public String getCurrencyName(long amount) {
		return GrandEconomyApi.getCurrencyName(amount);
	}

	@Override
	public String getCurrencyString(long amount) {
		return GrandEconomyApi.toString(amount);
	}
}
