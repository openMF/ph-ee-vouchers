package org.mifos.pheevouchermanagementsystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mifos.pheevouchermanagementsystem.data.CallbackRequestDTO;
import org.mifos.pheevouchermanagementsystem.data.RequestDTO;
import org.mifos.pheevouchermanagementsystem.data.SuccessfulVouchers;
import org.mifos.pheevouchermanagementsystem.data.VoucherInstruction;
import org.mifos.pheevouchermanagementsystem.domain.ErrorTracking;
import org.mifos.pheevouchermanagementsystem.domain.Voucher;
import org.mifos.pheevouchermanagementsystem.exception.InstructionIdException;
import org.mifos.pheevouchermanagementsystem.repository.ErrorTrackingRepository;
import org.mifos.pheevouchermanagementsystem.repository.VoucherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mifos.pheevouchermanagementsystem.util.EncryptVoucher.hashVoucherNumber;
import static org.mifos.pheevouchermanagementsystem.util.UniqueIDGenerator.generateUniqueNumber;
import static org.mifos.pheevouchermanagementsystem.util.VoucherStatusEnum.INACTIVE;

@Service
public class CreateVoucherService {
    private final VoucherRepository voucherRepository;
    private final ErrorTrackingRepository errorTrackingRepository;
    private final SendCallbackService sendCallbackService;
    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(CreateVoucherService.class);

    @Autowired
    public CreateVoucherService(VoucherRepository voucherRepository, ErrorTrackingRepository errorTrackingRepository, SendCallbackService sendCallbackService,
                                ObjectMapper objectMapper){
        this.voucherRepository = voucherRepository;
        this.errorTrackingRepository = errorTrackingRepository;
        this.sendCallbackService = sendCallbackService;
        this.objectMapper = objectMapper;
    }
    @Async("asyncExecutor")
    public void createVouchers(RequestDTO request, String callbackURL){
        List<VoucherInstruction> voucherInstructionList = request.getVoucherInstructions();
        List<ErrorTracking> errorTrackingsList = new ArrayList<>();
        List<SuccessfulVouchers> successfulVouchers = new ArrayList<>();
        validateAndSaveVoucher(voucherInstructionList, successfulVouchers, errorTrackingsList, request);
        try {
            sendCallbackService.sendCallback(objectMapper.writeValueAsString(new CallbackRequestDTO(request.getRequestID(), request.getBatchID(), successfulVouchers)), callbackURL);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }
    }
    public void validateAndSaveVoucher(List<VoucherInstruction> voucherInstructionList, List<SuccessfulVouchers> successfulVouchers ,List<ErrorTracking> errorTrackingList, RequestDTO request){
        try {
            voucherInstructionList.stream().forEach(voucherInstruction -> {
                String requestID = request.getRequestID();
                Boolean instructionValidation = validateInstruction(voucherInstruction, request, successfulVouchers);
                if (!instructionValidation) {
                    addVouchers(voucherInstruction, successfulVouchers, errorTrackingList, request);
                } else {
                    throw new InstructionIdException(voucherInstruction.getInstructionID());
                }
            });
        }catch (InstructionIdException e){
            logger.error(e.toString());
        }
    }

    public Boolean validateInstruction(VoucherInstruction voucherInstruction, RequestDTO request, List<SuccessfulVouchers> successfulVouchers){
        return voucherRepository.existsByInstructionId(voucherInstruction.getInstructionID());
    }

@Transactional
    public void addVouchers(VoucherInstruction voucherInstruction, List<SuccessfulVouchers> successfulVouchers, List<ErrorTracking> errorTrackingList, RequestDTO request){
        String serialNumber = generateUniqueNumber(14);
        String voucherNumber = hashVoucherNumber(serialNumber,generateUniqueNumber(18));
        Voucher voucher =  new Voucher(serialNumber, voucherNumber,voucherInstruction.getAmount(),voucherInstruction.getCurrency(),voucherInstruction.getGroupCode()
        ,INACTIVE.getValue(),null,LocalDateTime.now(),null, voucherInstruction.getPayeeFunctionalID(), request.getBatchID(),voucherInstruction.getInstructionID(),request.getRequestID());
        try {
            voucherRepository.save(voucher);
            successfulVouchers.add(new SuccessfulVouchers(voucherInstruction.getInstructionID(), voucherInstruction.getCurrency(), voucherInstruction.getAmount()
            ,voucherInstruction.getNarration(),voucherNumber, serialNumber));
        }catch (RuntimeException exception){
            ErrorTracking error = new ErrorTracking(request.getRequestID(), voucherInstruction.getInstructionID(), exception.getMessage());
            errorTrackingList.add(error);
            try {
                errorTrackingRepository.save(error);
            }catch (RuntimeException e){
                logger.error(e.getMessage());
            }
        }
    }
}
