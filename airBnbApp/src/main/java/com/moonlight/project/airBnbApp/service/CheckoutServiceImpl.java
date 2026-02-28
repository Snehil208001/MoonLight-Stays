package com.moonlight.project.airBnbApp.service;

import com.moonlight.project.airBnbApp.entity.Booking;
import com.moonlight.project.airBnbApp.entity.User;
import com.moonlight.project.airBnbApp.repository.BookingRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Refund; // ADDED
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.RefundCreateParams; // ADDED
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements  CheckoutService{

    private final BookingRepository bookingRepository;

    @Override
    public String getCheckoutSession(Booking booking, String successUrl, String failureUrl) {
        log.info("Creating session for booking with ID: {}", booking.getId());
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            CustomerCreateParams customerCreateParams = CustomerCreateParams.builder()
                    .setName(user.getName())
                    .setEmail(user.getEmail())
                    .build();

            Customer customer = Customer.create(customerCreateParams);

            SessionCreateParams sessionParams = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                    .setCustomer(customer.getId())
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(failureUrl)
                    .setClientReferenceId(String.valueOf(booking.getId()))
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("inr")
                                                    .setUnitAmount(booking.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(booking.getHotel().getName() +" : "+booking.getRoom().getTypes())
                                                                    .setDescription("Booking ID: "+booking.getId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(sessionParams);


            booking.setPaymentSessionId(session.getId());
            bookingRepository.save(booking);
            log.info("Session created successfully for booking with ID: {}", booking.getId());


            return session.getUrl();


        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    // NEW: Implementing the refund logic
    @Override
    public void refundPayment(String paymentSessionId) {
        try {
            // 1. Retrieve the session from Stripe to get the PaymentIntent ID
            Session session = Session.retrieve(paymentSessionId);
            String paymentIntentId = session.getPaymentIntent();

            if (paymentIntentId == null) {
                throw new RuntimeException("Payment Intent not found. Cannot process refund.");
            }

            // 2. Create the refund using the PaymentIntent
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();

            Refund refund = Refund.create(params);
            log.info("Successfully processed refund for payment intent: {}", paymentIntentId);

        } catch (StripeException e) {
            log.error("Stripe refund failed for session {}: {}", paymentSessionId, e.getMessage());
            throw new RuntimeException("Refund processing failed: " + e.getMessage());
        }
    }
}