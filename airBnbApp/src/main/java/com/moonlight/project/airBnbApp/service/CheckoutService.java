package com.moonlight.project.airBnbApp.service;

import com.moonlight.project.airBnbApp.entity.Booking;

public interface CheckoutService {

    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);

    // NEW: Method to process Stripe Refunds
    void refundPayment(String paymentSessionId);
}