package org.mifos.pheevouchermanagementsystem.api.implementation;

import java.util.concurrent.ExecutionException;
import org.mifos.pheevouchermanagementsystem.api.definition.VoucherStatusApi;
import org.mifos.pheevouchermanagementsystem.data.FetchVoucherResponseDTO;
import org.mifos.pheevouchermanagementsystem.data.VoucherStatusResponseDTO;
import org.mifos.pheevouchermanagementsystem.repository.VoucherRepository;
import org.mifos.pheevouchermanagementsystem.service.FetchVoucherService;
import org.mifos.pheevouchermanagementsystem.service.ValidateHeaders;
import org.mifos.pheevouchermanagementsystem.util.HeaderConstants;
import org.mifos.pheevouchermanagementsystem.validator.HeaderValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VoucherStatusApiController implements VoucherStatusApi {

    @Autowired
    private FetchVoucherService fetchVoucherService;
    @Autowired
    private final VoucherRepository voucherRepository;

    public VoucherStatusApiController(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @Override
    @ValidateHeaders(requiredHeaders = {
            HeaderConstants.X_REGISTERING_INSTITUTION_ID }, validatorClass = HeaderValidator.class, validationFunction = "validateForRegisteringInstitutionID")
    public ResponseEntity<VoucherStatusResponseDTO> voucherStatus(String serialNumber, String registeringInstitutionId, String status)
            throws ExecutionException, InterruptedException {
        if (registeringInstitutionId == null || registeringInstitutionId.isEmpty() || !voucherRepository.existsBySerialNo(serialNumber)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        FetchVoucherResponseDTO fetchVoucherResponseDTO = fetchVoucherService.fetchVoucher(serialNumber, registeringInstitutionId);
        VoucherStatusResponseDTO voucherStatusResponseDTO = new VoucherStatusResponseDTO();
        voucherStatusResponseDTO.setSerialNumber(fetchVoucherResponseDTO.getSerialNumber());
        voucherStatusResponseDTO.setStatus(fetchVoucherResponseDTO.getStatus());
        return ResponseEntity.status(HttpStatus.OK).body(voucherStatusResponseDTO);
    }

}
