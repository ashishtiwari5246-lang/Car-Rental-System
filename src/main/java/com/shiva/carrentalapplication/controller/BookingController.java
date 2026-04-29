package com.shiva.carrentalapplication.controller;
import com.shiva.carrentalapplication.entity.Booking;
import com.shiva.carrentalapplication.repository.UserRepository;
import com.shiva.carrentalapplication.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> body) {
        try {
            Long userId   = Long.parseLong(body.get("userId").toString());
            Long carId    = Long.parseLong(body.get("carId").toString());
            LocalDate startDate = LocalDate.parse(body.get("startDate").toString());
            LocalDate endDate   = LocalDate.parse(body.get("endDate").toString());
            String pickup  = (String) body.getOrDefault("pickupLocation", "");
            String dropoff = (String) body.getOrDefault("dropoffLocation", "");

            Booking booking = bookingService.createBooking(userId, carId, startDate, endDate, pickup, dropoff);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getMyBookings(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id,
                                           @RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.parseLong(body.get("userId").toString());
            return ResponseEntity.ok(bookingService.cancelBooking(id, userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestBody Map<String, String> body) {
        try {
            Booking.BookingStatus status = Booking.BookingStatus.valueOf(body.get("status"));
            return ResponseEntity.ok(bookingService.updateStatus(id, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}