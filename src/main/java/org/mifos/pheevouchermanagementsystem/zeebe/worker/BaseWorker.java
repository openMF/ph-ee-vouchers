package org.mifos.pheevouchermanagementsystem.zeebe.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import javax.annotation.PostConstruct;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public abstract class BaseWorker {

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${zeebe.client.evenly-allocated-max-jobs}")
    private int workerMaxJobs;

    @Autowired
    protected CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public abstract void setup();

    public void newWorker(Worker worker, JobHandler handler) {
        zeebeClient.newWorker().jobType(worker.getValue()).handler(handler).name(worker.getValue()).maxJobsActive(workerMaxJobs).open();
    }

}
