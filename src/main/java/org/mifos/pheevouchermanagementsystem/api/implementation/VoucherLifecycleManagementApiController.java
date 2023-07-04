package org.mifos.pheevouchermanagementsystem.api.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mifos.pheevouchermanagementsystem.api.definition.VoucherLifecycleManagementApi;
import org.mifos.pheevouchermanagementsystem.data.RedeemVoucherRequestDTO;
import org.mifos.pheevouchermanagementsystem.data.RequestDTO;
import org.mifos.pheevouchermanagementsystem.data.ResponseDTO;
import org.mifos.pheevouchermanagementsystem.service.ActivateVoucherService;
import org.mifos.pheevouchermanagementsystem.service.RedeemVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

import static org.mifos.pheevouchermanagementsystem.util.VoucherManagementEnum.*;
import static org.mifos.pheevouchermanagementsystem.util.VoucherManagementEnum.SUCCESS_RESPONSE_MESSAGE;

@RestController
public class VoucherLifecycleManagementApiController implements VoucherLifecycleManagementApi {
    @Autowired
    ActivateVoucherService activateVoucherService;
    @Autowired
    RedeemVoucherService redeemVoucherService;
    @Autowired
    ObjectMapper objectMapper;
    @Override
    public <T> ResponseEntity<T> voucherStatusChange(String callbackURL, Object requestBody, String command) throws ExecutionException, InterruptedException, JsonProcessingException {
        RequestDTO requestDTO = null;
        RedeemVoucherRequestDTO redeemVoucherRequestDTO = null;
        try {
            if(command.equals("activate")){
                requestDTO = objectMapper.convertValue(requestBody, RequestDTO.class);
                activateVoucherService.activateVouchers(requestDTO, callbackURL);
            }
            else if(command.equals("redeem")){
                redeemVoucherRequestDTO = objectMapper.convertValue(requestBody, RedeemVoucherRequestDTO.class);
                return ResponseEntity.status(HttpStatus.OK).body((T) redeemVoucherRequestDTO);
            }
        } catch (Exception e) {
            ResponseDTO responseDTO = new ResponseDTO(FAILED_RESPONSE_CODE.getValue(), FAILED_RESPONSE_MESSAGE.getValue(), requestDTO.getRequestID());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((T) responseDTO);
        }
        ResponseDTO responseDTO = new ResponseDTO(SUCCESS_RESPONSE_CODE.getValue(), SUCCESS_RESPONSE_MESSAGE.getValue(), requestDTO.getRequestID());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body((T) responseDTO);

    }
}
