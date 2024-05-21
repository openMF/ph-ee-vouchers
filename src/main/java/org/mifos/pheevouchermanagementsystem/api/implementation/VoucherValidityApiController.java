package org.mifos.pheevouchermanagementsystem.api.implementation;

import static org.mifos.pheevouchermanagementsystem.util.VoucherManagementEnum.FAILED_RESPONSE;
import static org.mifos.pheevouchermanagementsystem.util.VoucherManagementEnum.SUCCESS_RESPONSE;

import java.util.concurrent.ExecutionException;
import org.mifos.pheevouchermanagementsystem.api.definition.VoucherValidityApi;
import org.mifos.pheevouchermanagementsystem.data.ResponseDTO;
import org.mifos.pheevouchermanagementsystem.service.ValidateHeaders;
import org.mifos.pheevouchermanagementsystem.service.VoucherValidityService;
import org.mifos.pheevouchermanagementsystem.util.HeaderConstants;
import org.mifos.pheevouchermanagementsystem.validator.HeaderValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VoucherValidityApiController implements VoucherValidityApi {

    @Autowired
    VoucherValidityService voucherValidityService;

    @Override
    @ValidateHeaders(requiredHeaders = { HeaderConstants.X_PROGRAM_ID, HeaderConstants.X_REGISTERING_INSTITUTION_ID,
            HeaderConstants.X_CALLBACKURL }, validatorClass = HeaderValidator.class, validationFunction = "validateVoucherLifecycle")
    public ResponseEntity<ResponseDTO> voucherValidity(String callbackURL, String registeringInstitutionId, String serialNumber,
            String voucherNumber, String groupCode, Boolean isValid) throws ExecutionException, InterruptedException {
        try {
            voucherValidityService.getVoucherValidity(serialNumber, voucherNumber, groupCode, callbackURL);
        } catch (Exception e) {
            ResponseDTO responseDTO = new ResponseDTO(FAILED_RESPONSE.getValue(), FAILED_RESPONSE.getMessage(), "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
        }
        ResponseDTO responseDTO = new ResponseDTO(SUCCESS_RESPONSE.getValue(), SUCCESS_RESPONSE.getMessage(), "");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseDTO);
    }
}
