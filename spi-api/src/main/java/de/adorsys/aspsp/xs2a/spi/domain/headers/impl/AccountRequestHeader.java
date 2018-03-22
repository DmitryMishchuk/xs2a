package de.adorsys.aspsp.xs2a.spi.domain.headers.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.aspsp.xs2a.spi.domain.ReportType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Account request header", value = "AccountRequestHeader")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountRequestHeader extends CommonRequestHeader {

    @ApiModelProperty(value = "ID of the account consent", required = true, example = "5845f9b0-cef6-4f2d-97e0-ed1e0469a907")
    @JsonProperty(value = "consent-id")
    private String consentId;

    @ApiModelProperty(value = "Is contained only, if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in the related consent authorisation", required = false, example = "5845f9b0-cef6-4f2d-97e0-ed1e0469a907")
    @JsonProperty(value = "authorization bearer")
    private String authorizationBearer;

    @ApiModelProperty(value = "Indicates the formats of account reports supported together with a prioritisation following the http header definition", required = false, example = "application/json")
    @JsonProperty(value = "accept")
    private ReportType accept;

    @Override
    public boolean isValid() {
        return super.isValid() && notNullValidate();
    }

    private boolean notNullValidate() {
        return (consentId != null);
    }
}
