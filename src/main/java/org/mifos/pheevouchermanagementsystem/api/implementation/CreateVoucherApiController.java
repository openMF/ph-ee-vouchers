package org.mifos.pheevouchermanagementsystem.api.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.mifos.pheevouchermanagementsystem.api.definition.CreateVoucherApi;
import org.mifos.pheevouchermanagementsystem.data.RequestDTO;
import org.mifos.pheevouchermanagementsystem.data.ResponseDTO;
import org.mifos.pheevouchermanagementsystem.service.CreateVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

import static org.mifos.pheevouchermanagementsystem.util.VoucherManagementEnum.*;

@RestController
public class CreateVoucherApiController implements CreateVoucherApi {
    @Autowired
    CreateVoucherService createVoucherService;
    @Override
    public ResponseEntity<ResponseDTO> createVouchers(String callbackURL,String registeringInstitutionId, RequestDTO requestBody) throws ExecutionException, InterruptedException, JsonProcessingException {
        try {
            createVoucherService.createVouchers(requestBody, callbackURL, registeringInstitutionId);
        } catch (Exception e) {
            ResponseDTO responseDTO = new ResponseDTO(FAILED_RESPONSE.getValue(), FAILED_RESPONSE.getValue(), requestBody.getRequestID());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
        }
        ResponseDTO responseDTO = new ResponseDTO(SUCCESS_RESPONSE.getValue(), SUCCESS_RESPONSE.getValue(), requestBody.getRequestID());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseDTO);
        }

}
