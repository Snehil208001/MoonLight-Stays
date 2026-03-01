package com.moonlight.project.airBnbApp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    @Async // Runs in a background thread
    public void sendBookingConfirmation(String toEmail, String customerName, Long bookingId, String hotelName, String checkIn, String checkOut) {
        log.info("Starting background task to send confirmation email to {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(toEmail);
            message.setSubject("Booking Confirmed! - " + hotelName);

            // Build the email body
            String emailBody = String.format(
                    "Dear %s,\n\n" +
                            "Your payment was successful and your booking is confirmed!\n\n" +
                            "Booking Details:\n" +
                            "----------------\n" +
                            "Booking ID: %d\n" +
                            "Hotel: %s\n" +
                            "Check-In Date: %s\n" +
                            "Check-Out Date: %s\n\n" +
                            "Thank you for choosing GrunZimmer! Have a wonderful stay.",
                    customerName, bookingId, hotelName, checkIn, checkOut
            );

            message.setText(emailBody);

            javaMailSender.send(message);
            log.info("Booking confirmation email sent successfully to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send booking confirmation email to {}. Error: {}", toEmail, e.getMessage());
        }
    }

    // --- NEW: Cancellation and Refund Email ---
    @Async // Runs in a background thread
    public void sendBookingCancellationEmail(String toEmail, String customerName, Long bookingId, String hotelName, boolean isRefunded) {
        log.info("Starting background task to send cancellation email to {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(toEmail);
            message.setSubject("Booking Cancelled - " + hotelName);

            // Dynamically build the refund message based on whether money was returned
            String refundText = isRefunded ?
                    "A full refund has been initiated to your original payment method. It may take 5-10 business days to appear on your statement." :
                    "Since this booking was not yet paid or confirmed, no charges were made.";

            // Build the email body
            String emailBody = String.format(
                    "Dear %s,\n\n" +
                            "Your booking has been successfully cancelled.\n\n" +
                            "Booking Details:\n" +
                            "----------------\n" +
                            "Booking ID: %d\n" +
                            "Hotel: %s\n\n" +
                            "%s\n\n" +
                            "We hope to host you again in the future!\n\n" +
                            "Best regards,\n" +
                            "AirBnb Team",
                    customerName, bookingId, hotelName, refundText
            );

            message.setText(emailBody);

            javaMailSender.send(message);
            log.info("Booking cancellation email sent successfully to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send booking cancellation email to {}. Error: {}", toEmail, e.getMessage());
        }
    }
}