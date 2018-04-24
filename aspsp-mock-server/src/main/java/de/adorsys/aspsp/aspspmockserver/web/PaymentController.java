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

package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.service.PaymentService;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus.ACCP;
import static de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus.RJCT;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping(path = "/payments")
public class PaymentController {
    private PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping(path = "/")
    public ResponseEntity<SpiSinglePayments> createPayment(
    @RequestBody SpiSinglePayments payment) throws Exception {
        return paymentService.addPayment(payment)
               .map(saved -> new ResponseEntity<>(saved, CREATED))
               .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping(path = "/{paymentId}/status/")
    public ResponseEntity getPaymentStatusById(
    @PathVariable("paymentId") String paymentId) {
        return paymentService.isPaymentExist(paymentId)
               ? ResponseEntity.ok(ACCP) : ResponseEntity.ok(RJCT);
    }
}