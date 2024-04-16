package org.mifos.pheevouchermanagementsystem.api.implementation;

import static org.mifos.pheevouchermanagementsystem.util.VoucherManagementEnum.FAILED_RESPONSE;
import static org.mifos.pheevouchermanagementsystem.util.VoucherManagementEnum.SUCCESS_RESPONSE;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.mifos.connector.common.channel.dto.PhErrorDTO;
import org.mifos.pheevouchermanagementsystem.api.definition.CreateVoucherApi;
import org.mifos.pheevouchermanagementsystem.data.RequestDTO;
import org.mifos.pheevouchermanagementsystem.data.ResponseDTO;
import org.mifos.pheevouchermanagementsystem.exception.ConflictingDataException;
import org.mifos.pheevouchermanagementsystem.exception.InstructionIdException;
import org.mifos.pheevouchermanagementsystem.service.CreateVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class CreateVoucherApiController implements CreateVoucherApi {

    @Autowired
    CreateVoucherService createVoucherService;

    @Override
    public <T> ResponseEntity<T> createVouchers(String callbackURL, String programId, String registeringInstitutionId,
            RequestDTO requestBody) throws ExecutionException, InterruptedException, JsonProcessingException {
        try {
            PhErrorDTO phErrorDTO = createVoucherService.validateAndCreateVoucher(requestBody, callbackURL, registeringInstitutionId);
            if (phErrorDTO != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((T) phErrorDTO);
            }
        } catch (InstructionIdException | ConflictingDataException | NullPointerException e) {
            throw e;
        } catch (Exception e) {
            ResponseDTO responseDTO = new ResponseDTO(FAILED_RESPONSE.getValue(), e.getMessage(), requestBody.getRequestID());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((T) responseDTO);
        }
        ResponseDTO responseDTO = new ResponseDTO(SUCCESS_RESPONSE.getValue(), SUCCESS_RESPONSE.getMessage(), requestBody.getRequestID());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body((T) responseDTO);
    }
}
