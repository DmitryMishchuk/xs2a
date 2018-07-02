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

package de.adorsys.aspsp.aspspmockserver.web.view;

import de.adorsys.aspsp.aspspmockserver.service.TanConfirmationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RequiredArgsConstructor
@Controller
@RequestMapping(path = "/view/payment/confirmation")
@Api(tags = "TAN confirmation", description = "Provides access to email TAN confirmation for payment execution")
public class TanConfirmationController {
    private final TanConfirmationService tanConfirmationService;

    @GetMapping(path = "/{psu-id}/{consent-id}")
    @ApiOperation(value = "Displays content of email TAN confirmation page")
    public ModelAndView showConfirmationPage(@PathVariable("psu-id") String psuId,
                                             @PathVariable("consent-id") String consentId) {
        return new ModelAndView("tanConfirmationPage", "paymentConfirmation", new PaymentConfirmation(psuId, consentId));
    }

    @PostMapping(path = "/")
    @ApiOperation(value = "Sends TAN to psu`s email, validates TAN sended to PSU`s e-mail and returns a link to continue as authenticated user")
    public ModelAndView confirmTan(
        @ModelAttribute("paymentConfirmation") PaymentConfirmation paymentConfirmation) {
        String psuId = paymentConfirmation.getPsuId();
        tanConfirmationService.generateAndSendTanForPsu(psuId);

        return tanConfirmationService.isPsuTanNumberValid(psuId, paymentConfirmation.getTanNumber())
                   ? new ModelAndView("consentConfirmationPage", "paymentConfirmation", paymentConfirmation)
                   : new ModelAndView("tanConfirmationError");
    }
}
