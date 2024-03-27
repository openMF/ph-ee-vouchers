package org.mifos.pheevouchermanagementsystem.api.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "GOV")
public interface CancelAndRedeemVoucherApi {

    @Operation(summary = "Cancel or Redeem Voucher API")
    @PostMapping("/vouchers")
    <T> ResponseEntity<T> cancelOrRedeemVoucher(@RequestHeader(value = "X-CallbackURL") String callbackURL,
            @RequestHeader(value = "X-Registering-Institution-ID") String registeringInstitutionId,
            @RequestHeader(value = "X-Program-ID", required = false) String programId, @RequestBody Object requestBody,
            @RequestParam(value = "command") String command) throws JsonProcessingException;
}
