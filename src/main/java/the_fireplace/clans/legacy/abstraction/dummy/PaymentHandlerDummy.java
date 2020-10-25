package the_fireplace.clans.legacy.abstraction.dummy;

import the_fireplace.clans.legacy.abstraction.IPaymentHandler;

import java.util.UUID;

public class PaymentHandlerDummy implements IPaymentHandler {
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
	public String getCurrencyName(double amount) {
		return "";
	}

	@Override
	public String getFormattedCurrency(double amount) {
		return "";
	}
}
