/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.account.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.account.AccountReport;
import de.adorsys.aspsp.xs2a.domain.consent.AccountAccess;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.service.validator.ValidationGroup;
import de.adorsys.aspsp.xs2a.service.validator.ValueValidatorService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBookingStatus;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.CONSENT_INVALID;
import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.RESOURCE_UNKNOWN_404;
import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;

@Slf4j
@Service
@Validated
@AllArgsConstructor
public class AccountService {

    private final AccountSpi accountSpi;
    private final AccountMapper accountMapper;
    private final ValueValidatorService validatorService;
    private final ConsentService consentService;

    public ResponseObject<Map<String, List<AccountDetails>>> getAccountDetailsList(String consentId, boolean withBalance, boolean psuInvolved) {
        ResponseObject<AccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<Map<String, List<AccountDetails>>>builder()
                       .fail(allowedAccountData.getError()).build();
        }

        List<AccountDetails> accountDetails = getAccountDetailsFromReferences(withBalance, allowedAccountData.getBody());

        return accountDetails.isEmpty()
                   ? ResponseObject.<Map<String, List<AccountDetails>>>builder()
                         .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build()
                   : ResponseObject.<Map<String, List<AccountDetails>>>builder()
                         .body(Collections.singletonMap("accountList", accountDetails)).build();
    }

    public ResponseObject<AccountDetails> getAccountDetails(String consentId, String accountId, boolean withBalance, boolean psuInvolved) {
        AccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId));
        if (accountDetails == null) {
            return ResponseObject.<AccountDetails>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        }
        ResponseObject<AccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<AccountDetails>builder()
                       .fail(allowedAccountData.getError()).build();
        }
        AccountDetails details = null;
        if (withBalance && consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getBalances())) {
            details = accountDetails;
        } else if (!withBalance && consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getAccounts())) {
            details = getAccountDetailNoBalances(accountDetails);
        }

        return details == null
                   ? ResponseObject.<AccountDetails>builder()
                         .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build()
                   : ResponseObject.<AccountDetails>builder()
                         .body(details).build();
    }

    public ResponseObject<List<Balances>> getBalances(String consentId, String accountId, boolean psuInvolved) {
        AccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId));
        if (accountDetails == null) {
            return ResponseObject.<List<Balances>>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        }
        ResponseObject<AccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<List<Balances>>builder()
                       .fail(allowedAccountData.getError()).build();
        }

        return consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getBalances())
                   ? ResponseObject.<List<Balances>>builder().body(accountDetails.getBalances()).build()
                   : ResponseObject.<List<Balances>>builder()
                         .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build();
    }

    public ResponseObject<AccountReport> getAccountReport(String consentId, String accountId, Instant dateFrom,
                                                          Instant dateTo, String transactionId,
                                                          boolean psuInvolved, BookingStatus bookingStatus, boolean withBalance, boolean deltaList) {

        AccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId));
        if (accountDetails == null) {
            return ResponseObject.<AccountReport>builder().fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        }
        ResponseObject<AccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<AccountReport>builder()
                       .fail(allowedAccountData.getError()).build();
        }
        AccountReport accountReport = consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getTransactions())
                                          ? getAccountReport(accountDetails, dateFrom, dateTo, transactionId, bookingStatus, allowedAccountData.getBody().getTransactions())
                                          : null;

        return accountReport == null
                   ? ResponseObject.<AccountReport>builder().fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build()
                   : ResponseObject.<AccountReport>builder().body(accountReport).build();
    }

    public List<Balances> getAccountBalancesByAccountReference(AccountReference reference) {
        return Optional.ofNullable(reference)
                   .map(ref -> accountSpi.readAccountDetailsByIban(ref.getIban()))
                   .map(Collection::stream)
                   .flatMap(accDets -> accDets
                                           .filter(spiAcc -> spiAcc.getCurrency() == reference.getCurrency())
                                           .findFirst())
                   .map(spiAcc -> accountMapper.mapToBalancesList(spiAcc.getBalances()))
                   .orElse(Collections.emptyList());
    }

    public boolean isAccountExists(AccountReference reference) {
        return getAccountDetailsByAccountReference(reference).isPresent();
    }

    private List<AccountDetails> getAccountDetailsFromReferences(boolean withBalance, AccountAccess accountAccess) {
        List<AccountReference> references = withBalance
                                                ? accountAccess.getBalances()
                                                : accountAccess.getAccounts();
        List<AccountDetails> details = getAccountDetailsFromReferences(references);
        return withBalance
                   ? details
                   : getAccountDetailsNoBalances(details);
    }

    private List<AccountDetails> getAccountDetailsFromReferences(List<AccountReference> references) {
        return CollectionUtils.isEmpty(references)
                   ? Collections.emptyList()
                   : references.stream()
                         .map(this::getAccountDetailsByAccountReference)
                         .filter(Optional::isPresent)
                         .collect(Collectors.mapping(Optional::get, Collectors.toList()));
    }

    private List<AccountDetails> getAccountDetailsNoBalances(List<AccountDetails> details) {
        return details.stream()
                   .map(this::getAccountDetailNoBalances)
                   .collect(Collectors.toList());
    }

    private AccountDetails getAccountDetailNoBalances(AccountDetails detail) {
        return new AccountDetails(detail.getId(), detail.getIban(), detail.getBban(), detail.getPan(),
            detail.getMaskedPan(), detail.getMsisdn(), detail.getCurrency(), detail.getName(),
            detail.getAccountType(), detail.getCashAccountType(), detail.getBic(), null);
    }

    private AccountReport getAccountReport(AccountDetails details, Instant dateFrom, Instant dateTo, String transactionId, BookingStatus bookingStatus, List<AccountReference> allowedAccountData) {
        Instant dateToChecked = dateTo == null ? Instant.now() : dateTo;
        return StringUtils.isBlank(transactionId)
                   ? getAccountReportByPeriod(details, dateFrom, dateToChecked, bookingStatus, allowedAccountData)
                   : getAccountReportByTransaction(details, transactionId, allowedAccountData);
    }

    private AccountReport getAccountReportByPeriod(AccountDetails details, Instant dateFrom, Instant dateTo, BookingStatus bookingStatus, List<AccountReference> allowedAccountData) {
        validateAccountIdPeriod(details.getIban(), dateFrom, dateTo);
        return getAllowedTransactionsByAccess(readTransactionsByPeriod(details, dateFrom, dateTo, bookingStatus), allowedAccountData);
    }

    private AccountReport getAccountReportByTransaction(AccountDetails details, String transactionId, List<AccountReference> allowedAccountData) {
        validateAccountIdTransactionId(details.getIban(), transactionId);
        return readTransactionsById(transactionId, allowedAccountData);
    }

    private AccountReport getAllowedTransactionsByAccess(AccountReport accountReport, List<AccountReference> allowedAccountData) {
        if (accountReport == null) {
            return null;
        }
        Transactions[] booked = getAllowedTransactions(accountReport.getBooked(), allowedAccountData);
        Transactions[] pending = getAllowedTransactions(accountReport.getPending(), allowedAccountData);
        return new AccountReport(booked, pending);
    }

    private Transactions[] getAllowedTransactions(Transactions[] transactions, List<AccountReference> allowedAccountData) {
        return Arrays.stream(transactions).allMatch(t -> isAllowedTransaction(t, allowedAccountData))
                   ? transactions
                   : new Transactions[]{};
    }

    private boolean isAllowedTransaction(Transactions transaction, List<AccountReference> allowedAccountData) {
        return consentService.isValidAccountByAccess(transaction.getCreditorAccount().getIban(), transaction.getCreditorAccount().getCurrency(), allowedAccountData)
                   || consentService.isValidAccountByAccess(transaction.getDebtorAccount().getIban(), transaction.getDebtorAccount().getCurrency(), allowedAccountData);
    }

    private AccountReport readTransactionsByPeriod(AccountDetails details, Instant dateFrom,
                                                   Instant dateTo, BookingStatus bookingStatus) { //NOPMD TODO to be reviewed upon change to v1.1
        Optional<AccountReport> result = accountMapper.mapToAccountReport(accountSpi.readTransactionsByPeriod(details.getIban(), details.getCurrency(), dateFrom, dateTo, SpiBookingStatus.valueOf(bookingStatus.name())));

        return result.orElseGet(() -> new AccountReport(new Transactions[]{}, new Transactions[]{}));
    }

    private AccountReport readTransactionsById(String transactionId, List<AccountReference> allowedAccountData) {
        SpiTransaction spiTransaction = accountSpi.readTransactionsById(transactionId);
        return isAllowedSpiTransaction(spiTransaction, allowedAccountData)
                   ? accountMapper.mapToAccountReport(Collections.singletonList(spiTransaction)).orElseGet(() -> null)
                   : null;
    }

    private boolean isAllowedSpiTransaction(SpiTransaction spiTransaction, List<AccountReference> allowedAccountData) {
        return Optional.ofNullable(spiTransaction)
                   .filter(t -> isAllowedTransaction(accountMapper.mapToTransaction(t), allowedAccountData))
                   .isPresent();
    }

    private Optional<AccountDetails> getAccountDetailsByAccountReference(AccountReference reference) {
        return Optional.ofNullable(reference)
                   .map(ref -> accountSpi.readAccountDetailsByIban(ref.getIban()))
                   .map(Collection::stream)
                   .flatMap(accDets -> accDets
                                           .filter(spiAcc -> spiAcc.getCurrency() == reference.getCurrency())
                                           .findFirst())
                   .map(accountMapper::mapToAccountDetails);
    }

    // Validation
    private void validateAccountIdPeriod(String accountId, Instant dateFrom, Instant dateTo) {
        ValidationGroup fieldValidator = new ValidationGroup();
        fieldValidator.setAccountId(accountId);
        fieldValidator.setDateFrom(dateFrom);
        fieldValidator.setDateTo(dateTo);

        validatorService.validate(fieldValidator, ValidationGroup.AccountIdAndPeriodIsValid.class);
    }

    private void validateAccountIdTransactionId(String accountId, String transactionId) {
        ValidationGroup fieldValidator = new ValidationGroup();
        fieldValidator.setAccountId(accountId);
        fieldValidator.setTransactionId(transactionId);

        validatorService.validate(fieldValidator, ValidationGroup.AccountIdAndTransactionIdIsValid.class);
    }
}
