package org.mifos.pheevouchermanagementsystem.service;

import io.restassured.RestAssured;
import org.mifos.pheevouchermanagementsystem.data.CallbackRequestDTO;
import org.mifos.pheevouchermanagementsystem.data.FailedCaseDTO;
import org.mifos.pheevouchermanagementsystem.util.UniqueIDGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class SendCallbackService {
    public void sendCallback(String body, String callbackURL){
        RestAssured.given()
                .baseUri(callbackURL)
                .body(body)
                .when()
                .post();
    }

}
