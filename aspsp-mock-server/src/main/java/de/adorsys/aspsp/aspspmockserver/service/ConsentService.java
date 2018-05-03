package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.repository.ConsentRepository;
import de.adorsys.aspsp.aspspmockserver.repository.PsuRepository;
import de.adorsys.aspsp.xs2a.spi.domain.Psu;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccessType;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConsentService {
    private ConsentRepository consentRepository;
    private PsuRepository psuRepository;

    @Autowired
    public ConsentService(ConsentRepository consentRepository, PsuRepository psuRepository) {
        this.consentRepository = consentRepository;
        this.psuRepository = psuRepository;
    }

    public Optional<String> createConsentAndReturnId(SpiCreateConsentRequest request, String psuId) {
        return readActualAccountAccess(request.getAccess(), psuId)
                       .filter(access -> access.isNotEmpty())
                       .map(access -> saveNewConsentWithAccess(access, request.isRecurringIndicator(), request.getValidUntil(), request.getFrequencyPerDay()))
                       .map(SpiAccountConsent::getId);
    }

    public SpiAccountConsent getConsent(String id) {
        return consentRepository.findOne(id);
    }

    public List<SpiAccountConsent> getAllConsents() {
        return consentRepository.findAll();
    }

    public boolean deleteConsentById(String id) {
        if (id != null && consentRepository.exists(id)) {
            consentRepository.delete(id);
            return true;
        }
        return false;
    }

    private SpiAccountConsent saveNewConsentWithAccess(SpiAccountAccess access, boolean recurringIndicator, Date validUntil, Integer frequencyPerDay) {
        return consentRepository.save(
                new SpiAccountConsent(null, access,
                        recurringIndicator, validUntil, frequencyPerDay, new Date(),
                        SpiTransactionStatus.ACCP, SpiConsentStatus.VALID, true, true));
    }

    private Optional<SpiAccountAccess> readActualAccountAccess(SpiAccountAccess accountAccess, String psuId) {
        return Optional.ofNullable(accountAccess)
                       .flatMap(access -> getActualAccess(access, psuId));
    }

    private Optional<SpiAccountAccess> getActualAccess(SpiAccountAccess access, String psuId) {
        if (hasAccessToAllAccounts(access)) {
            return getActualAccessForAllAccounts(access, psuId);
        } else {
            return getActualAccessToCertainAccounts(access);
        }
    }

    private Optional<SpiAccountAccess> getActualAccessForAllAccounts(SpiAccountAccess access, String psuId) {
        return getAccountReferencesByPsuId(psuId)
                       .map(references -> new SpiAccountAccess(
                               references,
                               references,
                               references,
                               getActualAccessType(access.getAvailableAccounts()),
                               getActualAccessType(access.getAllPsd2())));
    }

    private Optional<SpiAccountAccess> getActualAccessToCertainAccounts(SpiAccountAccess access) {
        return Optional.of(new SpiAccountAccess(
                mapActualAccountReferences(access.getAccounts()),
                mapActualAccountReferences(access.getBalances()),
                mapActualAccountReferences(access.getTransactions()),
                null,
                null));
    }

    private SpiAccountAccessType getActualAccessType(SpiAccountAccessType type) {
        if (type == SpiAccountAccessType.ALL_ACCOUNTS) {
            return SpiAccountAccessType.ALL_ACCOUNTS;
        } else {
            return null;
        }
    }

    private Optional<List<SpiAccountReference>> getAccountReferencesByPsuId(String psuId) {
        return Optional.ofNullable(psuRepository.findOne(psuId))
                       .filter(Objects::nonNull)
                       .map(psu -> mapToSpiAccountReference(psu.getAccountDetailsList()));
    }

    private boolean hasAccessToAllAccounts(SpiAccountAccess access) {
        return access.getAvailableAccounts() == SpiAccountAccessType.ALL_ACCOUNTS
                       || access.getAllPsd2() == SpiAccountAccessType.ALL_ACCOUNTS;
    }

    private List<SpiAccountReference> mapActualAccountReferences(List<SpiAccountReference> references) {
        List<String> ibans = getIbanListFromAccountReferences(references);

        if (ibans.isEmpty()) {
            return Collections.emptyList();
        }

        return getAccountsReferencesByIbans(ibans);
    }

    private List<String> getIbanListFromAccountReferences(List<SpiAccountReference> references) {
        return Optional.ofNullable(references)
                       .map(refs -> refs.stream().map(SpiAccountReference::getIban).collect(Collectors.toList()))
                       .orElse(Collections.emptyList());
    }

    private List<SpiAccountReference> getAccountsReferencesByIbans(List<String> ibans) {
        List<Psu> psuList = psuRepository.findPsuByAccountDetailsList_IbanIn(ibans);
        return mapPsuListToAccountRefList(psuList, ibans);
    }

    private List<SpiAccountReference> mapPsuListToAccountRefList(List<Psu> psuList, List<String> ibans) {
        return psuList.stream()
                   .flatMap(psu -> psu.getAccountDetailsList().stream())
                   .filter(acc -> ibans.contains(acc.getIban()))
                   .map(this::mapToSpiAccountReference)
                   .collect(Collectors.toList());
    }

    private List<SpiAccountReference> mapToSpiAccountReference(List<SpiAccountDetails> detailsList) {
        return detailsList.stream()
                       .map(this::mapToSpiAccountReference).collect(Collectors.toList());
    }

    private SpiAccountReference mapToSpiAccountReference(SpiAccountDetails details) {
        return new SpiAccountReference(details.getId(), details.getIban(), details.getBban(), details.getPan(), details.getMaskedPan(), details.getMsisdn(), details.getCurrency());
    }
}
