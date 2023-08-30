package org.mifos.pheevouchermanagementsystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mifos.pheevouchermanagementsystem.data.*;
import org.mifos.pheevouchermanagementsystem.domain.ErrorTracking;
import org.mifos.pheevouchermanagementsystem.domain.Voucher;
import org.mifos.pheevouchermanagementsystem.exception.VoucherNotFoundException;
import org.mifos.pheevouchermanagementsystem.repository.ErrorTrackingRepository;
import org.mifos.pheevouchermanagementsystem.repository.VoucherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mifos.pheevouchermanagementsystem.util.EncryptVoucher.hashVoucherNumber;
import static org.mifos.pheevouchermanagementsystem.util.VoucherStatusEnum.ACTIVE;

@Service
public class VoucherValidityService {

    private final VoucherRepository voucherRepository;
    private final ErrorTrackingRepository errorTrackingRepository;
    private final SendCallbackService sendCallbackService;
    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(VoucherValidityService.class);

    @Autowired
    public VoucherValidityService(VoucherRepository voucherRepository, ErrorTrackingRepository errorTrackingRepository, SendCallbackService sendCallbackService,
                                ObjectMapper objectMapper){
        this.voucherRepository = voucherRepository;
        this.errorTrackingRepository = errorTrackingRepository;
        this.sendCallbackService = sendCallbackService;
        this.objectMapper = objectMapper;
    }
    @Async("asyncExecutor")
    public void getVoucherValidity(String serialNumber, String voucherNumber, String groupCode, String callbackURL){
        if (serialNumber != null) {
            checkValidityBySerialNumber(serialNumber, callbackURL);
        } else if (voucherNumber != null && groupCode != null) {
            checkValidityByVoucherNumberAndGroupCode(voucherNumber, groupCode, callbackURL);
        } else {
            sendCallbackService.sendCallback("Requested resource not found!", callbackURL);
        }
    }

    public void checkValidityBySerialNumber(String serialNumber, String callbackURL){
        Voucher voucher =null;
        try {
            voucher = voucherRepository.findBySerialNo(serialNumber).orElseThrow(() -> VoucherNotFoundException.voucherNotFound(serialNumber));
        }catch (RuntimeException e){
            logger.error(e.getMessage());
            sendCallbackService.sendCallback("Requested resource not found!", callbackURL);
            return;
        }
        if( voucher.getStatus().equals(ACTIVE.getValue())) {

            if (voucherRepository.existsBySerialNo(serialNumber) && voucher.getExpiryDate().toLocalDate().isAfter(LocalDate.now())) {
                sendValidityCallback(true, callbackURL, voucher.getSerialNo());
            } else {
                sendValidityCallback(false, callbackURL, voucher.getSerialNo());
            }
        }else {
            sendValidityCallback(false, callbackURL, voucher.getSerialNo());
        }
    }

    public void checkValidityByVoucherNumberAndGroupCode(String voucherNumber, String groupCode, String callbackURL){
        try {
            Voucher voucher = voucherRepository.findByVoucherNo(hashVoucherNumber(voucherNumber)).orElseThrow(() -> VoucherNotFoundException.voucherNotFound(voucherNumber));
            String groupCodeVoucher = voucher.getGroupCode();
            if(voucher.getStatus().equals(ACTIVE.getValue())) {
                if (groupCodeVoucher.equals(groupCode) && voucher.getExpiryDate().toLocalDate().isAfter(LocalDate.now())) {
                    sendValidityCallback(true, callbackURL, voucher.getSerialNo());
                } else {
                    sendValidityCallback(false, callbackURL, voucher.getSerialNo());
                }
            }
            else {
                sendValidityCallback(false, callbackURL, voucher.getSerialNo());
            }
        }catch (RuntimeException  e) {
            logger.error(e.getMessage());
            if (!voucherRepository.existsByVoucherNo(hashVoucherNumber(voucherNumber))) {
                sendCallbackService.sendCallback("Requested resource not found!", callbackURL);
            }
        }
    }

    public void sendValidityCallback(Boolean isValid, String callbackURL, String serialNumber){
        try {
            sendCallbackService.sendCallback(objectMapper.writeValueAsString(new ValidityDTO(serialNumber,
                    isValid)), callbackURL);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }
    }
}
