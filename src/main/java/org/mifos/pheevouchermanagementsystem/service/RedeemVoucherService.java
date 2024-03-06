package org.mifos.pheevouchermanagementsystem.service;

import static org.mifos.pheevouchermanagementsystem.util.RedemptionStatusEnum.FAILURE;
import static org.mifos.pheevouchermanagementsystem.util.RedemptionStatusEnum.SUCCESS;
import static org.mifos.pheevouchermanagementsystem.util.UniqueIDGenerator.generateUniqueNumber;
import static org.mifos.pheevouchermanagementsystem.util.VoucherStatusEnum.ACTIVE;
import static org.mifos.pheevouchermanagementsystem.util.VoucherStatusEnum.UTILIZED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.mifos.connector.common.util.SecurityUtil;
import org.mifos.pheevouchermanagementsystem.data.RedeemVoucherRequestDTO;
import org.mifos.pheevouchermanagementsystem.data.RedeemVoucherResponseDTO;
import org.mifos.pheevouchermanagementsystem.domain.Voucher;
import org.mifos.pheevouchermanagementsystem.exception.VoucherNotFoundException;
import org.mifos.pheevouchermanagementsystem.repository.VoucherRepository;
import org.mifos.pheevouchermanagementsystem.zeebe.ZeebeProcessStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class RedeemVoucherService {

    private final VoucherRepository voucherRepository;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(RedeemVoucherService.class);
    @Autowired
    ZeebeProcessStarter zeebeProcessStarter;

    @Value("${paymentAdvice}")
    Boolean paymentAdvice;

    @Autowired
    EncryptionService encryptionService;
    @Autowired
    SendCallbackService sendCallbackService;

    @Autowired
    public RedeemVoucherService(VoucherRepository voucherRepository, ObjectMapper objectMapper) {
        this.voucherRepository = voucherRepository;
        this.objectMapper = objectMapper;
    }

    public RedeemVoucherResponseDTO redeemVoucher(RedeemVoucherRequestDTO redeemVoucherRequestDTO, String registeringInstitutionId) {
        String transactionId = generateUniqueNumber(10);
        Voucher voucher = null;
        try {
            // find voucher by voucher no / secret code
            /*
             * String encryptedVoucher = "this is hidden voucher"; String voucherNum = decrypt(encryptedVoucher,
             * publicKey) find in db: findBy(hash(voucherNum))
             */
            String voucherNumber = encryptionService.decrypt(redeemVoucherRequestDTO.getVoucherSecretNumber());
            voucher = voucherRepository.findByVoucherNo(SecurityUtil.hash(voucherNumber))
                    .orElseThrow(() -> VoucherNotFoundException.voucherNotFound(redeemVoucherRequestDTO.getVoucherSecretNumber()));
            // Boolean validate = validateVoucher(voucher);
            if (voucher.getStatus().equals(ACTIVE.getValue())
                    && voucher.getExpiryDate().toLocalDate().isAfter(LocalDate.now(ZoneId.systemDefault()))
                    && voucher.getVoucherNo().equals(SecurityUtil.hash(voucherNumber))
                    && voucher.getRegisteringInstitutionId().equals(registeringInstitutionId)) {
                voucher.setStatus(UTILIZED.getValue());
                voucherRepository.save(voucher);

            } else {
                return new RedeemVoucherResponseDTO(FAILURE.getValue(), "Voucher Details Invalid!",
                        redeemVoucherRequestDTO.getVoucherSecretNumber(), voucher.getAmount().toString(),
                        LocalDateTime.now(ZoneId.systemDefault()).toString(), transactionId);
            }
        } catch (RuntimeException e) {
            logger.error(e.getMessage());
            return new RedeemVoucherResponseDTO(FAILURE.getValue(), "Voucher Not Found!", redeemVoucherRequestDTO.getVoucherSecretNumber(),
                    "", LocalDateTime.now(ZoneId.systemDefault()).toString(), transactionId); // sendCallbackService.sendCallback("Requested
                                                                                              // resource not found!");
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException
                | InvalidKeySpecException | InvalidKeyException e) {
            logger.error(e.getMessage());
            return new RedeemVoucherResponseDTO(FAILURE.getValue(), "Voucher number invalid!",
                    redeemVoucherRequestDTO.getVoucherSecretNumber(), "", LocalDateTime.now(ZoneId.systemDefault()).toString(),
                    transactionId);
        }
        return new RedeemVoucherResponseDTO(SUCCESS.getValue(), "Voucher redemption successful!",
                redeemVoucherRequestDTO.getVoucherSecretNumber(), voucher.getAmount().toString(),
                LocalDateTime.now(ZoneId.systemDefault()).toString(), transactionId);
    }

    @Async("asyncExecutor")
    public void redeemAndPay(RedeemVoucherRequestDTO redeemVoucherRequestDTO, String callbackURL, String registeringInstitutionId) {
        try {
            String voucherNumber = encryptionService.decrypt(redeemVoucherRequestDTO.getVoucherSecretNumber());
            Voucher voucher = voucherRepository.findByVoucherNo(SecurityUtil.hash(voucherNumber))
                    .orElseThrow(() -> VoucherNotFoundException.voucherNotFound(redeemVoucherRequestDTO.getVoucherSecretNumber()));
            // Boolean validate = validateVoucher(voucher);
            if (voucher.getStatus().equals(ACTIVE.getValue())
                    && voucher.getExpiryDate().toLocalDate().isAfter(LocalDate.now(ZoneId.systemDefault()))
                    && voucher.getVoucherNo().equals(SecurityUtil.hash(voucherNumber))
                    && voucher.getRegisteringInstitutionId().equals(registeringInstitutionId)) {
                voucher.setStatus(UTILIZED.getValue());
                voucherRepository.save(voucher);
                Map<String, Object> extraVariables = new HashMap<>();
                extraVariables.put("payeeIdentity", voucher.getPayeeFunctionalId());
                // extraVariables.put("paymentModality", redeemVoucherRequestDTO.getPaymentModality());
                extraVariables.put("registeringInstitutionId", registeringInstitutionId);
                extraVariables.put("callbackURL", callbackURL);
                extraVariables.put("amount", voucher.getAmount());
                extraVariables.put("currency", voucher.getCurrency());
                extraVariables.put("voucherSerialNumber", redeemVoucherRequestDTO.getVoucherSecretNumber());
                extraVariables.put("paymentAdvice", paymentAdvice);
                zeebeProcessStarter.startZeebeWorkflow("redeem_and_pay_voucher", null, extraVariables);
            } else {
                RedeemVoucherResponseDTO redeemVoucherResponseDTO = new RedeemVoucherResponseDTO(FAILURE.getValue(),
                        "Voucher number Utilized!", redeemVoucherRequestDTO.getVoucherSecretNumber(), "",
                        LocalDateTime.now(ZoneId.systemDefault()).toString(), null);

                ObjectMapper objectMapper = new ObjectMapper();
                String body = objectMapper.writeValueAsString(redeemVoucherResponseDTO);
                sendCallbackService.sendCallback(body, callbackURL);
            }
        } catch (RuntimeException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException
                | InvalidKeySpecException | InvalidKeyException | JsonProcessingException e) {
            logger.error(e.getMessage());
        }
    }
}
