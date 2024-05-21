package org.mifos.pheevouchermanagementsystem.api.implementation;

import java.util.concurrent.ExecutionException;
import org.mifos.pheevouchermanagementsystem.api.definition.FetchVoucherApi;
import org.mifos.pheevouchermanagementsystem.data.FetchVoucherResponseDTO;
import org.mifos.pheevouchermanagementsystem.repository.VoucherRepository;
import org.mifos.pheevouchermanagementsystem.service.FetchVoucherService;
import org.mifos.pheevouchermanagementsystem.service.ValidateHeaders;
import org.mifos.pheevouchermanagementsystem.util.HeaderConstants;
import org.mifos.pheevouchermanagementsystem.validator.HeaderValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FetchVoucherApiController implements FetchVoucherApi {

    @Autowired
    private FetchVoucherService fetchVoucherService;
    @Autowired
    private final VoucherRepository voucherRepository;

    public FetchVoucherApiController(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @Override
    @ValidateHeaders(requiredHeaders = {
            HeaderConstants.X_REGISTERING_INSTITUTION_ID }, validatorClass = HeaderValidator.class, validationFunction = "validateForRegisteringInstitutionID")
    public ResponseEntity<FetchVoucherResponseDTO> fetchVoucher(String serialNumber, String registeringInstitutionId)
            throws ExecutionException, InterruptedException {
        if (registeringInstitutionId == null || registeringInstitutionId.isEmpty() || !voucherRepository.existsBySerialNo(serialNumber)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        FetchVoucherResponseDTO fetchVoucherResponseDTO = fetchVoucherService.fetchVoucher(serialNumber, registeringInstitutionId);
        return ResponseEntity.status(HttpStatus.OK).body(fetchVoucherResponseDTO);
    }

    @Override
    @ValidateHeaders(requiredHeaders = {
            HeaderConstants.X_REGISTERING_INSTITUTION_ID }, validatorClass = HeaderValidator.class, validationFunction = "validateForRegisteringInstitutionID")
    public ResponseEntity<Page<FetchVoucherResponseDTO>> fetchAllVouchers(String registeringInstitutionId, Integer page, Integer size)
            throws ExecutionException, InterruptedException {
        if (registeringInstitutionId == null || registeringInstitutionId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        Page<FetchVoucherResponseDTO> fetchVoucherResponseDTOS = fetchVoucherService.fetchAllVouchers(registeringInstitutionId, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(fetchVoucherResponseDTOS);
    }
}
