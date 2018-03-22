package de.adorsys.aspsp.xs2a.spi.domain.headers.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Payment request header", value = "PaymentRequestHeader")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentRequestHeader extends CommonRequestHeader {

    @ApiModelProperty(value = "Might be mandated in the ASPSP's documentation, if OAuth is not chosen as Pre-Step", required = false, example = "5845f9b0-cef6-4f2d-97e0-ed1e0469a907")
    @JsonProperty(value = "psu-id")
    private String psuId;

    @ApiModelProperty(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility.", required = false, example = "5845f9b0-cef6-4f2d-97e0-ed1e0469a907")
    @JsonProperty(value = "psu-id-type")
    private String psuIdType;

    @ApiModelProperty(value = "Contained if not yet contained in the first request, and mandated by the ASPSP in the related response. This field is relevant only in a corporate context", required = false, example = "5845f9b0-cef6-4f2d-97e0-ed1e0469a907")
    @JsonProperty(value = "psu-corporate-id")
    private String psuCorporateId;

    @ApiModelProperty(value = "Might be mandated by the ASPSP in addition if the PSU-Corporate-ID is containe", required = false, example = "5845f9b0-cef6-4f2d-97e0-ed1e0469a907")
    @JsonProperty(value = "psu-corporate-id-type")
    private String psuCorporateIdType;
}
