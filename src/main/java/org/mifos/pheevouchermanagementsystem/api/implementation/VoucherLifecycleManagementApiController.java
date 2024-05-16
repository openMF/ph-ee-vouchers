package org.mifos.pheevouchermanagementsystem.api.implementation;

import static org.mifos.pheevouchermanagementsystem.util.VoucherManagementEnum.FAILED_RESPONSE;
import static org.mifos.pheevouchermanagementsystem.util.VoucherManagementEnum.SUCCESS_RESPONSE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.mifos.connector.common.channel.dto.PhErrorDTO;
import org.mifos.pheevouchermanagementsystem.api.definition.VoucherLifecycleManagementApi;
import org.mifos.pheevouchermanagementsystem.data.RedeemVoucherRequestDTO;
import org.mifos.pheevouchermanagementsystem.data.RequestDTO;
import org.mifos.pheevouchermanagementsystem.data.ResponseDTO;
import org.mifos.pheevouchermanagementsystem.data.VoucherValidator;
import org.mifos.pheevouchermanagementsystem.service.ActivateVoucherService;
import org.mifos.pheevouchermanagementsystem.service.RedeemVoucherService;
import org.mifos.pheevouchermanagementsystem.service.ValidateHeaders;
import org.mifos.pheevouchermanagementsystem.service.VoucherLifecycleService;
import org.mifos.pheevouchermanagementsystem.util.HeaderConstants;
import org.mifos.pheevouchermanagementsystem.validator.HeaderValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class VoucherLifecycleManagementApiController implements VoucherLifecycleManagementApi {

    @Autowired
    ActivateVoucherService activateVoucherService;
    @Autowired
    RedeemVoucherService redeemVoucherService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    VoucherLifecycleService voucherLifecycleService;

    @Autowired
    VoucherValidator voucherValidator;

    @Override
    @ValidateHeaders(requiredHeaders = { HeaderConstants.X_PROGRAM_ID, HeaderConstants.X_REGISTERING_INSTITUTION_ID,
            HeaderConstants.X_CALLBACKURL }, validatorClass = HeaderValidator.class, validationFunction = "validateVoucherLifecycle")
    public <T> ResponseEntity<T> voucherStatusChange(String callbackURL, String registeringInstitutionId, String programId,
            Object requestBody, String command) {
        RequestDTO requestDTO = null;
        RedeemVoucherRequestDTO redeemVoucherRequestDTO = null;
        try {
            if (command.equals("activate")) {
                requestDTO = objectMapper.convertValue(requestBody, RequestDTO.class);
                PhErrorDTO phErrorDTO = voucherValidator.validateVoucherLifecycle(requestDTO);
                if (phErrorDTO != null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((T) phErrorDTO);
                }
                activateVoucherService.activateVouchers(requestDTO, callbackURL, registeringInstitutionId);
            } else if (command.equals("suspend")) {
                requestDTO = objectMapper.convertValue(requestBody, RequestDTO.class);
                PhErrorDTO phErrorDTO = voucherValidator.validateVoucherLifecycle(requestDTO);
                if (phErrorDTO != null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((T) phErrorDTO);
                }
                voucherLifecycleService.suspendVoucher(requestDTO, callbackURL, registeringInstitutionId);
            } else if (command.equals("reactivate")) {
                requestDTO = objectMapper.convertValue(requestBody, RequestDTO.class);
                PhErrorDTO phErrorDTO = voucherValidator.validateVoucherLifecycle(requestDTO);
                if (phErrorDTO != null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((T) phErrorDTO);
                }
                voucherLifecycleService.reactivateVoucher(requestDTO, callbackURL, registeringInstitutionId);
            } else if (command.equals("redeemPay")) {
                redeemVoucherRequestDTO = objectMapper.convertValue(requestBody, RedeemVoucherRequestDTO.class);

                PhErrorDTO phErrorDTO = voucherValidator.validateRedeemVoucher(redeemVoucherRequestDTO);
                if (phErrorDTO != null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((T) phErrorDTO);
                }
                redeemVoucherService.redeemAndPay(redeemVoucherRequestDTO, callbackURL, registeringInstitutionId);
            }
        } catch (NullPointerException e) {
            Map<String, String> responseBody = new HashMap<>();
            log.error("An error occurred : {}", e.getMessage());
            responseBody.put("failureReason", "An error occurred, contact the system admin !!");
            return (ResponseEntity<T>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        } catch (Exception e) {
            ResponseDTO responseDTO = new ResponseDTO(FAILED_RESPONSE.getValue(), FAILED_RESPONSE.getMessage(), requestDTO.getRequestID());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((T) responseDTO);
        }
        String requestId;
        if (requestDTO == null) {
            requestId = redeemVoucherRequestDTO.getRequestId();
        } else {
            requestId = requestDTO.getRequestID();
        }
        ResponseDTO responseDTO = new ResponseDTO(SUCCESS_RESPONSE.getValue(), SUCCESS_RESPONSE.getMessage(), requestId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body((T) responseDTO);

    }
}
