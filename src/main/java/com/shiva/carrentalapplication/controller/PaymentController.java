package com.shiva.carrentalapplication.controller;


import com.shiva.carrentalapplication.entity.Payment;
import com.shiva.carrentalapplication.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<?> pay(@RequestBody Map<String, String> body) {
        try {
            Long bookingId = Long.parseLong(body.get("bookingId"));
            Payment.PaymentMethod method = Payment.PaymentMethod.valueOf(body.get("paymentMethod"));
            return ResponseEntity.ok(paymentService.processPayment(bookingId, method));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getByBooking(@PathVariable Long bookingId) {
        try {
            return ResponseEntity.ok(paymentService.getPaymentByBookingId(bookingId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<?> refund(@PathVariable Long paymentId) {
        try {
            return ResponseEntity.ok(paymentService.refundPayment(paymentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}