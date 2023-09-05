package org.mifos.pheevouchermanagementsystem.api.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mifos.pheevouchermanagementsystem.api.definition.VoucherLifecycleManagementApi;
import org.mifos.pheevouchermanagementsystem.data.RedeemVoucherRequestDTO;
import org.mifos.pheevouchermanagementsystem.data.RequestDTO;
import org.mifos.pheevouchermanagementsystem.data.ResponseDTO;
import org.mifos.pheevouchermanagementsystem.service.ActivateVoucherService;
import org.mifos.pheevouchermanagementsystem.service.RedeemVoucherService;
import org.mifos.pheevouchermanagementsystem.service.VoucherLifecycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

import static org.mifos.pheevouchermanagementsystem.util.VoucherManagementEnum.*;

@RestController
public class   VoucherLifecycleManagementApiController implements VoucherLifecycleManagementApi {
    @Autowired
    ActivateVoucherService activateVoucherService;
    @Autowired
    RedeemVoucherService redeemVoucherService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    VoucherLifecycleService voucherLifecycleService;
    @Override
    public <T> ResponseEntity<T> voucherStatusChange(String callbackURL, String registeringInstitutionId, String programId, Object requestBody, String command) throws ExecutionException, InterruptedException, JsonProcessingException {
        RequestDTO requestDTO = null;
        RedeemVoucherRequestDTO redeemVoucherRequestDTO = null;
        try {
            if(command.equals("activate")){
                requestDTO = objectMapper.convertValue(requestBody, RequestDTO.class);
                activateVoucherService.activateVouchers(requestDTO, callbackURL, registeringInstitutionId);
            }
            else if(command.equals("redeem")){
                redeemVoucherRequestDTO = objectMapper.convertValue(requestBody, RedeemVoucherRequestDTO.class);
                return ResponseEntity.status(HttpStatus.OK).body((T) redeemVoucherService.redeemVoucher(redeemVoucherRequestDTO, registeringInstitutionId));
            }
            else if(command.equals("suspend")){
                requestDTO = objectMapper.convertValue(requestBody, RequestDTO.class);
                voucherLifecycleService.suspendVoucher(requestDTO, callbackURL, registeringInstitutionId);
            }
            else if(command.equals("reactivate")){
                requestDTO = objectMapper.convertValue(requestBody, RequestDTO.class);
                voucherLifecycleService.reactivateVoucher(requestDTO, callbackURL, registeringInstitutionId);
            }
            else if(command.equals("cancel")){
                requestDTO = objectMapper.convertValue(requestBody, RequestDTO.class);
                voucherLifecycleService.cancelVoucher(requestDTO, callbackURL, registeringInstitutionId);
            }
            else if(command.equals("redeemPay")){
                redeemVoucherRequestDTO = objectMapper.convertValue(requestBody, RedeemVoucherRequestDTO.class);
                redeemVoucherService.redeemAndPay(redeemVoucherRequestDTO, callbackURL, registeringInstitutionId);
            }
        } catch (Exception e) {
            ResponseDTO responseDTO = new ResponseDTO(FAILED_RESPONSE.getValue(), FAILED_RESPONSE.getMessage(), requestDTO.getRequestID());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((T) responseDTO);
        }
        String requestId;
        if(requestDTO == null){
            requestId = redeemVoucherRequestDTO.getRequestId();
        }else {
            requestId = requestDTO.getRequestID();
        }
        ResponseDTO responseDTO = new ResponseDTO(SUCCESS_RESPONSE.getValue(), SUCCESS_RESPONSE.getMessage(), requestId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body((T) responseDTO);

    }
}
