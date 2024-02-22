package org.mifos.pheevouchermanagementsystem.zeebe.worker;

import static org.mifos.pheevouchermanagementsystem.util.VoucherStatusEnum.ACTIVE;
import static org.mifos.pheevouchermanagementsystem.zeebe.ZeebeVariables.MAX_RETRY;
import static org.mifos.pheevouchermanagementsystem.zeebe.ZeebeVariables.RETRY;
import static org.mifos.pheevouchermanagementsystem.zeebe.ZeebeVariables.THRESHOLD_DELAY;
import static org.mifos.pheevouchermanagementsystem.zeebe.ZeebeVariables.TRANSACTION_COMPLETED;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mifos.pheevouchermanagementsystem.data.RedeemVoucherResponseDTO;
import org.mifos.pheevouchermanagementsystem.domain.Voucher;
import org.mifos.pheevouchermanagementsystem.exception.VoucherNotFoundException;
import org.mifos.pheevouchermanagementsystem.repository.VoucherRepository;
import org.mifos.pheevouchermanagementsystem.service.SendCallbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    @Value("${maxRetry}")
    private int maxRetry;
    @Value("${thresholdDelay}")
    private String thresholdDelay;

    @Autowired
    private SendCallbackService sendCallbackService;

    @Autowired
    private VoucherRepository voucherRepository;

    private static final Logger logger = LoggerFactory.getLogger(CheckTransferStatusWorker.class);

    @PostConstruct
    public void setup() {
        zeebeClient.newWorker().jobType("check-transfer-status").handler((client, job) -> {
            logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());

            Map<String, Object> existingVariables = job.getVariablesAsMap();

            existingVariables.put(THRESHOLD_DELAY, thresholdDelay);
            existingVariables.put(MAX_RETRY, maxRetry);
            existingVariables.put(TRANSACTION_COMPLETED, false);

            int retry = existingVariables.getOrDefault(RETRY, 0).equals(existingVariables.get(MAX_RETRY)) ? 0
                    : (int) existingVariables.getOrDefault(RETRY, 0);
            if (retry <= maxRetry) {
                logger.info("No of retry {} of max retry count {}", retry, maxRetry);
                RequestSpecification requestSpec = new RequestSpecBuilder().build();
                requestSpec.relaxedHTTPSValidation();
                requestSpec.header("Platform-TenantId", existingVariables.get("partyLookupFspId").toString());
                requestSpec.header("Content-Type", "application/json");
                String clientCorrelationId = existingVariables.get("clientCorrelationId").toString();

                String response = RestAssured.given(requestSpec).baseUri(operationHostname).expect()
                        .spec(new ResponseSpecBuilder().expectStatusCode(200).build()).when()
                        .get(transfersEndpoint + "&clientCorrelationId=" + clientCorrelationId).andReturn().asString();

                JSONObject responseJson = new JSONObject(response);
                String status = null;
                if (responseJson.has("content")) {
                    JSONArray contentArray = responseJson.getJSONArray("content");
                    logger.info("content length {}", contentArray.length());
                    if (contentArray.length() > 0) {
                        JSONObject contentObject = contentArray.getJSONObject(0);
                        logger.info("content object {}", contentObject.has("status"));
                        if (contentObject.has("status")) {
                            status = contentObject.getString("status");
                        }
                    } else {
                        retry++;
                        existingVariables.put(RETRY, retry);
                    }
                }
                logger.info("Status: ", status);
                if (status != null) {
                    if (!status.equals("COMPLETED")) {
                        try {
                            Voucher voucher = voucherRepository.findBySerialNo(existingVariables.get("voucherSerialNumber").toString())
                                    .orElseThrow(() -> VoucherNotFoundException
                                            .voucherNotFound(existingVariables.get("voucherSerialNumber").toString()));
                            voucher.setStatus(ACTIVE.getValue());
                            existingVariables.put(TRANSACTION_COMPLETED, false);
                            logger.info("Updating voucher status as ACTIVE redemption FAILED");
                            voucherRepository.save(voucher);
                        } catch (RuntimeException e) {
                            logger.error(e.getMessage());
                        }
                    }
                    if (status.equals("COMPLETED")) {
                        existingVariables.put(TRANSACTION_COMPLETED, true);
                        RedeemVoucherResponseDTO redeemVoucherResponseDTO = new RedeemVoucherResponseDTO(status,
                                "Voucher redemption successful", existingVariables.get("voucherSerialNumber").toString(), null,
                                LocalDateTime.now(ZoneId.systemDefault()).toString(), existingVariables.get("transactionId").toString());
                        ObjectMapper objectMapper = new ObjectMapper();
                        String body = objectMapper.writeValueAsString(redeemVoucherResponseDTO);
                        logger.info("Sending callback on URL: {}", existingVariables.get("callbackURL"));
                        sendCallbackService.sendCallback(body, existingVariables.get("callbackURL").toString());
                    }
                }
                logger.info("Updating status zeebe variable");

            }

            client.newCompleteCommand(job.getKey()).variables(existingVariables).send();
        }).name("check-transfer-status").open();
    }
}
