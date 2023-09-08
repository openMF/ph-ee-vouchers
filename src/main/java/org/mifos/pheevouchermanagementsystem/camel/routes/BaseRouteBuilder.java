package org.mifos.pheevouchermanagementsystem.camel.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseRouteBuilder extends RouteBuilder {

    @Autowired
    public ObjectMapper objectMapper;

    @Autowired
    ZeebeClient zeebeClient;

    public Logger logger = LoggerFactory.getLogger(this.getClass());

    protected enum HttpRequestMethod {

        GET("GET"), POST("POST"), PUT("PUT"), DELETE("DELETE");

        private final String text;

        HttpRequestMethod(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
