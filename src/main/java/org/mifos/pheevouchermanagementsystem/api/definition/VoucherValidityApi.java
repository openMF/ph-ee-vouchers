package org.mifos.pheevouchermanagementsystem.api.definition;

import org.mifos.pheevouchermanagementsystem.data.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.concurrent.ExecutionException;

public interface VoucherValidityApi {
    @GetMapping("/voucher/validity")
    ResponseEntity<ResponseDTO> voucherValidity(@RequestHeader(value="X-CallbackURL") String callbackURL, @RequestHeader(value = "X-Registering-Institution-ID") String registeringInstitutionId,
                                                @RequestParam(value = "serialNumber", required = false) String serialNumber, @RequestParam(value = "voucherNumber", required = false) String voucherNumber,
                                                @RequestParam(value = "groupCode", required = false) String groupCode, @RequestParam(value = "isValid") Boolean isValid) throws ExecutionException, InterruptedException;
}
