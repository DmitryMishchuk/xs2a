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

package de.adorsys.aspsp.xs2a.config;


import de.adorsys.aspsp.xs2a.domain.PaymentType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;
import java.util.List;

@Data
@Configuration
@PropertySource("classpath:bank_profile.yml")
@ConfigurationProperties(prefix = "setting")
public class ProfileConfiguration {
    private final boolean isDelayedPaymentTypeAllowedAlways = true;

    private int frequencyPerDay;
    private boolean combinedServiceIndicator;
    private List<String> availablePaymentProducts;
    private List<String> availablePaymentTypes;
    private String scaApproach;
    private boolean httpSignature;

    @PostConstruct
    private void addNecessaryPaymentTypesByDefault() { //NOPMD It is necessary for set single payment available bu default
        String necessaryType = PaymentType.FUTURE_DATED.getValue();

        if (!availablePaymentTypes.contains(necessaryType)) {
            availablePaymentTypes.add(PaymentType.FUTURE_DATED.getValue());
        }
    }
}
