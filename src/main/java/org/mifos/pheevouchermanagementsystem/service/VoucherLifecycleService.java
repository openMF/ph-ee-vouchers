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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mifos.pheevouchermanagementsystem.service.ActivateVoucherService.validateRegisteringInstitutionId;
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
    public void suspendVoucher(RequestDTO request, String callbackURL, String registeringInstitutionId){
        List<VoucherInstruction> voucherInstructionList = request.getVoucherInstructions();
        List<FailedCaseDTO> failedCaseList = new ArrayList<>();
        validateAndSuspendVouchers(voucherInstructionList, request, failedCaseList, registeringInstitutionId);
        try {
            sendCallbackService.sendCallback(objectMapper.writeValueAsString(new VoucherLifecycleCallbackResponseDTO(UniqueIDGenerator.generateUniqueNumber(12), request.getRequestID(), failedCaseList.size(), failedCaseList)), callbackURL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    @Async("asyncExecutor")
    public void reactivateVoucher(RequestDTO request, String callbackURL, String registeringInstitutionId){
        List<VoucherInstruction> voucherInstructionList = request.getVoucherInstructions();
        List<FailedCaseDTO> failedCaseList = new ArrayList<>();
        validateAndReactivateVouchers(voucherInstructionList, request, failedCaseList, registeringInstitutionId);
        try {
            sendCallbackService.sendCallback(objectMapper.writeValueAsString(new VoucherLifecycleCallbackResponseDTO(UniqueIDGenerator.generateUniqueNumber(12), request.getRequestID(), failedCaseList.size(), failedCaseList)), callbackURL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    @Async("asyncExecutor")
    public void cancelVoucher(RequestDTO request, String callbackURL, String registeringInstitutionId){
        List<VoucherInstruction> voucherInstructionList = request.getVoucherInstructions();
        List<FailedCaseDTO> failedCaseList = new ArrayList<>();
        validateAndCancelVouchers(voucherInstructionList, request, failedCaseList, registeringInstitutionId);
        try {
            sendCallbackService.sendCallback(objectMapper.writeValueAsString(new VoucherLifecycleCallbackResponseDTO(UniqueIDGenerator.generateUniqueNumber(12), request.getRequestID(), failedCaseList.size(), failedCaseList)), callbackURL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public void validateAndReactivateVouchers(List<VoucherInstruction> voucherInstructionList, RequestDTO request, List<FailedCaseDTO> failedCaseList, String registeringInstitutionId){
        voucherInstructionList.stream().forEach(voucherInstruction -> {
            String requestID = request.getRequestID();
            Boolean isValidated = validateVoucherForReactivate(voucherInstruction, request);
            if (isValidated) {
                Voucher voucher = fetchVoucher(voucherInstruction);
                if(validateRegisteringInstitutionId(voucher, registeringInstitutionId)) {
                    changeStatus(voucher, ACTIVE.getValue());                }
                else {
                    FailedCaseDTO failedCase =   new FailedCaseDTO(voucherInstruction.getSerialNumber(), "The voucher provided in the request does not correspond to the same registering ID.");
                    failedCaseList.add(failedCase);
                }
            } else {
                FailedCaseDTO failedCase =   new FailedCaseDTO(voucherInstruction.getSerialNumber(), "Voucher validation failed.");
                failedCaseList.add(failedCase);
            }
        });
    }

    public void validateAndSuspendVouchers(List<VoucherInstruction> voucherInstructionList, RequestDTO request, List<FailedCaseDTO> failedCaseList, String registeringInstitutionId)
    {
        voucherInstructionList.stream().forEach(voucherInstruction -> {
            String requestID = request.getRequestID();
            Boolean isValidated = validateVoucherForSuspend(voucherInstruction, request);
            if (isValidated) {
                Voucher voucher = fetchVoucher(voucherInstruction);
                if(validateRegisteringInstitutionId(voucher, registeringInstitutionId)) {
                    changeStatus(voucher, SUSPENDED.getValue());                }
                else {
                    FailedCaseDTO failedCase =   new FailedCaseDTO(voucherInstruction.getSerialNumber(), "The voucher provided in the request does not correspond to the same registering ID.");
                    failedCaseList.add(failedCase);
                }
            } else {
                FailedCaseDTO failedCase =   new FailedCaseDTO(voucherInstruction.getSerialNumber(), "Voucher validation failed.");
                failedCaseList.add(failedCase);
            }
        });
    }

    public void validateAndCancelVouchers(List<VoucherInstruction> voucherInstructionList, RequestDTO request, List<FailedCaseDTO> failedCaseList, String registeringInstitutionId){
        voucherInstructionList.stream().forEach(voucherInstruction -> {
            String requestID = request.getRequestID();
            Boolean isValidated = validateVoucherForCancel(voucherInstruction, request);
            if (isValidated) {
                Voucher voucher = fetchVoucher(voucherInstruction);
                if(validateRegisteringInstitutionId(voucher, registeringInstitutionId)) {
                    changeStatus(voucher, CANCELLED.getValue());                }
                else {
                    FailedCaseDTO failedCase =   new FailedCaseDTO(voucherInstruction.getSerialNumber(), "The voucher provided in the request does not correspond to the same registering ID.");
                    failedCaseList.add(failedCase);
                }
            } else {
                FailedCaseDTO failedCase =   new FailedCaseDTO(voucherInstruction.getSerialNumber(), "Voucher validation failed.");
                failedCaseList.add(failedCase);
            }
        });
    }

    public Boolean validateVoucherForReactivate(VoucherInstruction voucherInstruction, RequestDTO request){
        if(voucherRepository.existsBySerialNo(voucherInstruction.getSerialNumber())){
            Voucher voucher = fetchVoucher(voucherInstruction);
            return !voucher.getStatus().equals(EXPIRED.getValue()) && !voucher.getStatus().equals(UTILIZED.getValue()) && !voucher.getStatus().equals(CANCELLED.getValue());
        }
        return false;
    }
    public Boolean validateVoucherForCancel(VoucherInstruction voucherInstruction, RequestDTO request){
        if(voucherRepository.existsBySerialNo(voucherInstruction.getSerialNumber())){
            Voucher voucher = fetchVoucher(voucherInstruction);
            return !voucher.getStatus().equals(EXPIRED.getValue()) && !voucher.getStatus().equals(UTILIZED.getValue()) && !voucher.getStatus().equals(CANCELLED.getValue());
        }
        return false;
    }
    public Boolean validateVoucherForSuspend(VoucherInstruction voucherInstruction, RequestDTO request){
        if(voucherRepository.existsBySerialNo(voucherInstruction.getSerialNumber())){
            Voucher voucher = fetchVoucher(voucherInstruction);
            return !voucher.getStatus().equals(EXPIRED.getValue()) && !voucher.getStatus().equals(CANCELLED.getValue()) && !voucher.getStatus().equals(UTILIZED.getValue());
        }
        return false;
    }

    @Transactional
    public void changeStatus(Voucher voucher, String status){
        voucher.setStatus(status);
        try {
            voucherRepository.save(voucher);
        }catch (RuntimeException e){
            logger.error(e.getMessage());
        }
    }

    public Voucher fetchVoucher(VoucherInstruction voucherInstruction){
        return voucherRepository.findBySerialNo(voucherInstruction.getSerialNumber()).orElseThrow(() -> VoucherNotFoundException.voucherNotFound(voucherInstruction.getSerialNumber()));
    }
}
