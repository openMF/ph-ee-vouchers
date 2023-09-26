package org.mifos.pheevouchermanagementsystem.api.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.concurrent.ExecutionException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface BatchAuthorizationCallbackApi {

    @PostMapping("/authorization/callbacks")
    ResponseEntity<Object> accountLookupCallback(@RequestBody String requestBody)
            throws ExecutionException, InterruptedException, JsonProcessingException;
}
