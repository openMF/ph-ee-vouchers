package org.mifos.pheevouchermanagementsystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mifos.pheevouchermanagementsystem.data.*;
import org.mifos.pheevouchermanagementsystem.domain.Voucher;
import org.mifos.pheevouchermanagementsystem.exception.VoucherNotFoundException;
import org.mifos.pheevouchermanagementsystem.repository.ErrorTrackingRepository;
import org.mifos.pheevouchermanagementsystem.repository.VoucherRepository;
import org.mifos.pheevouchermanagementsystem.util.UniqueIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mifos.pheevouchermanagementsystem.util.VoucherStatusEnum.*;
import static org.mifos.pheevouchermanagementsystem.util.VoucherStatusEnum.CANCELLED;

@Service
public class ActivateVoucherService {
    private final VoucherRepository voucherRepository;
    private final ErrorTrackingRepository errorTrackingRepository;
    private final SendCallbackService sendCallbackService;
    private final ObjectMapper objectMapper;
    @Value("${expiry_time}")
    private Integer expiryTime;

    private static final Logger logger = LoggerFactory.getLogger(ActivateVoucherService.class);

    @Autowired
    public ActivateVoucherService(VoucherRepository voucherRepository, ErrorTrackingRepository errorTrackingRepository, SendCallbackService sendCallbackService,
                                ObjectMapper objectMapper){
        this.voucherRepository = voucherRepository;
        this.errorTrackingRepository = errorTrackingRepository;
        this.sendCallbackService = sendCallbackService;
        this.objectMapper = objectMapper;
    }
    @Async("asyncExecutor")
    public void activateVouchers(RequestDTO request, String callbackURL, String registeringInstitutionId){
        List<VoucherInstruction> voucherInstructionList = request.getVoucherInstructions();
        List<FailedCaseDTO> failedCaseList = new ArrayList<>();
        validateVouchers(voucherInstructionList, request, failedCaseList, registeringInstitutionId);
        try {
            sendCallbackService.sendCallback(objectMapper.writeValueAsString(new VoucherLifecycleCallbackResponseDTO(UniqueIDGenerator.generateUniqueNumber(12), request.getRequestID(), failedCaseList.size(), failedCaseList)), callbackURL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public void validateVouchers(List<VoucherInstruction> voucherInstructionList, RequestDTO request, List<FailedCaseDTO> failedCaseList, String registeringInstitutionId){
        voucherInstructionList.stream().forEach(voucherInstruction -> {
            String requestID = request.getRequestID();
            Boolean voucherValidation = validateVoucher(voucherInstruction, request);
            if (voucherValidation) {
                Voucher voucher = voucherRepository.findBySerialNo(voucherInstruction.getSerialNumber()).orElseThrow(() -> VoucherNotFoundException.voucherNotFound(voucherInstruction.getSerialNumber()));
                if(validateRegisteringInstitutionId(voucher, registeringInstitutionId)) {
                    activateVouchers(voucherInstruction, failedCaseList, request);
                }
                else {
                    FailedCaseDTO failedCase =   new FailedCaseDTO(voucherInstruction.getSerialNumber(), "The voucher provided in the request does not correspond to the same registering ID.");
                    failedCaseList.add(failedCase);
                }
            } else {
                FailedCaseDTO failedCase =   new FailedCaseDTO(voucherInstruction.getSerialNumber(), "Voucher with serial number does not exist.");
                failedCaseList.add(failedCase);
            }
        });
    }

    public Boolean validateVoucher(VoucherInstruction voucherInstruction, RequestDTO request){
        if(voucherRepository.existsBySerialNo(voucherInstruction.getSerialNumber())){
            Voucher voucher = voucherRepository.findBySerialNo(voucherInstruction.getSerialNumber()).orElseThrow(() -> VoucherNotFoundException.voucherNotFound(voucherInstruction.getSerialNumber()));
            return !voucher.getStatus().equals(EXPIRED.getValue()) && !voucher.getStatus().equals(UTILIZED.getValue()) && !voucher.getStatus().equals(CANCELLED.getValue());
        }
        return false;
    }

    @Transactional
    public void activateVouchers(VoucherInstruction voucherInstruction, List<FailedCaseDTO> failedCaseList, RequestDTO request){
        try {
            Voucher voucher = voucherRepository.findBySerialNo(voucherInstruction.getSerialNumber()).orElseThrow(() -> VoucherNotFoundException.voucherNotFound(voucherInstruction.getSerialNumber()));
            if(validateVoucherStatus(voucher, failedCaseList)) {
                voucher.setStatus(ACTIVE.getValue());
                LocalDate updatedDate = LocalDate.now().plusDays(expiryTime);
                voucher.setActivatedDate(LocalDateTime.now());
                voucher.setExpiryDate(java.sql.Date.valueOf(updatedDate));
                voucherRepository.save(voucher);
            }
        }catch (RuntimeException e){
            logger.error(e.getMessage());
        }
    }
    public Boolean validateVoucherStatus(Voucher voucher, List<FailedCaseDTO> failedCaseList){
        if(voucher.getStatus().equals(ACTIVE.getValue())){
            failedCaseList.add(new FailedCaseDTO(voucher.getSerialNo(),"Voucher Already Active."));
            return false;
        }
        return true;
    }
    public static Boolean validateRegisteringInstitutionId(Voucher voucher, String registeringInstitutionId){
        return voucher.getRegisteringInstitutionId().equals(registeringInstitutionId);
    }
}
