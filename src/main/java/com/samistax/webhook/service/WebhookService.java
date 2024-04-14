package com.samistax.webhook.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class WebhookService {
    private final CopyOnWriteArrayList<String> webhooks = new CopyOnWriteArrayList<>();
    private final RestTemplate restTemplate;

    public WebhookService() {
        this.restTemplate = new RestTemplate();
    }

    public void registerWebhook(String webhookUrl) {
        webhooks.add(webhookUrl);
    }
    public void unregisterWebhook(String webhookUrl) {
        webhooks.remove(webhookUrl);
    }

    public void triggerEvent(String jsonPayload) {
        webhooks.forEach(webhookUrl -> sendPostRequest(webhookUrl, jsonPayload));
    }

    private void sendPostRequest(String url, String payload) {
        try {
            System.out.println("Posting to : " +url) ;
            restTemplate.postForObject(url, payload, String.class);
        } catch (Exception e) {
            System.out.println("Failed to send webhook POST request: " + e.getMessage());
        }
    }
}
