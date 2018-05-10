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

package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.aspsp.xs2a.domain.AccountReference;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.ais.consent.*;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccessType;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ConsentMapper {
    public TransactionStatus mapFromSpiTransactionStatus(SpiTransactionStatus spiTransactionStatus) {
        return Optional.ofNullable(spiTransactionStatus)
               .map(ts -> TransactionStatus.valueOf(ts.name()))
               .orElse(null);
    }

    public SpiCreateConsentRequest mapToSpiCreateConsentRequest(CreateConsentReq consentReq) {
        return Optional.ofNullable(consentReq)
               .map(cr -> new SpiCreateConsentRequest(mapToSpiAccountAccess(cr.getAccess()),
               cr.isRecurringIndicator(), cr.getValidUntil(),
               cr.getFrequencyPerDay(), cr.isCombinedServiceIndicator()))
               .orElse(null);
    }

    public AccountConsent mapFromSpiAccountConsent(SpiAccountConsent spiAccountConsent) {
        return Optional.ofNullable(spiAccountConsent)
               .map(ac -> new AccountConsent(
               ac.getId(), mapFromSpiAccountAccess(ac.getAccess()),
               ac.isRecurringIndicator(), ac.getValidUntil(),
               ac.getFrequencyPerDay(), ac.getLastActionDate(),
               TransactionStatus.valueOf(ac.getSpiTransactionStatus().name()),
               ConsentStatus.valueOf(ac.getSpiConsentStatus().name()),
               ac.isWithBalance(), ac.isTppRedirectPreferred()))
               .orElse(null);
    }

    //Domain
    private AccountAccess mapFromSpiAccountAccess(SpiAccountAccess access) {
        return Optional.ofNullable(access)
               .map(aa -> {
                   AccountAccess accountAccess = new AccountAccess();
                   accountAccess.setAccounts(mapFromSpiAccountReferencesList(aa.getAccounts()));
                   accountAccess.setBalances(mapFromSpiAccountReferencesList(aa.getBalances()));
                   accountAccess.setTransactions(mapFromSpiAccountReferencesList(aa.getTransactions()));
                   accountAccess.setAvailableAccounts(mapFromSpiAccountAccessType(aa.getAvailableAccounts()));
                   accountAccess.setAllPsd2(mapFromSpiAccountAccessType(aa.getAllPsd2()));
                   return accountAccess;
               })
               .orElse(null);
    }

    private AccountReference[] mapFromSpiAccountReferencesList(List<SpiAccountReference> references) {
        if (references == null) {
            return null;
        }

        return references.stream().map(this::mapFromSpiAccountReference).toArray(AccountReference[]::new);
    }

    private AccountReference mapFromSpiAccountReference(SpiAccountReference reference) {
        return Optional.ofNullable(reference)
               .map(ar -> {
                   AccountReference accountReference = new AccountReference();
                   accountReference.setIban(ar.getIban());
                   accountReference.setBban(ar.getBban());
                   accountReference.setPan(ar.getPan());
                   accountReference.setMaskedPan(ar.getMaskedPan());
                   accountReference.setMsisdn(ar.getMsisdn());
                   accountReference.setCurrency(ar.getCurrency());

                   return accountReference;
               }).orElse(null);
    }

    private AccountAccessType mapFromSpiAccountAccessType(SpiAccountAccessType accessType) {
        if (accessType == null) {
            return null;
        } else {
            return AccountAccessType.valueOf(accessType.name());
        }
    }

    //Spi

    private SpiAccountAccess mapToSpiAccountAccess(AccountAccess access) {
        return Optional.ofNullable(access)
               .map(aa -> {
                   SpiAccountAccess spiAccountAccess = new SpiAccountAccess();
                   spiAccountAccess.setAccounts(mapToSpiAccountReferencesList(aa.getAccounts()));
                   spiAccountAccess.setBalances(mapToSpiAccountReferencesList(aa.getBalances()));
                   spiAccountAccess.setTransactions(mapToSpiAccountReferencesList(aa.getTransactions()));
                   spiAccountAccess.setAvailableAccounts(mapToSpiAccountAccessType(aa.getAvailableAccounts()));
                   spiAccountAccess.setAllPsd2(mapToSpiAccountAccessType(aa.getAllPsd2()));
                   return spiAccountAccess;
               })
               .orElse(null);
    }

    private List<SpiAccountReference> mapToSpiAccountReferencesList(AccountReference[] references) {
        if (references == null) {
            return null;
        }

        return Arrays.stream(references).map(this::mapToSpiAccountReference).collect(Collectors.toList());
    }

    public SpiAccountReference mapToSpiAccountReference(AccountReference reference) {
        return Optional.of(reference)
               .map(ar -> new SpiAccountReference(
               ar.getIban(),
               ar.getBban(),
               ar.getPan(),
               ar.getMaskedPan(),
               ar.getMsisdn(),
               ar.getCurrency())).orElse(null);
    }

    private SpiAccountAccessType mapToSpiAccountAccessType(AccountAccessType accessType) {
        if (accessType == null) {
            return null;
        } else {
            return SpiAccountAccessType.valueOf(accessType.name());
        }
    }
}
