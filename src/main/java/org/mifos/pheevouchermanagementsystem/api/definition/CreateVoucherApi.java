package org.mifos.pheevouchermanagementsystem.api.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.mifos.pheevouchermanagementsystem.data.RequestDTO;
import org.mifos.pheevouchermanagementsystem.data.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.concurrent.ExecutionException;
@Tag(name = "GOV")
public interface CreateVoucherApi {
    @Operation(
            summary = "Create Vouchers API")
    @PostMapping("/vouchers")
    ResponseEntity<ResponseDTO> createVouchers(@RequestHeader(value="X-CallbackURL") String callbackURL,
                                               @RequestHeader(value =  "X-Program-ID", required = false) String programId,
                                               @RequestHeader(value = "X-Registering-Institution-ID") String registeringInstitutionId,
                                               @RequestBody RequestDTO requestBody) throws ExecutionException, InterruptedException, JsonProcessingException;
}
