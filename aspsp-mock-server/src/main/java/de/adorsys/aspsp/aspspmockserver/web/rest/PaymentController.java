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

package de.adorsys.aspsp.aspspmockserver.web.rest;

import de.adorsys.aspsp.aspspmockserver.service.PaymentService;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.AspspPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.util.CollectionUtils.isEmpty;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/payments")
@Api(tags = "Payments", description = "Provides access to payments")
public class PaymentController {
    private PaymentService paymentService;

    @ApiOperation(value = "Creates a single payment based on request body", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created", response = SpiSinglePayment.class),
        @ApiResponse(code = 204, message = "Payment Failed")})
    @PostMapping(path = "/")
    public ResponseEntity<SpiSinglePayment> createPayment(@RequestBody SpiSinglePayment payment) {
        return paymentService.addPayment(payment)
                   .map(saved -> new ResponseEntity<>(saved, CREATED))
                   .orElse(ResponseEntity.noContent().build());
    }

    @ApiOperation(value = "Creates a bulk payment(list of single payments) based on request body", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created", response = List.class),
        @ApiResponse(code = 204, message = "Payment Failed")})
    @PostMapping(path = "/bulk-payments")
    public ResponseEntity<List<SpiSinglePayment>> createBulkPayments(
        @RequestBody List<SpiSinglePayment> payments) {
        List<SpiSinglePayment> saved = paymentService.addBulkPayments(payments);
        return isEmpty(saved)
                   ? ResponseEntity.noContent().build()
                   : new ResponseEntity<>(saved, CREATED);
    }

    @ApiOperation(value = "Returns the status of payment requested by it`s ASPSP identifier", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SpiTransactionStatus.class),
        @ApiResponse(code = 204, message = "Payment Not Found")})
    @GetMapping(path = "/{paymentId}/status")
    public ResponseEntity<SpiTransactionStatus> getPaymentStatusById(@PathVariable("paymentId") String paymentId) {
        return paymentService.getPaymentStatusById(paymentId)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.noContent().build());
    }

    @ApiOperation(value = "Creates a periodic payment based on request body", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created", response = SpiPeriodicPayment.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    @PostMapping(path = "/create-periodic-payment")
    public ResponseEntity<SpiPeriodicPayment> createPeriodicPayment(@RequestBody SpiPeriodicPayment payment) {
        return paymentService.addPeriodicPayment(payment)
                   .map(saved -> new ResponseEntity<>(saved, CREATED))
                   .orElse(ResponseEntity.badRequest().build());
    }

    @ApiOperation(value = "Returns all payments present at ASPSP", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AspspPayment.class)})
    @GetMapping(path = "/getAllPayments")
    public ResponseEntity<List<AspspPayment>> getAllPayments() {
        List<AspspPayment> allPayments = paymentService.getAllPayments();
        return ResponseEntity.ok(allPayments);
    }

    @ApiOperation(value = "Returns the requested payment by it's paymentId", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AspspPayment.class)})
    @GetMapping(path = "/{paymentId}")
    public ResponseEntity<AspspPayment> getPaymentById(@PathVariable("paymentId") String paymentId) {
        return paymentService.getPaymentById(paymentId)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.noContent().build());
    }

    @ApiOperation(value = "Returns the payment requested by it`s ASPSP identifier", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AspspPayment.class),
        @ApiResponse(code = 204, message = "Payment Not Found")})
    @GetMapping(path = "/{payment-type}/{payment-product}/{paymentId}")
    public ResponseEntity<AspspPayment> getPaymentByIdAndTypeAndProduct(@PathVariable("payment-type") String paymentType, @PathVariable("payment-product") String paymentProduct, @PathVariable("paymentId") String paymentId) {
        return paymentService.getPaymentById(paymentId)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.noContent().build());
    }
}
