package org.mifos.pheevouchermanagementsystem.zeebe.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.restassured.RestAssured;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.mifos.connector.common.channel.dto.TransactionChannelRequestDTO;
import org.mifos.connector.common.mojaloop.dto.PartyIdInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

import static org.mifos.connector.common.zeebe.ZeebeVariables.TRANSACTION_ID;
import static org.mifos.pheevouchermanagementsystem.zeebe.ZeebeVariables.*;
import static org.mifos.pheevouchermanagementsystem.zeebe.worker.Worker.ACCOUNT_LOOKUP;

@Component
public class AccountLookupWorker extends BaseWorker{
    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private ProducerTemplate producerTemplate;
    @Autowired
    private CamelContext camelContext;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${identity-account-mapper.hostname}")
    private String identityMapperURL;
    @Value("${voucher.hostname}")
    private String voucherHostname;
    @Value("${payer.tenant}")
    private String payerTenant;
    @Value("${payer.identifier}")
    private String payerIdentifier;
    @Value("${payer.identifierType}")
    private String payerIdentifierType;
    @Value("${zeebe.client.evenly-allocated-max-jobs}")
    private int workerMaxJobs;
    private static final Logger logger = LoggerFactory.getLogger(AccountLookupWorker.class);

    @Override
    public void setup() {
        logger.info("## generating " + ACCOUNT_LOOKUP + "zeebe worker");
        zeebeClient.newWorker()
                .jobType("payee-account-Lookup-voucher")
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
                    Map<String, Object> existingVariables = job.getVariablesAsMap();
                    existingVariables.put(CACHED_TRANSACTION_ID, job.getKey());
                    existingVariables.put(PAYER_IDENTIFIER, payerIdentifier);
                    existingVariables.put(PAYER_IDENTIFIER_TYPE, payerIdentifierType);

                    existingVariables.put(INITIATOR_FSP_ID, payerTenant);

                    Exchange exchange = new DefaultExchange(camelContext);
                    exchange.setProperty(HOST, identityMapperURL);
                    exchange.setProperty(CALLBACK, voucherHostname + "/accountLookup/Callback");
                    exchange.setProperty(TRANSACTION_ID, existingVariables.get(TRANSACTION_ID));
                    exchange.setProperty(REQUEST_ID, job.getKey());
                    exchange.setProperty(REGISTERING_INSTITUTION_ID, existingVariables.get("registeringInstitutionId").toString());
                    exchange.setProperty(PAYEE_IDENTITY, existingVariables.get("payeeIdentity").toString());
                    //exchange.setProperty("paymentModality", existingVariables.get("paymentModality").toString());
                    producerTemplate.send("direct:send-account-lookup", exchange);

                    client.newCompleteCommand(job.getKey())
                            .variables(existingVariables)
                            .send()
                    ;
                })
                .name("payee-account-Lookup-voucher")
                .maxJobsActive(workerMaxJobs)
                .open();

    }
}
