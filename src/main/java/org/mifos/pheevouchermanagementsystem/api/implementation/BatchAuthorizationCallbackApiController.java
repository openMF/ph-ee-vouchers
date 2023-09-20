package org.mifos.pheevouchermanagementsystem.api.implementation;

import static org.mifos.pheevouchermanagementsystem.zeebe.ZeebeMessages.BATCH_AUTHORIZATION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.mifos.pheevouchermanagementsystem.api.definition.BatchAuthorizationCallbackApi;
import org.mifos.pheevouchermanagementsystem.data.AuthorizationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BatchAuthorizationCallbackApiController implements BatchAuthorizationCallbackApi {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired(required = false)
    private ZeebeClient zeebeClient;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ResponseEntity<Object> accountLookupCallback(String response)
            throws ExecutionException, InterruptedException, JsonProcessingException {
        Map<String, Object> variables = new HashMap<>();
        AuthorizationResponse authorizationResponse = new AuthorizationResponse();
        String transactionId = null;
        try {
            authorizationResponse = objectMapper.readValue(response, AuthorizationResponse.class);
            transactionId = authorizationResponse.getClientCorrelationId();
            variables.put("status", authorizationResponse.getStatus());

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        if (zeebeClient != null) {

            zeebeClient.newPublishMessageCommand().messageName(BATCH_AUTHORIZATION).correlationKey(transactionId)
                    .timeToLive(Duration.ofMillis(50000)).variables(variables).send();
        }
        return ResponseEntity.status(HttpStatus.OK).body("Accepted");
    }
}
