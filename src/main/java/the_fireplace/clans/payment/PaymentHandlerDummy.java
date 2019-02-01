package the_fireplace.clans.payment;

public class PaymentHandlerDummy implements IPaymentHandler {
	@Override
	public boolean buyChunk() {
		return true;
	}

	@Override
	public boolean buyClan() {
		return true;
	}
}
