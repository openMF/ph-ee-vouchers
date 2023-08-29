package org.mifos.pheevouchermanagementsystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mifos.pheevouchermanagementsystem.data.RedeemVoucherRequestDTO;
import org.mifos.pheevouchermanagementsystem.data.RedeemVoucherResponseDTO;
import org.mifos.pheevouchermanagementsystem.domain.Voucher;
import org.mifos.pheevouchermanagementsystem.exception.VoucherNotFoundException;
import org.mifos.pheevouchermanagementsystem.repository.VoucherRepository;
import org.mifos.pheevouchermanagementsystem.zeebe.ZeebeProcessStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mifos.pheevouchermanagementsystem.util.EncryptVoucher.hashVoucherNumber;
import static org.mifos.pheevouchermanagementsystem.util.RedemptionStatusEnum.FAILURE;
import static org.mifos.pheevouchermanagementsystem.util.RedemptionStatusEnum.SUCCESS;
import static org.mifos.pheevouchermanagementsystem.util.UniqueIDGenerator.generateUniqueNumber;
import static org.mifos.pheevouchermanagementsystem.util.VoucherStatusEnum.ACTIVE;
import static org.mifos.pheevouchermanagementsystem.util.VoucherStatusEnum.UTILIZED;
import static org.mifos.pheevouchermanagementsystem.zeebe.ZeebeVariables.TENANT_ID;

@Service
public class RedeemVoucherService {
    private final VoucherRepository voucherRepository;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(RedeemVoucherService.class);
    @Autowired
    ZeebeProcessStarter zeebeProcessStarter;

    @Autowired
    public RedeemVoucherService(VoucherRepository voucherRepository, ObjectMapper objectMapper){
        this.voucherRepository = voucherRepository;
        this.objectMapper = objectMapper;
    }

    public RedeemVoucherResponseDTO redeemVoucher(RedeemVoucherRequestDTO redeemVoucherRequestDTO, String registeringInstitutionId){
        String transactionId =  generateUniqueNumber(10);
        Voucher voucher = null;
        try {
            voucher = voucherRepository.findBySerialNo(redeemVoucherRequestDTO.getVoucherSerialNumber()).orElseThrow(() -> VoucherNotFoundException.voucherNotFound(redeemVoucherRequestDTO.getVoucherSerialNumber()));
            //Boolean validate = validateVoucher(voucher);
            if(voucher.getStatus().equals(ACTIVE.getValue()) && voucher.getExpiryDate().toLocalDate().isAfter(LocalDate.now())
                    && voucher.getVoucherNo().equals(hashVoucherNumber(redeemVoucherRequestDTO.getVoucherSecretNumber())) && voucher.getRegisteringInstitutionId().equals(registeringInstitutionId)) {
                voucher.setStatus(UTILIZED.getValue());
                voucherRepository.save(voucher);

            } else {
                return new RedeemVoucherResponseDTO(FAILURE.getValue(), "Voucher Details Invalid!", redeemVoucherRequestDTO.getVoucherSerialNumber(), voucher.getAmount().toString(), LocalDateTime.now().toString(), transactionId);            }
            }catch (RuntimeException  e) {
            logger.error(e.getMessage());
            return new RedeemVoucherResponseDTO(FAILURE.getValue(), "Voucher Not Found!", redeemVoucherRequestDTO.getVoucherSerialNumber(), "", LocalDateTime.now().toString(),transactionId);
            //sendCallbackService.sendCallback("Requested resource not found!");
        }
        return new RedeemVoucherResponseDTO(SUCCESS.getValue(), "Voucher redemption successful!", redeemVoucherRequestDTO.getVoucherSerialNumber(), voucher.getAmount().toString(), LocalDateTime.now().toString(), transactionId);
    }

    @Async("asyncExecutor")
    public void redeemAndPay(RedeemVoucherRequestDTO redeemVoucherRequestDTO, String callbackURL, String registeringInstitutionId){
        try {
            Voucher voucher = voucherRepository.findBySerialNo(redeemVoucherRequestDTO.getVoucherSerialNumber()).orElseThrow(() -> VoucherNotFoundException.voucherNotFound(redeemVoucherRequestDTO.getVoucherSerialNumber()));
            //Boolean validate = validateVoucher(voucher);
            if (voucher.getStatus().equals(ACTIVE.getValue()) && voucher.getExpiryDate().toLocalDate().isAfter(LocalDate.now())
                    && voucher.getVoucherNo().equals(hashVoucherNumber(redeemVoucherRequestDTO.getVoucherSecretNumber())) && voucher.getRegisteringInstitutionId().equals(registeringInstitutionId)) {
                voucher.setStatus(UTILIZED.getValue());
                voucherRepository.save(voucher);

            }
            Map<String, Object> extraVariables = new HashMap<>();
            extraVariables.put("payeeIdentity", voucher.getPayeeFunctionalId());
            //extraVariables.put("paymentModality", redeemVoucherRequestDTO.getPaymentModality());
            extraVariables.put("registeringInstitutionId", registeringInstitutionId);
            extraVariables.put("callbackURL", callbackURL);
            extraVariables.put("amount", voucher.getAmount());
            extraVariables.put("currency", voucher.getCurrency());
            extraVariables.put("voucherSerialNumber", redeemVoucherRequestDTO.getVoucherSerialNumber());
            zeebeProcessStarter.startZeebeWorkflow("redeem_and_pay_voucher", null, extraVariables);
        }catch (RuntimeException e){
            logger.error(e.getMessage());
        }
    }
}
