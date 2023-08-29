package org.mifos.pheevouchermanagementsystem.zeebe.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mifos.pheevouchermanagementsystem.domain.Voucher;
import org.mifos.pheevouchermanagementsystem.exception.VoucherNotFoundException;
import org.mifos.pheevouchermanagementsystem.repository.VoucherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

import static org.mifos.pheevouchermanagementsystem.util.VoucherStatusEnum.ACTIVE;

@Component
public class CheckTransferStatusWorker {
    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private ProducerTemplate producerTemplate;
    @Autowired
    private CamelContext camelContext;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${operations.hostname}")
    private String operationHostname;
    @Value("${operations.endpoints.transfers}")
    public String transfersEndpoint;

    @Autowired
    private VoucherRepository voucherRepository;

    private static final Logger logger = LoggerFactory.getLogger(CheckTransferStatusWorker.class);

    @PostConstruct
    public void setup() {
        zeebeClient.newWorker().jobType("check-transfer-status").handler((client, job) -> {
            logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());

            Map<String, Object> existingVariables = job.getVariablesAsMap();

            RequestSpecification requestSpec = new RequestSpecBuilder().build();
            requestSpec.relaxedHTTPSValidation();
            requestSpec.header("Platform-TenantId", existingVariables.get("tenantId").toString());
            requestSpec.header("Content-Type", "application/json");
            requestSpec.queryParam("clientCorrelationId", "\"" + existingVariables.get("clientCorrelationId").toString() + "\"");
            requestSpec.queryParam("size", "1");


            String response = RestAssured.given(requestSpec).baseUri(operationHostname).expect()
                    .spec(new ResponseSpecBuilder().expectStatusCode(200).build()).when().get(transfersEndpoint)
                    .andReturn().asString();

            JSONObject responseJson = new JSONObject(response);
            String status = null;
            if (responseJson.has("content")) {
                JSONArray contentArray = responseJson.getJSONArray("content");
                if (contentArray.length() > 0) {
                    JSONObject contentObject = contentArray.getJSONObject(0);
                    if (contentObject.has("status")) {
                        status = contentObject.getString("status");
                    }
                }
            }
            if(status != null) {
                if (!status.equals("COMPLETED")) {
                    try {
                        Voucher voucher = voucherRepository.findBySerialNo(existingVariables.get("voucherSerialNumber").toString()).orElseThrow(() -> VoucherNotFoundException.voucherNotFound(existingVariables.get("voucherSerialNumber").toString()));
                        voucher.setStatus(ACTIVE.getValue());
                        voucherRepository.save(voucher);
                    } catch (RuntimeException e) {
                        logger.error(e.getMessage());
                    }
                }
            }

            client.newCompleteCommand(job.getKey()).variables(existingVariables).send();
        }).name("check-transfer-status").open();
    }
}
