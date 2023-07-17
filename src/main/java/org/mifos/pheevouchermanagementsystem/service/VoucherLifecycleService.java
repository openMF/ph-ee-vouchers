package org.mifos.pheevouchermanagementsystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mifos.pheevouchermanagementsystem.data.FailedCaseDTO;
import org.mifos.pheevouchermanagementsystem.data.RequestDTO;
import org.mifos.pheevouchermanagementsystem.data.VoucherInstruction;
import org.mifos.pheevouchermanagementsystem.data.VoucherLifecycleCallbackResponseDTO;
import org.mifos.pheevouchermanagementsystem.domain.Voucher;
import org.mifos.pheevouchermanagementsystem.exception.VoucherNotFoundException;
import org.mifos.pheevouchermanagementsystem.repository.ErrorTrackingRepository;
import org.mifos.pheevouchermanagementsystem.repository.VoucherRepository;
import org.mifos.pheevouchermanagementsystem.util.UniqueIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mifos.pheevouchermanagementsystem.util.VoucherStatusEnum.*;

@Service
public class VoucherLifecycleService {
    private final VoucherRepository voucherRepository;
    private final ErrorTrackingRepository errorTrackingRepository;
    private final SendCallbackService sendCallbackService;
    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(VoucherLifecycleService.class);

    @Autowired
    public VoucherLifecycleService(VoucherRepository voucherRepository, ErrorTrackingRepository errorTrackingRepository, SendCallbackService sendCallbackService,
                                   ObjectMapper objectMapper){
        this.voucherRepository = voucherRepository;
        this.errorTrackingRepository = errorTrackingRepository;
        this.sendCallbackService = sendCallbackService;
        this.objectMapper = objectMapper;
    }

    @Async("asyncExecutor")
    public void suspendVoucher(RequestDTO request, String callbackURL){
        List<VoucherInstruction> voucherInstructionList = request.getVoucherInstructions();
        List<FailedCaseDTO> failedCaseList = new ArrayList<>();
        validateAndSuspendVouchers(voucherInstructionList, request, failedCaseList);
        try {
            sendCallbackService.sendCallback(objectMapper.writeValueAsString(new VoucherLifecycleCallbackResponseDTO(UniqueIDGenerator.generateUniqueNumber(12), request.getRequestID(), failedCaseList.size(), failedCaseList)), callbackURL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    @Async("asyncExecutor")
    public void reactivateVoucher(RequestDTO request, String callbackURL){
        List<VoucherInstruction> voucherInstructionList = request.getVoucherInstructions();
        List<FailedCaseDTO> failedCaseList = new ArrayList<>();
        validateAndSuspendVouchers(voucherInstructionList, request, failedCaseList);
        try {
            sendCallbackService.sendCallback(objectMapper.writeValueAsString(new VoucherLifecycleCallbackResponseDTO(UniqueIDGenerator.generateUniqueNumber(12), request.getRequestID(), failedCaseList.size(), failedCaseList)), callbackURL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    @Async("asyncExecutor")
    public void cancelVoucher(RequestDTO request, String callbackURL){
        List<VoucherInstruction> voucherInstructionList = request.getVoucherInstructions();
        List<FailedCaseDTO> failedCaseList = new ArrayList<>();
        validateAndReactivateVouchers(voucherInstructionList, request, failedCaseList);
        try {
            sendCallbackService.sendCallback(objectMapper.writeValueAsString(new VoucherLifecycleCallbackResponseDTO(UniqueIDGenerator.generateUniqueNumber(12), request.getRequestID(), failedCaseList.size(), failedCaseList)), callbackURL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public void validateAndReactivateVouchers(List<VoucherInstruction> voucherInstructionList, RequestDTO request, List<FailedCaseDTO> failedCaseList){
        voucherInstructionList.stream().forEach(voucherInstruction -> {
            String requestID = request.getRequestID();
            Boolean voucherValidation = validateVoucher(voucherInstruction, request);
            if (voucherValidation) {
                Voucher voucher = voucherRepository.findBySerialNo(voucherInstruction.getSerialNumber()).orElseThrow(() -> VoucherNotFoundException.voucherNotFound(voucherInstruction.getSerialNumber()));
                changeStatus(voucher, ACTIVE.getValue());
            } else {
                FailedCaseDTO failedCase =   new FailedCaseDTO(voucherInstruction.getSerialNumber(), "Voucher validation failed.");
                failedCaseList.add(failedCase);
            }
        });
    }

    public void validateAndSuspendVouchers(List<VoucherInstruction> voucherInstructionList, RequestDTO request, List<FailedCaseDTO> failedCaseList){
        voucherInstructionList.stream().forEach(voucherInstruction -> {
            String requestID = request.getRequestID();
            Boolean voucherValidation = validateVoucher(voucherInstruction, request);
            if (voucherValidation) {
                Voucher voucher = voucherRepository.findBySerialNo(voucherInstruction.getSerialNumber()).orElseThrow(() -> VoucherNotFoundException.voucherNotFound(voucherInstruction.getSerialNumber()));
                changeStatus(voucher, SUSPENDED.getValue());
            } else {
                FailedCaseDTO failedCase =   new FailedCaseDTO(voucherInstruction.getSerialNumber(), "Voucher validation failed.");
                failedCaseList.add(failedCase);
            }
        });
    }

    public void validateAndCancelVouchers(List<VoucherInstruction> voucherInstructionList, RequestDTO request, List<FailedCaseDTO> failedCaseList){
        voucherInstructionList.stream().forEach(voucherInstruction -> {
            String requestID = request.getRequestID();
            Boolean voucherValidation = validateVoucher(voucherInstruction, request);
            if (voucherValidation) {
                Voucher voucher = voucherRepository.findBySerialNo(voucherInstruction.getSerialNumber()).orElseThrow(() -> VoucherNotFoundException.voucherNotFound(voucherInstruction.getSerialNumber()));
                changeStatus(voucher, CANCELLED.getValue());
            } else {
                FailedCaseDTO failedCase =   new FailedCaseDTO(voucherInstruction.getSerialNumber(), "Voucher validation failed.");
                failedCaseList.add(failedCase);
            }
        });
    }

    public Boolean validateVoucher(VoucherInstruction voucherInstruction, RequestDTO request){
        if(voucherRepository.existsBySerialNo(voucherInstruction.getSerialNumber())){
            Voucher voucher = voucherRepository.findBySerialNo(voucherInstruction.getSerialNumber()).orElseThrow(() -> VoucherNotFoundException.voucherNotFound(voucherInstruction.getSerialNumber()));
            return !voucher.getStatus().equals(EXPIRED.getValue()) && !voucher.getStatus().equals(CANCELLED.getValue()) && !voucher.getExpiryDate().toLocalDate().isAfter(LocalDate.now());
        }
        return false;
    }

    public void changeStatus(Voucher voucher, String status){
        voucher.setStatus(status);
        voucherRepository.save(voucher);
    }
}
