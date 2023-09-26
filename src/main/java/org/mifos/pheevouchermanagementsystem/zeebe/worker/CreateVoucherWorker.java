package org.mifos.pheevouchermanagementsystem.zeebe.worker;

import static org.mifos.pheevouchermanagementsystem.zeebe.worker.Worker.CREATE_VOUCHERS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.json.JSONArray;
import org.mifos.pheevouchermanagementsystem.data.CallbackRequestDTO;
import org.mifos.pheevouchermanagementsystem.data.SuccessfulVouchers;
import org.mifos.pheevouchermanagementsystem.data.VoucherInstruction;
import org.mifos.pheevouchermanagementsystem.domain.ErrorTracking;
import org.mifos.pheevouchermanagementsystem.service.CreateVoucherService;
import org.mifos.pheevouchermanagementsystem.service.SendCallbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CreateVoucherWorker extends BaseWorker {

    @Autowired
    private ZeebeClient zeebeClient;
    @Autowired
    private ProducerTemplate producerTemplate;
    @Autowired
    private CamelContext camelContext;
    @Value("${zeebe.client.evenly-allocated-max-jobs}")
    private int workerMaxJobs;
    @Autowired
    private CreateVoucherService createVoucherService;
    @Autowired
    private SendCallbackService sendCallbackService;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void setup() {
        logger.info("## generating " + CREATE_VOUCHERS.getValue() + "zeebe worker");
        zeebeClient.newWorker().jobType(CREATE_VOUCHERS.getValue()).handler((client, job) -> {
            logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
            Map<String, Object> existingVariables = job.getVariablesAsMap();
            String status = existingVariables.get("status").toString();
            String callbackURL = existingVariables.get("callbackURL").toString();

            if (status.equals("Y")) {
                List<ErrorTracking> errorTrackingsList = new ArrayList<>();
                List<SuccessfulVouchers> successfulVouchers = new ArrayList<>();
                List<VoucherInstruction> voucherInstructionList = (List<VoucherInstruction>) existingVariables.get("instructionList");
                String requestId = existingVariables.get("requestId").toString();
                String batchId = existingVariables.get("batchId").toString();
                ObjectMapper mapper = new ObjectMapper();
                String jsonString = new JSONArray(voucherInstructionList).toString();
                List<VoucherInstruction> voucherInstructions = mapper.readValue(jsonString,
                        new TypeReference<List<VoucherInstruction>>() {});

                for (VoucherInstruction voucherInstruction : voucherInstructions) {
                    createVoucherService.addVouchers(voucherInstruction, successfulVouchers, errorTrackingsList, existingVariables.get("registeringInstitutionId").toString(), batchId,
                            requestId);
                }
                try {
                    sendCallbackService.sendCallback(
                            objectMapper.writeValueAsString(new CallbackRequestDTO(requestId, batchId, successfulVouchers)), callbackURL);
                } catch (JsonProcessingException e) {
                    logger.error(e.getMessage());
                }

            } else {
                sendCallbackService.sendCallback("Voucher Creation Failed!", callbackURL);
            }

            client.newCompleteCommand(job.getKey()).variables(existingVariables).send();
        }).name(CREATE_VOUCHERS.getValue()).maxJobsActive(workerMaxJobs).open();
    }
}
