package com.shiva.carrentalapplication.repository;


import com.shiva.carrentalapplication.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByCarId(Long carId);
    List<Booking> findByStatus(Booking.BookingStatus status);
}
