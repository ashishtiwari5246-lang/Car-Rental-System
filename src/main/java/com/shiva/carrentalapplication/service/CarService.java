package com.shiva.carrentalapplication.service;



import com.shiva.carrentalapplication.repository.CarRepository;
import com.shiva.carrentalapplication.entity.Car;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public List<Car> getAvailableCars() {
        return carRepository.findByAvailableTrue();
    }

    public List<Car> searchAvailableCars(LocalDate startDate, LocalDate endDate) {
        return carRepository.findAvailableCars(startDate, endDate);
    }

    public List<Car> searchByKeyword(String keyword) {
        return carRepository.findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(
                keyword, keyword);
    }

    public List<Car> filterByCategory(Car.Category category) {
        return carRepository.findByCategoryAndAvailableTrue(category);
    }

    public List<Car> filterByPriceRange(BigDecimal min, BigDecimal max) {
        return carRepository.findByPricePerDayBetween(min, max);
    }

    public Car getCarById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car not found with id: " + id));
    }

    public Car addCar(Car car) {
        return carRepository.save(car);
    }

    public Car updateCar(Long id, Car updated) {
        Car car = getCarById(id);
        car.setBrand(updated.getBrand());
        car.setModel(updated.getModel());
        car.setYear(updated.getYear());
        car.setCategory(updated.getCategory());
        car.setPricePerDay(updated.getPricePerDay());
        car.setAvailable(updated.isAvailable());
        car.setLicensePlate(updated.getLicensePlate());
        car.setColor(updated.getColor());
        car.setSeats(updated.getSeats());
        car.setImageUrl(updated.getImageUrl());
        car.setDescription(updated.getDescription());
        return carRepository.save(car);
    }

    public void deleteCar(Long id) {
        carRepository.deleteById(id);
    }

    public Car toggleAvailability(Long id) {
        Car car = getCarById(id);
        car.setAvailable(!car.isAvailable());
        return carRepository.save(car);
    }
}

