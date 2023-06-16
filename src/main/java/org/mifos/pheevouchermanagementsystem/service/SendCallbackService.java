package org.mifos.pheevouchermanagementsystem.service;

import io.restassured.RestAssured;
import org.springframework.stereotype.Service;


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
