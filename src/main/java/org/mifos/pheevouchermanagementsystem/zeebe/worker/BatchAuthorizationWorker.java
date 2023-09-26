package org.mifos.pheevouchermanagementsystem.zeebe.worker;

import static org.mifos.pheevouchermanagementsystem.zeebe.ZeebeVariables.CACHED_TRANSACTION_ID;
import static org.mifos.pheevouchermanagementsystem.zeebe.worker.Worker.BATCH_AUTH;

import io.camunda.zeebe.client.ZeebeClient;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import java.math.BigDecimal;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.mifos.pheevouchermanagementsystem.data.AuthorizationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BatchAuthorizationWorker extends BaseWorker {

    @Autowired
    private ZeebeClient zeebeClient;
    @Autowired
    private ProducerTemplate producerTemplate;
    @Autowired
    private CamelContext camelContext;
    @Value("${zeebe.client.evenly-allocated-max-jobs}")
    private int workerMaxJobs;
    @Value("${mock_schema.hostname}")
    private String mockSchemaHostname;
    @Value("${mock_schema.endpoints.batch_auth}")
    private String batchAuthEndpoint;
    @Value("${voucher.hostname}")
    private String voucherHostname;
    @Value("${payer.identifier}")
    private String payerIdentifier;

    @Override
    public void setup() {
        logger.info("## generating " + BATCH_AUTH.getValue() + "zeebe worker");
        zeebeClient.newWorker().jobType(BATCH_AUTH.getValue()).handler((client, job) -> {
            logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
            Map<String, Object> existingVariables = job.getVariablesAsMap();
            existingVariables.put(CACHED_TRANSACTION_ID, job.getKey());
            RequestSpecification requestSpec = new RequestSpecBuilder().build();
            requestSpec.relaxedHTTPSValidation();
            requestSpec.header("X-Client-Correlation-ID", job.getKey());
            requestSpec.header("Content-Type", "application/json");
            requestSpec.header("X-CallbackURL", voucherHostname + "/authorization/callbacks");
            requestSpec.queryParam("command", "authorize");
            batchAuthEndpoint = batchAuthEndpoint + existingVariables.get("batchId").toString();
            AuthorizationRequest authorizationRequest = new AuthorizationRequest();
            authorizationRequest.setBatchId(existingVariables.get("batchId").toString());
            authorizationRequest.setPayerIdentifier(payerIdentifier);
            String totalAmount = existingVariables.get("totalAmount").toString();
            authorizationRequest.setAmount(new BigDecimal(totalAmount));
            authorizationRequest.setCurrency(existingVariables.get("currency").toString());

            String response = RestAssured.given(requestSpec).baseUri(mockSchemaHostname).body(authorizationRequest).expect()
                    .spec(new ResponseSpecBuilder().build()).when().post(batchAuthEndpoint).andReturn().asString();

            client.newCompleteCommand(job.getKey()).variables(existingVariables).send();
        }).name(BATCH_AUTH.getValue()).maxJobsActive(workerMaxJobs).open();
    }
}
