package org.mifos.pheevouchermanagementsystem.service;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.mifos.pheevouchermanagementsystem.data.CallbackRequestDTO;
import org.mifos.pheevouchermanagementsystem.data.FailedCaseDTO;
import org.mifos.pheevouchermanagementsystem.util.UniqueIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class SendCallbackService {
    private static final Logger logger = LoggerFactory.getLogger(SendCallbackService.class);

    public void sendCallback(String body, String callbackURL){
        logger.debug(body);
        logger.debug(callbackURL);
        Response response = RestAssured.given()
                .baseUri(callbackURL)
                .header("Content-Type", ContentType.JSON)
                .body(body)
                .when()
                .put();
        String responseBody = response.getBody().asString();
        logger.info(responseBody);
        int responseCode = response.getStatusCode();
        logger.info(String.valueOf(responseCode));
    }

}
