package org.mifos.pheevouchermanagementsystem.zeebe.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.mifos.pheevouchermanagementsystem.domain.Voucher;
import org.mifos.pheevouchermanagementsystem.exception.VoucherNotFoundException;
import org.mifos.pheevouchermanagementsystem.repository.VoucherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

import static org.mifos.pheevouchermanagementsystem.util.VoucherStatusEnum.ACTIVE;
import static org.mifos.pheevouchermanagementsystem.util.VoucherStatusEnum.UTILIZED;

@Component
public class SendErrorWorker {
    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private ProducerTemplate producerTemplate;
    @Autowired
    private CamelContext camelContext;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private VoucherRepository voucherRepository;

    private static final Logger logger = LoggerFactory.getLogger(SendErrorWorker.class);

    @PostConstruct
    public void setup() {
        zeebeClient.newWorker().jobType("send-failure-voucher").handler((client, job) -> {
            logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
            Map<String, Object> existingVariables = job.getVariablesAsMap();
            try {
                Voucher voucher = voucherRepository.findBySerialNo(existingVariables.get("voucherSerialNumber").toString()).orElseThrow(() -> VoucherNotFoundException.voucherNotFound(existingVariables.get("voucherSerialNumber").toString()));
                voucher.setStatus(ACTIVE.getValue());
                voucherRepository.save(voucher);
            }catch (RuntimeException e){
                logger.error(e.getMessage());
            }
            client.newCompleteCommand(job.getKey()).variables(existingVariables).send();
        }).name("send-failure-voucher").open();
    }
}
