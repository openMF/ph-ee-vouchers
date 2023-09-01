package org.mifos.pheevouchermanagementsystem.api.definition;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.mifos.pheevouchermanagementsystem.data.RequestDTO;
import org.mifos.pheevouchermanagementsystem.data.ResponseDTO;
s
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;
@Tag(name = "GOV")
public interface VoucherLifecycleManagementApi {
    @Operation(
            summary = "Create Vouchers API")
    @PutMapping("/vouchers")
    <T> ResponseEntity<T> voucherStatusChange(@RequestHeader(value="X-CallbackURL") String callbackURL, @RequestHeader(value = "X-Registering-Institution-ID") String registeringInstitutionId,
                                                    @RequestBody Object requestBody, @RequestParam(value = "command") String command) throws ExecutionException, InterruptedException, JsonProcessingException;
}
