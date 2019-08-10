package the_fireplace.clans.forge.compat;

import the_fireplace.clans.abstraction.IPaymentHandler;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.grandeconomy.GrandEconomy;
import the_fireplace.grandeconomy.api.GrandEconomyApi;
import the_fireplace.grandeconomy.econhandlers.ge.GrandEconomyEconHandler;

import java.util.UUID;

public class PaymentHandlerGE implements IPaymentHandler {

	@Override
	public boolean deductAmount(long amount, UUID account) {
		boolean ret = GrandEconomyApi.takeFromBalance(account, amount, false);
		if(ret && ClanCache.getClanById(account) != null && GrandEconomy.economy instanceof GrandEconomyEconHandler)
			GrandEconomyApi.forceSave(account);
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
		GrandEconomyApi.addToBalance(account, amount, false);
		if(ClanCache.getClanById(account) != null && GrandEconomy.economy instanceof GrandEconomyEconHandler)
			GrandEconomyApi.forceSave(account);
		return true;
	}

	@Override
	public void ensureAccountExists(UUID account) {
		GrandEconomyApi.ensureAccountExists(account);
	}

	@Override
	public long getBalance(UUID account) {
		return GrandEconomyApi.getBalance(account);
	}

	@Override
	public String getCurrencyName(long amount) {
		return GrandEconomyApi.getCurrencyName(amount);
	}
}
