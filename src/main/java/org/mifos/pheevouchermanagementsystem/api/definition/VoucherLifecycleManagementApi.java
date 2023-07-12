package org.mifos.pheevouchermanagementsystem.api.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.mifos.pheevouchermanagementsystem.data.RequestDTO;
import org.mifos.pheevouchermanagementsystem.data.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

public interface VoucherLifecycleManagementApi {
    @PutMapping("/vouchers")
    ResponseDTO voucherStatusChange(@RequestHeader(value="X-CallbackURL") String callbackURL,
                                    @RequestBody RequestDTO requestBody, @RequestParam(value = "command") String command) throws ExecutionException, InterruptedException, JsonProcessingException;
}
