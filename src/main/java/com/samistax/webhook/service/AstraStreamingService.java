package com.samistax.webhook.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.apache.pulsar.client.api.*;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AstraStreamingService {

    protected Logger logger = Logger.getLogger(AstraStreamingService.class.getName());

    @Value("${pulsar.service.topic}")
    private String PULSAR_TOPIC_URL;
    @Value("${pulsar.service.url}")
    private String SERVICE_URL;
    @Value("${pulsar.service.token}")
    private String PULSAR_TOKEN;
    private PulsarClient client;
    private static Producer<String> producer;
    private Schema schema = JSONSchema.STRING;


    public AstraStreamingService() {}

    @PostConstruct
    public Producer<String> getProducer() {

        try {
            this.client =  PulsarClient.builder()
                    .serviceUrl(SERVICE_URL)
                    .authentication(AuthenticationFactory.token(PULSAR_TOKEN))
                    .build();

        } catch (PulsarClientException pce) {
            logger.log(Level.INFO,"Pulsar Client exception", pce);
        }

        // Create reusable producer instance to stream chat messages to other consumers
        if ( this.client != null && this.producer == null ) {
            try {
                this.producer = client.newProducer(schema)
                        .topic(PULSAR_TOPIC_URL)
                        .producerName("webhook demo producer")
                        .create();
            } catch (PulsarClientException e) {
                throw new RuntimeException(e);
                }
        }
        return producer;
    }
    @PreDestroy
    public void preDestroy() {
        logger.info("Closing Pulsar connections");
        if ( producer != null ) {
            try {
                producer.close();
            } catch (PulsarClientException e) {
                logger.log(Level.INFO, "Exception while closing Pulsar Producer", e);
            }
        }
        try {
            if (! client.isClosed() ) {
                client.close();
            }
        } catch (PulsarClientException e) {
            logger.log(Level.INFO,"Exception while closing Pulsar Client", e);
        }
    }
    @Async
    public CompletableFuture<MessageId> sendAsynchPulsarMessage(String jsonPayload) {
        // Process the subscription event json payload
        if ( producer != null && jsonPayload != null) {
            // Initiate asynchronous processing sending the payload as is so Astra Streaming source topic
            return producer.sendAsync(jsonPayload);
        }
        return CompletableFuture.completedFuture(null);
    }


}