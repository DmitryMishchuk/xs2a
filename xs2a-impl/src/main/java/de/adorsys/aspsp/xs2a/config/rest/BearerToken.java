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

package de.adorsys.aspsp.xs2a.config.rest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import static de.adorsys.aspsp.xs2a.spi.domain.constant.AuthorizationConstant.BEARER_TOKEN_PREFIX;

@Getter
@RequiredArgsConstructor
public class BearerToken {
    private final String token;

    public String getToken(){
        return StringUtils.substringAfter(token, BEARER_TOKEN_PREFIX);
    }
}
