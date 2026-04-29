package com.shiva.carrentalapplication.controller;

import com.shiva.carrentalapplication.entity.Car;

import com.shiva.carrentalapplication.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cars")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping
    public List<Car> getAllCars() {
        return carService.getAvailableCars();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Car> getCarById(@PathVariable Long id) {
        return ResponseEntity.ok(carService.getCarById(id));
    }

    @GetMapping("/search")
    public List<Car> searchCars(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Car.Category category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate != null && endDate != null) return carService.searchAvailableCars(startDate, endDate);
        if (keyword != null && !keyword.isBlank())  return carService.searchByKeyword(keyword);
        if (category != null)                        return carService.filterByCategory(category);
        if (minPrice != null && maxPrice != null)    return carService.filterByPriceRange(minPrice, maxPrice);
        return carService.getAvailableCars();
    }

    @PostMapping
    public ResponseEntity<Car> addCar(@RequestBody Car car) {
        return ResponseEntity.ok(carService.addCar(car));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Car> updateCar(@PathVariable Long id, @RequestBody Car car) {
        return ResponseEntity.ok(carService.updateCar(id, car));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<Car> toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(carService.toggleAvailability(id));
    }
}