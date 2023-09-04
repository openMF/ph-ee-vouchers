package org.mifos.pheevouchermanagementsystem.api.definition;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;
@Tag(name = "GOV")
public interface VoucherLifecycleManagementApi {
    @Operation(
            summary = "Create Vouchers API")
    @PutMapping("/vouchers")
    <T> ResponseEntity<T> voucherStatusChange(@RequestHeader(value="X-CallbackURL") String callbackURL,
                                              @RequestHeader(value = "X-Registering-Institution-ID") String registeringInstitutionId,
                                              @RequestHeader(value =  "X-Program-ID") String programId,
                                              @RequestBody Object requestBody,
                                              @RequestParam(value = "command") String command) throws ExecutionException, InterruptedException, JsonProcessingException, JsonProcessingException;
}
