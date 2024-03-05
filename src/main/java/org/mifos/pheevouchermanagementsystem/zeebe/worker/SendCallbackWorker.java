package org.mifos.pheevouchermanagementsystem.zeebe.worker;

import static org.mifos.pheevouchermanagementsystem.util.RedemptionStatusEnum.SUCCESS;
import static org.mifos.pheevouchermanagementsystem.zeebe.ZeebeVariables.PAYMENT_ADVICE;
import static org.mifos.pheevouchermanagementsystem.zeebe.worker.Worker.VOUCHER_STATUS_SEND_CALLBACK;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.mifos.pheevouchermanagementsystem.data.RedeemVoucherResponseDTO;
import org.mifos.pheevouchermanagementsystem.service.SendCallbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SendCallbackWorker extends BaseWorker {

    @Autowired
    private ZeebeClient zeebeClient;
    @Autowired
    private SendCallbackService sendCallbackService;
    private static final Logger logger = LoggerFactory.getLogger(SendCallbackWorker.class);

    @Override
    public void setup() {
        logger.info("## generating " + VOUCHER_STATUS_SEND_CALLBACK + "zeebe worker");
        zeebeClient.newWorker().jobType(VOUCHER_STATUS_SEND_CALLBACK.getValue()).handler((client, job) -> {
            logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());

            Map<String, Object> existingVariables = job.getVariablesAsMap();
            if (existingVariables.get(PAYMENT_ADVICE).equals(true)) {
                logger.info("Status: {}", existingVariables.get("status").toString());
                RedeemVoucherResponseDTO redeemVoucherResponseDTO = new RedeemVoucherResponseDTO(SUCCESS.getValue(),
                        "Voucher redemption successful", existingVariables.get("voucherSerialNumber").toString(), null,
                        LocalDateTime.now(ZoneId.systemDefault()).toString(), existingVariables.get("transactionId").toString());
                ObjectMapper objectMapper = new ObjectMapper();
                String body = objectMapper.writeValueAsString(redeemVoucherResponseDTO);
                logger.info("Sending callback on URL: {}", existingVariables.get("callbackURL"));
                sendCallbackService.sendCallback(body, existingVariables.get("callbackURL").toString());
            } else {
                RedeemVoucherResponseDTO redeemVoucherResponseDTO = new RedeemVoucherResponseDTO(SUCCESS.getValue(),
                        "Voucher redemption successful", existingVariables.get("voucherSerialNumber").toString(), null,
                        LocalDateTime.now(ZoneId.systemDefault()).toString(), existingVariables.get("transactionId").toString());
                ObjectMapper objectMapper = new ObjectMapper();
                String body = objectMapper.writeValueAsString(redeemVoucherResponseDTO);
                logger.info("Sending callback on URL: {}", existingVariables.get("callbackURL"));
                sendCallbackService.sendCallback(body, existingVariables.get("callbackURL").toString());
            }

            client.newCompleteCommand(job.getKey()).variables(existingVariables).send();
        }).name(VOUCHER_STATUS_SEND_CALLBACK.getValue()).open();
    }
}
