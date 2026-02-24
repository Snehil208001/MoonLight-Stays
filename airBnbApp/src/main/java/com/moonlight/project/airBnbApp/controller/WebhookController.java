package com.moonlight.project.airBnbApp.controller;

import com.moonlight.project.airBnbApp.service.BookingService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final BookingService bookingService;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping("/payment")
    public ResponseEntity<String> capturePayments(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        try {
            // Verify the event came from Stripe
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            log.info("Received Stripe Webhook Event: {}", event.getType());

            // Handle the successful payment event
            if ("checkout.session.completed".equals(event.getType())) {

                Session session = null;

                // Check if the safe deserializer works
                if (event.getDataObjectDeserializer().getObject().isPresent()) {
                    session = (Session) event.getDataObjectDeserializer().getObject().get();
                } else {
                    // Fallback to unsafe deserializer if there's an API version mismatch.
                    // This can throw EventDataObjectDeserializationException, which is
                    // gracefully caught by the catch (Exception e) block below.
                    session = (Session) event.getDataObjectDeserializer().deserializeUnsafe();
                }

                if (session != null) {
                    log.info("Successfully extracted Session ID: {}", session.getId());
                    bookingService.capturePayment(session.getId());
                } else {
                    log.error("Failed to deserialize the Stripe Session object.");
                }
            }

            return ResponseEntity.ok("Success");

        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe signature", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("Error processing Stripe webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
    }
}