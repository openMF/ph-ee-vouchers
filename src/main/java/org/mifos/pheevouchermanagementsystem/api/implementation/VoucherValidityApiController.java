package org.mifos.pheevouchermanagementsystem.api.implementation;

import org.mifos.pheevouchermanagementsystem.api.definition.VoucherValidityApi;
import org.mifos.pheevouchermanagementsystem.data.ResponseDTO;
import org.mifos.pheevouchermanagementsystem.service.VoucherValidityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

import static org.mifos.pheevouchermanagementsystem.util.VoucherManagementEnum.*;

@RestController
public class VoucherValidityApiController implements VoucherValidityApi {
    @Autowired
    VoucherValidityService voucherValidityService;
    @Override
    public ResponseEntity<ResponseDTO> voucherValidity(String callbackURL, String serialNumber, String voucherNumber, String groupCode, Boolean isValid) throws ExecutionException, InterruptedException {
        try{
        voucherValidityService.getVoucherValidity(serialNumber, voucherNumber, groupCode, callbackURL);
        }catch (Exception e) {
            ResponseDTO responseDTO = new ResponseDTO(FAILED_RESPONSE_CODE.getValue(), FAILED_RESPONSE_MESSAGE.getValue(), "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
        }
        ResponseDTO responseDTO = new ResponseDTO(SUCCESS_RESPONSE_CODE.getValue(), SUCCESS_RESPONSE_MESSAGE.getValue(), "");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseDTO);
    }
}
