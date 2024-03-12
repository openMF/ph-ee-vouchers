package org.mifos.pheevouchermanagementsystem.exceptionmapper;

import static org.mifos.pheevouchermanagementsystem.util.VoucherManagementEnum.CONFLICT_OCCURRED;
import static org.mifos.pheevouchermanagementsystem.util.VoucherManagementEnum.FAILED_RESPONSE;
import static org.mifos.pheevouchermanagementsystem.util.VoucherManagementEnum.PROCESS_DEFINITION_NOT_FOUND;

import org.mifos.pheevouchermanagementsystem.data.ConflictResponseDTO;
import org.mifos.pheevouchermanagementsystem.data.ResponseDTO;
import org.mifos.pheevouchermanagementsystem.exception.InstructionIdException;
import org.mifos.pheevouchermanagementsystem.exception.ZeebeClientStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionMapper {

    @ExceptionHandler(ZeebeClientStatusException.class)
    public ResponseEntity<ResponseDTO> handleClientStatusException(ZeebeClientStatusException ex) {
        ResponseDTO responseDTO = new ResponseDTO(PROCESS_DEFINITION_NOT_FOUND.getValue(), PROCESS_DEFINITION_NOT_FOUND.getMessage(),
                ex.getId());
        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(responseDTO);
    }

    @ExceptionHandler(InstructionIdException.class)
    public ResponseEntity<ConflictResponseDTO> handleInstructionIdException(InstructionIdException ex) {
        ConflictResponseDTO responseDTO = new ConflictResponseDTO(CONFLICT_OCCURRED.getValue(), CONFLICT_OCCURRED.getMessage(),
                "instructionID", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(responseDTO);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO> handleException(Exception ex) {
        ResponseDTO responseDTO = new ResponseDTO(FAILED_RESPONSE.getValue(), FAILED_RESPONSE.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
    }
}
