package org.mifos.pheevouchermanagementsystem.service;

import static org.mifos.pheevouchermanagementsystem.util.UniqueIDGenerator.generateUniqueNumber;
import static org.mifos.pheevouchermanagementsystem.util.VoucherStatusEnum.INACTIVE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.transaction.Transactional;
import org.mifos.connector.common.channel.dto.PhErrorDTO;
import org.mifos.connector.common.util.SecurityUtil;
import org.mifos.pheevouchermanagementsystem.data.RequestDTO;
import org.mifos.pheevouchermanagementsystem.data.SuccessfulVouchers;
import org.mifos.pheevouchermanagementsystem.data.VoucherInstruction;
import org.mifos.pheevouchermanagementsystem.data.VoucherValidator;
import org.mifos.pheevouchermanagementsystem.domain.ErrorTracking;
import org.mifos.pheevouchermanagementsystem.domain.Voucher;
import org.mifos.pheevouchermanagementsystem.exception.ConflictingDataException;
import org.mifos.pheevouchermanagementsystem.exception.InstructionIdException;
import org.mifos.pheevouchermanagementsystem.repository.ErrorTrackingRepository;
import org.mifos.pheevouchermanagementsystem.repository.VoucherRepository;
import org.mifos.pheevouchermanagementsystem.util.VouchersDTOConstant;
import org.mifos.pheevouchermanagementsystem.zeebe.ZeebeProcessStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateVoucherService {

    private final VoucherRepository voucherRepository;
    private final ErrorTrackingRepository errorTrackingRepository;
    private final SendCallbackService sendCallbackService;
    private final ObjectMapper objectMapper;
    private final ZeebeProcessStarter zeebeProcessStarter;
    private final VoucherValidator voucherValidator;

    private static final Logger logger = LoggerFactory.getLogger(CreateVoucherService.class);

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    public CreateVoucherService(VoucherRepository voucherRepository, ErrorTrackingRepository errorTrackingRepository,
            SendCallbackService sendCallbackService, ObjectMapper objectMapper, ZeebeProcessStarter zeebeProcessStarter,
            VoucherValidator voucherValidator) {
        this.voucherRepository = voucherRepository;
        this.errorTrackingRepository = errorTrackingRepository;
        this.sendCallbackService = sendCallbackService;
        this.objectMapper = objectMapper;
        this.zeebeProcessStarter = zeebeProcessStarter;
        this.voucherValidator = voucherValidator;
    }

    public PhErrorDTO validateAndCreateVoucher(RequestDTO request, String callbackURL, String registeringInstitutionId) {
        PhErrorDTO phErrorDTO = voucherValidator.validateCreateVoucher(request);
        if (phErrorDTO == null) {
            createVouchers(request, callbackURL, registeringInstitutionId);
        }

        return phErrorDTO;
    }

    public void createVouchers(RequestDTO request, String callbackURL, String registeringInstitutionId) {
        try {
            validateRequestID(request.getRequestID());
            validateBatchID(request.getBatchID());
        } catch (ConflictingDataException e) {
            logger.error(e.getMessage());
            throw e;
        }
        List<VoucherInstruction> voucherInstructionList = request.getVoucherInstructions();
        List<ErrorTracking> errorTrackingsList = new ArrayList<>();
        List<SuccessfulVouchers> successfulVouchers = new ArrayList<>();
        validateAndSaveVoucher(voucherInstructionList, successfulVouchers, errorTrackingsList, request, registeringInstitutionId,
                callbackURL);
        /*
         * try { sendCallbackService.sendCallback(objectMapper.writeValueAsString(new
         * CallbackRequestDTO(request.getRequestID(), request.getBatchID(), successfulVouchers)), callbackURL); } catch
         * (JsonProcessingException e) { logger.error(e.getMessage()); }
         */
    }

    public void validateRequestID(String requestID) {
        if (voucherRepository.existsByRequestId(requestID)) {
            throw new ConflictingDataException(VouchersDTOConstant.requestID, requestID);
        }
    }

    public void validateBatchID(String batchID) {
        if (voucherRepository.existsByBatchId(batchID)) {
            throw new ConflictingDataException(VouchersDTOConstant.batchID, batchID);
        }
    }

    public void validateAndSaveVoucher(List<VoucherInstruction> voucherInstructionList, List<SuccessfulVouchers> successfulVouchers,
            List<ErrorTracking> errorTrackingList, RequestDTO request, String registeringInstitutionId, String callbackURL) {
        final BigDecimal[] totalAmount = { BigDecimal.valueOf(0) };
        String currency = voucherInstructionList.get(0).getCurrency();
        String requestID = request.getRequestID();
        try {
            voucherInstructionList.stream().forEach(voucherInstruction -> {

                Boolean instructionValidation = validateInstruction(voucherInstruction, request, successfulVouchers);
                if (!instructionValidation) {
                    totalAmount[0] = totalAmount[0].add(voucherInstruction.getAmount());
                    // addVouchers(voucherInstruction, successfulVouchers, errorTrackingList, request,
                    // registeringInstitutionId);
                } else {
                    throw new InstructionIdException(voucherInstruction.getInstructionID());
                }
            });
            batchAuthorization(totalAmount[0], currency, request.getBatchID(), voucherInstructionList, requestID, registeringInstitutionId,
                    callbackURL);
        } catch (InstructionIdException e) {
            logger.error("Instruction Id already exist : {}", e.getMessage());
            throw e;
        }
    }

    public void batchAuthorization(BigDecimal amount, String currency, String batchId, List<VoucherInstruction> voucherInstructionList,
            String requestId, String registeringInstitutionId, String callbackURL) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("totalAmount", amount);
        variables.put("currency", currency);
        variables.put("batchId", batchId);
        variables.put("requestId", requestId);
        variables.put("instructionList", voucherInstructionList);
        variables.put("registeringInstitutionId", registeringInstitutionId);
        variables.put("callbackURL", callbackURL);
        zeebeProcessStarter.startZeebeWorkflow("voucher_budget_check", generateUniqueNumber(20), variables);
    }

    public Boolean validateInstruction(VoucherInstruction voucherInstruction, RequestDTO request,
            List<SuccessfulVouchers> successfulVouchers) {
        return voucherRepository.existsByInstructionId(voucherInstruction.getInstructionID());
    }

    @Transactional
    public void addVouchers(VoucherInstruction voucherInstruction, List<SuccessfulVouchers> successfulVouchers,
            List<ErrorTracking> errorTrackingList, String registeringInstitutionId, String batchId, String requestId) {
        String serialNumber = generateUniqueNumber(14);
        String voucherNumber = generateUniqueNumber(18);
        Voucher voucher = new Voucher(serialNumber, SecurityUtil.hash(voucherNumber), voucherInstruction.getAmount(),
                voucherInstruction.getCurrency(), voucherInstruction.getGroupCode(), INACTIVE.getValue(), null, LocalDateTime.now(), null,
                voucherInstruction.getPayeeFunctionalID(), batchId, voucherInstruction.getInstructionID(), requestId,
                registeringInstitutionId);
        try {
            voucherRepository.save(voucher);
            successfulVouchers.add(new SuccessfulVouchers(voucherInstruction.getInstructionID(), voucherInstruction.getCurrency(),
                    voucherInstruction.getAmount(), voucherInstruction.getNarration(), encryptionService.encrypt(voucherNumber),
                    serialNumber));
        } catch (RuntimeException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException
                | InvalidKeySpecException | InvalidKeyException exception) {
            ErrorTracking error = new ErrorTracking(requestId, voucherInstruction.getInstructionID(), exception.getMessage());
            errorTrackingList.add(error);
            try {
                errorTrackingRepository.save(error);
            } catch (RuntimeException e) {
                logger.error(e.getMessage());
            }
        }
    }
}
