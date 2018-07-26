package de.adorsys.aspsp.xs2a.integtest.stepdefinitions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@FeatureFileSteps
public class PerPayISteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context context;

    /* see GlobalSteps.java
        @Given("^PSU is logged in$")
    */

    /* see GlobalSteps.java
        @And("^(.*) approach is used$")
    */

    @And("^PSU wants to initiate a recurring payment (.*) using the payment product (.*)$")
    public void loadTestDataRecPay(String fileName, String paymentProduct) throws IOException {
        context.setPaymentProduct(paymentProduct);

        File jsFile = new File("src/test/resources/data-input/pis/recurring/" + fileName);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        TestData<PeriodicPayment> data = objectMapper.readValue(jsFile, new TypeReference<TestData<PeriodicPayment>>() {
        });

        context.setTestData(data);

    }

    @When("^PSU sends the recurring payment initiating request$")
    public void sendPerPaymentInitiatingRequest() {

        HttpHeaders header = new HttpHeaders();
        header.setAll(context.getTestData().getRequest().getHeader());
        header.add("Authorization", "Bearer" + context.getAccessToken());
        header.add("Content-Type", "application/json");

        HttpEntity<PeriodicPayment> entity = new HttpEntity<>(
            (PeriodicPayment) context.getTestData().getRequest().getBody(), header);
        ResponseEntity<HashMap> response = restTemplate.exchange(
            context.getBaseUrl() + "/periodic-payments/" + context.getPaymentProduct(),
            HttpMethod.POST,
            entity,
            HashMap.class);

        context.setResponse(response);
    }

    @Then("^a successful response code and the appropriate recurring payment response data")
    public void checkRespCodeRecPayment() {

        HashMap<String, String> respBody = (HashMap) context.getTestData().getResponse().getBody();
        ResponseEntity<HashMap> resp = context.getResponse();

        HttpStatus compStatus = convertStringToHttpStatusCode(context.getTestData().getResponse().getCode());

        assertThat(resp.getStatusCode(), equalTo(compStatus));
        assertThat(resp.getBody().get("transactionStatus"), equalTo(respBody.get("transactionStatus")));
        assertThat(resp.getBody().get("paymentId"), notNullValue());
    }

    private HttpStatus convertStringToHttpStatusCode(String code) {
        return HttpStatus.valueOf(Integer.valueOf(code));
    }

}