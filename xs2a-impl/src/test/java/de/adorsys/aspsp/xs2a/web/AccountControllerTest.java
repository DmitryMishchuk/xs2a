package de.adorsys.aspsp.xs2a.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.adorsys.aspsp.xs2a.domain.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.AccountReport;
import de.adorsys.aspsp.xs2a.domain.Balances;
import de.adorsys.aspsp.xs2a.service.AccountService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountControllerTest {
    private final String ACCOUNT_ID = "33333-999999999";
    private final String TRANSACTION_ID = "1234578";

    @Autowired
    private AccountController accountController;
    @MockBean
    private AccountService accountService;

    @Test
    public void getAccounts_withBalance() {
        boolean withBalance = true;
        boolean psuInvolved = false;

        checkAccountResults(withBalance, psuInvolved);
    }

    @Test
    public void getAccounts_noBalances() {
        boolean withBalance = false;
        boolean psuInvolved = false;

        checkAccountResults(withBalance, psuInvolved);
    }

    @Test
    public void getAccounts_withBalanceAndPsuInvolved() {
        boolean withBalance = true;
        boolean psuInvolved = true;

        checkAccountResults(withBalance, psuInvolved);
    }

    @Test
    public void getBalance_withPsuInvolved() {

        //Given:
        boolean psuInvolved = true;
        checkBalanceResults(ACCOUNT_ID, psuInvolved);
    }

    @Test
    public void getBalance_noPsuInvolved() {
        //Given:
        boolean psuInvolved = false;
        checkBalanceResults(ACCOUNT_ID, psuInvolved);
    }

/*
    // TODO Make a AccountServiceTest for it
    @Test(expected = ConstraintViolationException.class)
    public void shouldFail_getBalance_emptyAccountWithBalanceAndPsuInvolved() {
        //Given:
        String accountId = "";
        boolean psuInvolved = true;

        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        Balances expectedResult = accountService.getBalances(accountId, psuInvolved);

        //When:
        ResponseEntity<Balances> actualResponse = accountController.getBalances(accountId, psuInvolved);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        Balances actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(expectedResult);
    }
*/

    @Test
    public void getTransactions_withPeriodAndTransactionIdNoPsuInvolved() {
        //Given:
        Date dateFrom = new Date();
        Date dateTo = new Date();
        boolean psuInvolved = false;

        checkTransactionResults(ACCOUNT_ID, dateFrom, dateTo, TRANSACTION_ID, psuInvolved);
    }

    @Test
    public void getTransactions_onlyByPeriod() {
        //Given:
        Date dateFrom = new Date();
        Date dateTo = new Date();
        String transactionId = "";
        boolean psuInvolved = false;

        checkTransactionResults(ACCOUNT_ID, dateFrom, dateTo, transactionId, psuInvolved);
    }

    // TODO Make a AccountServiceTest for it
/*
    @Test(expected = ConstraintViolationException.class)
    public void shouldFail_getTransactions_noTransactionIdNoPsuInvolved() {
        //Given:
        String transactionId = "";
        boolean psuInvolved = false;

        checkTransactionResults(ACCOUNT_ID, null, null, transactionId, psuInvolved);
    }
*/

/*
    // TODO Make a AccountServiceTest for it
    @Test(expected = ConstraintViolationException.class)
    public void shouldFail_getTransactions_noAccountId() {
        //Given:
        String accountId = "";
        String transactionId = "";
        boolean psuInvolved = false;

        checkTransactionResults(accountId, null, null, transactionId, psuInvolved);
    }
*/

    private void checkTransactionResults(
    String accountId, Date dateFrom, Date dateTo, String transactionId,
    boolean psuInvolved
    ) {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        AccountReport expectedResult = accountService.getAccountReport(accountId, dateFrom, dateTo, transactionId, psuInvolved);

        //When:
        ResponseEntity<AccountReport> actualResponse = accountController.getTransactions(accountId,
        dateFrom,
        dateTo,
        transactionId,
        psuInvolved);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        AccountReport actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private void checkBalanceResults(String accountId, boolean psuInvolved) {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        Balances expectedResult = accountService.getBalances(accountId, psuInvolved);

        //When:
        ResponseEntity<Balances> actualResponse = accountController.getBalances(accountId, psuInvolved);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        Balances actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private void checkAccountResults(boolean withBalance, boolean psuInvolved) {

        //Given:
        AccountDetails accountDetails = new AccountDetails(
        "21fef",
        "DE1234523543",
        null,
        null,
        null,
        null,
        Currency.getInstance("EUR"),
        "name",
        "GIRO",
        null,
        "XE3DDD",
        null,
        null
        );
        List<AccountDetails> accountDetailsList = new ArrayList<>();
        accountDetailsList.add(accountDetails);

        HttpStatus expectedStatusCode = HttpStatus.OK;
        Map<String, List<AccountDetails>> expectedResult = new HashMap<>();
        expectedResult.put("accountList", accountDetailsList);

        when(accountService.getAccountDetailsList(withBalance, psuInvolved))
        .thenReturn(Collections.singletonList(accountDetails));

        //When:
        ResponseEntity<Map<String, List<AccountDetails>>> actualResponse = accountController.getAccounts(withBalance, psuInvolved);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        Map<String, List<AccountDetails>> actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(expectedResult);

    }
}
