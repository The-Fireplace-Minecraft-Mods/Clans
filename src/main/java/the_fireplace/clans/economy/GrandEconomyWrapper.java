package the_fireplace.clans.economy;

import the_fireplace.grandeconomy.api.GrandEconomyApi;

import java.util.UUID;

class GrandEconomyWrapper implements ExternalEconomy {

	@Override
	public boolean deductAmount(double amount, UUID account) {
		return GrandEconomyApi.takeFromBalance(account, amount, null);
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
	public boolean addAmount(double amount, UUID account) {
		return GrandEconomyApi.addToBalance(account, amount, null);
	}

	@Override
	public double getBalance(UUID account) {
		return GrandEconomyApi.getBalance(account, null);
	}

	@Override
	public String getFormattedCurrency(double amount) {
		return GrandEconomyApi.getFormattedCurrency(amount);
	}
}
