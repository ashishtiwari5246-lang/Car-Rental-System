package com.shiva.carrentalapplication.service;


import com.shiva.carrentalapplication.entity.Booking;
import com.shiva.carrentalapplication.entity.Payment;
import com.shiva.carrentalapplication.repository.BookingRepository;
import com.shiva.carrentalapplication.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public Payment processPayment(Long bookingId, Payment.PaymentMethod method) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot pay for a cancelled booking");
        }

        // Check if payment already exists
        paymentRepository.findByBookingId(bookingId).ifPresent(p -> {
            if (p.getStatus() == Payment.PaymentStatus.SUCCESS) {
                throw new IllegalStateException("Booking is already paid");
            }
        });

        // Simulate payment gateway — in production, integrate Stripe / Razorpay here
        boolean paymentSuccess = simulatePaymentGateway(method);

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalPrice())
                .paymentMethod(method)
                .status(paymentSuccess ? Payment.PaymentStatus.SUCCESS : Payment.PaymentStatus.FAILED)
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase())
                .paidAt(paymentSuccess ? LocalDateTime.now() : null)
                .build();

        payment = paymentRepository.save(payment);

        // Confirm booking on successful payment
        if (paymentSuccess) {
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
        }

        return payment;
    }

    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("No payment found for booking: " + bookingId));
    }

    @Transactional
    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Only successful payments can be refunded");
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        Booking booking = payment.getBooking();
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        return paymentRepository.save(payment);
    }

    // Simulates a payment gateway — replace with real Razorpay / Stripe SDK call
    private boolean simulatePaymentGateway(Payment.PaymentMethod method) {
        // Always succeeds in dev; add real gateway integration here
        return true;
    }
}

