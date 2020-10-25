package the_fireplace.clans.legacy.abstraction;

import java.util.UUID;

public interface IPaymentHandler {
	/**
	 * Deduct the full amount from the account, or return false if the account does not have that much money.
	 * @param amount
	 * The amount to deduct
	 * @param account
	 * The account to deduct funds from
	 * @return
	 * True if the amount was deducted from the account, false if the account doesn't exist or the account doesn't have enough money.
	 */
	boolean deductAmount(double amount, UUID account);

	/**
	 * Deduct an amount from an account, allowing partial deduction
	 * @param amount
	 * The amount to try to deduct
	 * @param account
	 * The account to deduct from
	 * @return
	 * The amount not deducted from the account. This should be 0 unless the account did not have enough funds to pay the full amount.
	 */
	double deductPartialAmount(double amount, UUID account);

	/**
	 * Add an amount to an account
	 * @param amount
	 * The amount to add
	 * @param account
	 * The account to add it to
	 * @return
	 * True if the amount was added, or false if the account doesn't exist.
	 */
	boolean addAmount(double amount, UUID account);

	/**
	 * Get the balance of an account
	 * @param account
	 * The account to get the balance of
	 * @return
	 * The account balance. Returns -1 if account not found.
	 */
	double getBalance(UUID account);

	String getCurrencyName(double amount);

	String getFormattedCurrency(double amount);
}
