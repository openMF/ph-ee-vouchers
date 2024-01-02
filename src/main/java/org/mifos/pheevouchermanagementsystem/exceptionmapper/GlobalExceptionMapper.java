package org.mifos.pheevouchermanagementsystem.exceptionmapper;

import org.mifos.pheevouchermanagementsystem.data.ResponseDTO;
import org.mifos.pheevouchermanagementsystem.exception.ZeebeClientStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.mifos.pheevouchermanagementsystem.util.VoucherManagementEnum.BPMN_NOT_FOUND;
import static org.mifos.pheevouchermanagementsystem.util.VoucherManagementEnum.FAILED_RESPONSE;

@RestControllerAdvice
public class GlobalExceptionMapper {

    @ExceptionHandler(ZeebeClientStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseDTO> handleClientStatusException(ZeebeClientStatusException ex) {
        ResponseDTO responseDTO = new ResponseDTO(BPMN_NOT_FOUND.getValue(), BPMN_NOT_FOUND.getMessage(), ex.getId());
        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(responseDTO);
    }
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseDTO> handleException(Exception ex) {
        ResponseDTO responseDTO = new ResponseDTO(FAILED_RESPONSE.getValue(), FAILED_RESPONSE.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
    }
}
