package de.adorsys.aspsp.xs2a.spi.domain.ais.consents;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.aspsp.xs2a.spi.domain.ApiDateConstants;
import de.adorsys.aspsp.xs2a.spi.domain.TransactionStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(description = "Response for the get account information consent request by consent Id")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountConsents {
    @ApiModelProperty(value = "ID of the corresponding consent object as returned by an Account Information Consent Request", required = true)
    @JsonIgnore
    private final String id;
    
    @ApiModelProperty(value = "Access", required = true)
    private final AccountAccess access;
    
    @ApiModelProperty(value = "'true', if the consent is for recurring access to the account data , 'false', if the consent is for one access to the account data", required = true)
    private final boolean recurringIndicator;
    
    @ApiModelProperty(value = "valid until date for the requested consent. The content is the local ASPSP date in ISODate Format", required = true, example = "2017-10-30")
    @JsonFormat(pattern = ApiDateConstants.DATE_PATTERN)
    private final Date validUntil;
    
    @ApiModelProperty(value = "requested maximum frequency for an access per day. For a once-off access, this attribute is set to 1", required = true, example = "4")
    private final int frequencyPerDay;
    
    @ApiModelProperty(value = "This date is containing the date of the last action on the consent object either through the XS2A interface or the PSU/ASPSP interface having an impact on the status.", required = true, example = "2017-10-30")
    @JsonFormat(pattern = ApiDateConstants.DATE_PATTERN)
    private final Date lastActionDate;
    
    @ApiModelProperty(value = "Transaction status", required = true, example = "Pending")
    private final TransactionStatus transactionStatus;
    
    @ApiModelProperty(value = "The following code values are permitted 'empty', 'valid', 'blocked', 'expired', 'deleted'. These values might be extended by ASPSP by more values.", required = true)
    private final ConsentStatus consentStatus;
    
    @ApiModelProperty(name = "withBalance", value = "If contained, this function reads the list of accessible payment accounts including the balance.")
    @JsonIgnore
    private final boolean withBalance;
    
    @ApiModelProperty(name = "tppRedirectPreferred", value = "If it equals “true”, the TPP prefers a redirect over an embedded SCA approach.")
    @JsonIgnore
    private final boolean tppRedirectPreferred;
}
