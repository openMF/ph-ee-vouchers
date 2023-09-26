package org.mifos.pheevouchermanagementsystem.api.definition;

import java.util.concurrent.ExecutionException;
import org.mifos.pheevouchermanagementsystem.data.FetchVoucherResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

public interface FetchVoucherApi {

    @GetMapping("/vouchers/{serialNumber}")
    ResponseEntity<FetchVoucherResponseDTO> fetchVoucher(@PathVariable String serialNumber,
            @RequestHeader(value = "X-Registering-Institution-ID") String registeringInstitutionId)
            throws ExecutionException, InterruptedException;

    @GetMapping("/vouchers")
    ResponseEntity<Page<FetchVoucherResponseDTO>> fetchAllVouchers(
            @RequestHeader(value = "X-Registering-Institution-ID") String registeringInstitutionId,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size)
            throws ExecutionException, InterruptedException;
}
