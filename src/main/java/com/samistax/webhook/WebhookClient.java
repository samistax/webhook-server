package com.samistax.webhook;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Controller
public class WebhookClient {

    public static void main(String[] args) {
        String webhookRegistrationUrl = "http://localhost:8888/subscribe";
        String webhookListenerURL = "http://localhost:8888/myHookListener"; // TEST URL to check callbacks work
        String userName = "user";
        String userPwd = "pass";

        HttpClient client = HttpClient.newHttpClient();

        var userNameAndPassword = Base64.getUrlEncoder().encodeToString((userName + ":" + userPwd).getBytes());
        var authenticationHeader = "Basic " + userNameAndPassword;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookRegistrationUrl))
                .header("Content-Type", "application/json")
                .header(HttpHeaders.AUTHORIZATION, authenticationHeader)
                .POST(HttpRequest.BodyPublishers.ofString(webhookListenerURL))
                .build();

        // Send a request to subscribe to the webhook

        CompletableFuture<HttpResponse<String>> responseFuture = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        // Process the response
        responseFuture.thenAccept(response -> {
            if (response.statusCode() == 200) {

                // Simulate waiting for notifications
                System.out.println("Subscription successful. Response: " + response.body());
            } else {
                System.err.println("Failed to subscribe. Status code: " + response.statusCode());
            }
        }).join(); // Blocking, used for demonstration purposes

        // Pause before sending the next request, if necessary
        try {
            TimeUnit.SECONDS.sleep(1); // Example: wait 10 seconds before the next request
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // In a real-world scenario, you might handle asynchronous operations differently
    }
    @PostMapping("/myHookListener")
    public ResponseEntity<String> myHookListener(@RequestBody String payload) {
        System.out.println("WebhookClient callback triggered:  " + payload);
        return new ResponseEntity<>("Event received by client.", HttpStatus.OK);
    }
}
