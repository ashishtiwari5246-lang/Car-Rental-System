package com.shiva.carrentalapplication.service;




import com.shiva.carrentalapplication.entity.Booking;
import com.shiva.carrentalapplication.entity.Car;
import com.shiva.carrentalapplication.entity.User;
import com.shiva.carrentalapplication.repository.BookingRepository;
import com.shiva.carrentalapplication.repository.CarRepository;
import com.shiva.carrentalapplication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;

    @Transactional
    public Booking createBooking(Long userId, Long carId,
                                 LocalDate startDate, LocalDate endDate,
                                 String pickupLocation, String dropoffLocation) {

        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        if (!car.isAvailable()) {
            throw new IllegalStateException("Car is not available for booking");
        }

        // Check for conflicting bookings
        List<Car> availableCars = carRepository.findAvailableCars(startDate, endDate);
        boolean isAvailable = availableCars.stream().anyMatch(c -> c.getId().equals(carId));
        if (!isAvailable) {
            throw new IllegalStateException("Car is already booked for the selected dates");
        }

        Booking booking = Booking.builder()
                .user(user)
                .car(car)
                .startDate(startDate)
                .endDate(endDate)
                .pickupLocation(pickupLocation)
                .dropoffLocation(dropoffLocation)
                .status(Booking.BookingStatus.PENDING)
                .build();

        booking.calculateTotalPrice();
        return bookingRepository.save(booking);
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, Long userId) {
        Booking booking = getBookingById(bookingId);

        if (!booking.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to cancel this booking");
        }
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }
        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed booking");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking updateStatus(Long bookingId, Booking.BookingStatus newStatus) {
        Booking booking = getBookingById(bookingId);
        booking.setStatus(newStatus);
        return bookingRepository.save(booking);
    }
}
