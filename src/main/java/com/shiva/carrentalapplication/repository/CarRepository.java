package com.shiva.carrentalapplication.repository;


import com.shiva.carrentalapplication.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> findByAvailableTrue();

    List<Car> findByCategoryAndAvailableTrue(Car.Category category);

    @Query("""
        SELECT c FROM Car c
        WHERE c.available = true
        AND c.id NOT IN (
            SELECT b.car.id FROM Booking b
            WHERE b.status NOT IN ('CANCELLED', 'COMPLETED')
            AND b.startDate < :endDate
            AND b.endDate > :startDate
        )
        """)
    List<Car> findAvailableCars(@Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    List<Car> findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(String brand, String model);

    List<Car> findByPricePerDayBetween(BigDecimal min, BigDecimal max);
}