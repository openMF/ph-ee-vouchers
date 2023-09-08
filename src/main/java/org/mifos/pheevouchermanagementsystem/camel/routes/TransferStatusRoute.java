package org.mifos.pheevouchermanagementsystem.camel.routes;

import static org.mifos.pheevouchermanagementsystem.camel.config.CamelProperties.ENDPOINT;
import static org.mifos.pheevouchermanagementsystem.camel.config.CamelProperties.HOST;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
public class TransferStatusRoute extends BaseRouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:direct:send-transfer-query").id("transfer-status").process(exchange -> {
            exchange.getIn().setHeader("Platform-TenantId", exchange.getProperty("tenantId"));
        }).setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader(Exchange.HTTP_QUERY,
                        simple(new StringBuilder().append("size=1").append("page=0")
                                .append("clientCorrelationId=${exchangeProperty.clientCorrelationId}").toString()))
                .setProperty(HOST, simple("{{operations.hostname}}")).setProperty(ENDPOINT, simple("/api/v1/transfers/"))
                .to("direct:external-api-calling");
    }
}
