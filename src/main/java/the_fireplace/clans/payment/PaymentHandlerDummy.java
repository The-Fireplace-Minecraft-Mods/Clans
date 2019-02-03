package the_fireplace.clans.payment;

import java.util.UUID;

public class PaymentHandlerDummy implements IPaymentHandler {
	@Override
	public boolean deductAmount(long amount, UUID account) {
		return true;
	}

	@Override
	public long deductPartialAmount(long amount, UUID account) {
		return 0;
	}

	@Override
	public boolean addAmount(long amount, UUID account) {
		return true;
	}

	@Override
	public void ensureAccountExists(UUID account) {

	}

	@Override
	public long getBalance(UUID account) {
		return 0;
	}

	@Override
	public String getCurrencyName(long amount) {
		return "";
	}
}
