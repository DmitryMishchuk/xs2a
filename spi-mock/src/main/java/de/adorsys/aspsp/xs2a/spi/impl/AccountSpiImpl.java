package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.AccountMockData;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AccountSpiImpl implements AccountSpi {

    @Override
    public List<SpiAccountDetails> readAccounts(boolean withBalance, boolean psuInvolved) {

        if (!withBalance) {
            return getNoBalanceAccountList(AccountMockData.getAccountDetails());
        }

        return AccountMockData.getAccountDetails();
    }

    private List<SpiAccountDetails> getNoBalanceAccountList(List<SpiAccountDetails> accountDetails) {
        return accountDetails.stream()
               .map(account -> AccountMockData.createAccount(
               account.getId(),
               account.getCurrency(),
               null,
               account.getIban(),
               account.getBic(),
               account.getName(),
               account.getAccountType())).collect(Collectors.toList());
    }

    @Override
    public SpiBalances readBalances(String accountId, boolean psuInvolved) {
        SpiBalances balances = null;
        SpiAccountDetails accountDetails = AccountMockData.getAccountsHashMap().get(accountId);

        if (accountDetails != null) {
            balances = accountDetails.getBalances();
        }

        return balances;
    }

    @Override
    public List<SpiTransaction> readTransactionsByPeriod(String accountId, Date dateFrom, Date dateTo, boolean psuInvolved) {
        List<SpiTransaction> spiTransactions = AccountMockData.getSpiTransactions();

        List<SpiTransaction> validSpiTransactions = filterValidTransactionsByAccountId(spiTransactions, accountId);
        List<SpiTransaction> transactionsFilteredByPeriod = filterTransactionsByPeriod(validSpiTransactions, dateFrom, dateTo);

        return Collections.unmodifiableList(transactionsFilteredByPeriod);
    }

    @Override
    public List<SpiTransaction> readTransactionsById(String accountId, String transactionId, boolean psuInvolved) {
        List<SpiTransaction> spiTransactions = AccountMockData.getSpiTransactions();

        List<SpiTransaction> validSpiTransactions = filterValidTransactionsByAccountId(spiTransactions, accountId);
        List<SpiTransaction> filteredSpiTransactions = filterValidTransactionsByTransactionId(validSpiTransactions, transactionId);

        return Collections.unmodifiableList(filteredSpiTransactions);
    }

    private SpiTransaction[] getFilteredPendingTransactions(List<SpiTransaction> spiTransactions) {
        return spiTransactions.parallelStream()
               .filter(this::isPendingTransaction)
               .toArray(SpiTransaction[]::new);
    }

    private SpiTransaction[] getFilteredBookedTransactions(List<SpiTransaction> spiTransactions) {
        return spiTransactions.parallelStream()
               .filter(transaction -> !isPendingTransaction(transaction))
               .toArray(SpiTransaction[]::new);
    }

    private boolean isPendingTransaction(SpiTransaction spiTransaction) {
        return spiTransaction.getBookingDate() == null;
    }

    private List<SpiTransaction> filterTransactionsByPeriod(List<SpiTransaction> spiTransactions, Date dateFrom, Date dateTo) {
        return spiTransactions.parallelStream()
               .filter(transaction -> isDateInTimeFrame(transaction.getBookingDate(), dateFrom, dateTo))
               .collect(Collectors.toList());
    }

    private static boolean isDateInTimeFrame(Date currentDate, Date dateFrom, Date dateTo) {
        return currentDate != null && currentDate.after(dateFrom) && currentDate.before(dateTo);
    }

    private List<SpiTransaction> filterValidTransactionsByAccountId(List<SpiTransaction> spiTransactions, String accountId) {
        return spiTransactions.parallelStream()
               .filter(transaction -> transactionIsValid(transaction, accountId))
               .collect(Collectors.toList());
    }

    private List<SpiTransaction> filterValidTransactionsByTransactionId(List<SpiTransaction> spiTransactions, String transactionId) {
        return spiTransactions.parallelStream()
               .filter(transaction -> transactionId.equals(transaction.getTransactionId()))
               .collect(Collectors.toList());
    }

    private boolean transactionIsValid(SpiTransaction spiTransaction, String accountId) {

        boolean isCreditorAccountValid = Optional.ofNullable(spiTransaction.getCreditorAccount())
                                         .map(creditorAccount -> creditorAccount.getAccountId().trim().equals(accountId))
                                         .orElse(false);

        boolean isDebtorAccountValid = Optional.ofNullable(spiTransaction.getDebtorAccount())
                                       .map(debtorAccount -> debtorAccount.getAccountId().trim().equals(accountId))
                                       .orElse(false);

        return isCreditorAccountValid || isDebtorAccountValid;
    }
}
