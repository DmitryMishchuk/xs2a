package de.adorsys.aspsp.xs2a.service.validator;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.account.SupportedAccountReferenceFields;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.AspspProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountReferenceValidationService {
    private final AspspProfileService profileService;

    public Optional<MessageError> validateAccountReferences(Set<AccountReference> references) {
        List<SupportedAccountReferenceFields> supportedFields = profileService.getSupportedAccountReferenceFields();

        List<Boolean> list = references.stream()
                                 .map(ar -> isValidAccountReference(ar, supportedFields))
                                 .collect(Collectors.toList());
        return list.contains(false) || !list.contains(true)
                   ? Optional.of(new MessageError(TransactionStatus.RJCT, new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.FORMAT_ERROR)))
                   : Optional.empty();
    }

    private boolean isValidAccountReference(AccountReference reference, List<SupportedAccountReferenceFields> supportedFields) {
        List<Boolean> list = supportedFields.stream()
                                 .map(f -> f.isValid(reference))
                                 .filter(Optional::isPresent)
                                 .map(Optional::get)
                                 .collect(Collectors.toList());
        return list.contains(true) && !list.contains(false);
    }
}