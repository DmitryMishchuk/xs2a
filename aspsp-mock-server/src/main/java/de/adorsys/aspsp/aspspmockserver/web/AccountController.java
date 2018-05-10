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

import de.adorsys.aspsp.aspspmockserver.service.AccountService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/account")
public class AccountController {
    private AccountService accountService;

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/")
    public ResponseEntity<List<SpiAccountDetails>> readAllAccounts(@RequestParam(value = "consent-id", required = true) String consentId) {
        List<SpiAccountDetails> result = accountService.getAllAccounts(consentId);
        return result.isEmpty()
                   ? new ResponseEntity<>(HttpStatus.FORBIDDEN)
                   : new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/{accountId}")
    public ResponseEntity<SpiAccountDetails> readAccountById(@PathVariable("accountId") String accountId,
                                                             @RequestParam(value = "consent-id", required = true) String consentId) {
        return accountService.getAccountByConsentId(accountId, consentId)
                   .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                   .orElse(new ResponseEntity<>(HttpStatus.FORBIDDEN));
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @PutMapping(path = "/")
    public ResponseEntity createAccount(HttpServletRequest request,
                                        @RequestBody SpiAccountDetails account) throws Exception {
        String uriString = getUriString(request);
        SpiAccountDetails saved = accountService.addOrUpdateAccount(account);
        return ResponseEntity.created(new URI(uriString + saved.getId())).build();
    }

    private String getUriString(HttpServletRequest request) {
        return UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build().toUriString();
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @DeleteMapping(path = "/{accountId}")
    public ResponseEntity deleteAccount(@PathVariable("accountId") String accountId) {
        if (accountService.deleteAccountById(accountId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/{accountId}/balances")
    public ResponseEntity<List<SpiBalances>> readBalancesById(@PathVariable("accountId") String accountId) {
        return accountService.getBalances(accountId)
                   .map(ResponseEntity::ok)
                   .orElse(new ResponseEntity<>(HttpStatus.FORBIDDEN));
    }
}
