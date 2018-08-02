package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.PaymentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@FeatureFileSteps
public class SinglePaymentSteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<SinglePayments, HashMap, PaymentInitialisationResponse> context;

    @Given("^PSU wants to initiate a single payment (.*) using the payment product (.*)$")
    public void loadTestData(String dataFileName, String paymentProduct) throws IOException {
        context.setPaymentProduct(paymentProduct);

        File jsonFile = new File("src/test/resources/data-input/pis/single/" + dataFileName);

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        TestData<SinglePayments, HashMap> data = mapper.readValue(jsonFile, new TypeReference<TestData<SinglePayments, HashMap>>() {
        });

        context.setTestData(data);
    }

    @When("^PSU sends the single payment initiating request$")
    public void sendPaymentInitiatingRequest() {
        HttpEntity<SinglePayments> entity = PaymentUtils.getPaymentsHttpEntity(context.getTestData().getRequest(), context.getAccessToken());

        ResponseEntity<PaymentInitialisationResponse> response = restTemplate.exchange(
            context.getBaseUrl() + "/payments/" + context.getPaymentProduct(),
            HttpMethod.POST,
            entity,
            PaymentInitialisationResponse.class);

        context.setActualResponse(response);
    }

    @Then("^a successful response code and the appropriate single payment response data$")
    public void checkResponseCode() {
        ResponseEntity<PaymentInitialisationResponse> actualResponse = context.getActualResponse();
        Map givenResponseBody = context.getTestData().getResponse().getBody();

        HttpStatus compareStatus = HttpStatus.valueOf(context.getTestData().getResponse().getCode());
        assertThat(actualResponse.getStatusCode(), equalTo(compareStatus));

        assertThat(actualResponse.getBody().getTransactionStatus().name(), equalTo(givenResponseBody.get("transactionStatus")));
        assertThat(actualResponse.getBody().getPaymentId(), notNullValue());
    }

    @And("^a redirect URL is delivered to the PSU$")
    public void checkRedirectUrl() {
        ResponseEntity<PaymentInitialisationResponse> actualResponse = context.getActualResponse();

        assertThat(actualResponse.getBody().getLinks().getScaRedirect(), notNullValue());
    }

    @When("^PSU sends the single payment initiating request with error$")
    public void sendPaymentInitiatingRequestWithError() throws HttpClientErrorException {
        HttpEntity<SinglePayments> entity = PaymentUtils.getPaymentsHttpEntity(context.getTestData().getRequest(), context.getAccessToken());

        try {
            restTemplate.exchange(
                context.getBaseUrl() + "/payments/" + context.getPaymentProduct(),
                HttpMethod.POST,
                entity,
                HashMap.class);
        } catch (HttpClientErrorException hce) {
            ResponseEntity<PaymentInitialisationResponse> actualResponse = new ResponseEntity<>(hce.getStatusCode());
            context.setActualResponse(actualResponse);
        }
    }

    /*
     * @Then("^an error response code is displayed the appropriate error response$")
     * see GlobalSteps.java
     */

}
