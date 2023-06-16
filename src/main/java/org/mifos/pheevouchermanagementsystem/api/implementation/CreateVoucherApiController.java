package org.mifos.pheevouchermanagementsystem.api.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.mifos.pheevouchermanagementsystem.api.definition.CreateVoucherApi;
import org.mifos.pheevouchermanagementsystem.data.RequestDTO;
import org.mifos.pheevouchermanagementsystem.data.ResponseDTO;
import org.mifos.pheevouchermanagementsystem.service.CreateVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

import static org.mifos.pheevouchermanagementsystem.util.VoucherManagementEnum.*;

@RestController
public class CreateVoucherApiController implements CreateVoucherApi {
    @Autowired
    CreateVoucherService createVoucherService;
    @Override
    public ResponseDTO createVouchers(String callbackURL, RequestDTO requestBody) throws ExecutionException, InterruptedException, JsonProcessingException {
        try {
            createVoucherService.createVouchers(requestBody, callbackURL);
        } catch (Exception e) {
            return new ResponseDTO(FAILED_RESPONSE_CODE.getValue(), FAILED_RESPONSE_MESSAGE.getValue(), requestBody.getRequestID());

        }
        return new ResponseDTO(SUCCESS_RESPONSE_CODE.getValue(), SUCCESS_RESPONSE_MESSAGE.getValue(), requestBody.getRequestID());
    }
}
