package org.mifos.pheevouchermanagementsystem.api.definition;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.concurrent.ExecutionException;
import org.mifos.pheevouchermanagementsystem.data.VoucherStatusResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "GOV")
public interface VoucherStatusApi {

    @Operation(summary = "Voucher Status API ")
    @GetMapping("/voucher/{serialnumber}")
    ResponseEntity<VoucherStatusResponseDTO> voucherStatus(@PathVariable(value = "serialnumber") String serialNumber,
            @RequestHeader(value = "X-Registering-Institution-ID") String registeringInstitutionId,
            @RequestParam(value = "fields", defaultValue = "status") String status) throws ExecutionException, InterruptedException;
}
