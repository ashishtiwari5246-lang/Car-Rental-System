package com.shiva.carrentalapplication.config;

import com.shiva.carrentalapplication.entity.Car;

import com.shiva.carrentalapplication.entity.User;
import com.shiva.carrentalapplication.repository.CarRepository;
import com.shiva.carrentalapplication.repository.UserRepository;
import com.shiva.carrentalapplication.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CarRepository carRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedCars();
    }

    private void seedAdmin() {
        if (!userRepository.existsByEmail("admin@carrental.com")) {
            userRepository.save(User.builder()
                    .name("Admin User")
                    .email("admin@carrental.com")
                    .password("Admin@123")   // plain text — no Spring Security
                    .phone("9999999999")
                    .role(User.Role.ADMIN)
                    .build());
        }
        System.out.println("✓ Admin ready → admin@carrental.com / Admin@123");
    }

    private void seedCars() {
        if (carRepository.count() == 0) {
            carRepository.saveAll(List.of(
                    Car.builder().brand("Maruti").model("Swift").year(2023)
                            .category(Car.Category.ECONOMY).pricePerDay(new BigDecimal("999"))
                            .licensePlate("MP04AB1234").color("White").seats(5)
                            .imageUrl("https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=400")
                            .description("Fuel-efficient city car, perfect for daily commutes").build(),

                    Car.builder().brand("Hyundai").model("Creta").year(2023)
                            .category(Car.Category.SUV).pricePerDay(new BigDecimal("2499"))
                            .licensePlate("MP04CD5678").color("Black").seats(5)
                            .imageUrl("https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=400")
                            .description("Spacious SUV with premium features").build(),

                    Car.builder().brand("Honda").model("City").year(2022)
                            .category(Car.Category.COMPACT).pricePerDay(new BigDecimal("1499"))
                            .licensePlate("MP04EF9012").color("Silver").seats(5)
                            .imageUrl("https://images.unsplash.com/photo-1555215695-3004980ad54e?w=400")
                            .description("Reliable sedan for city and highway travel").build(),

                    Car.builder().brand("Mercedes").model("E-Class").year(2023)
                            .category(Car.Category.LUXURY).pricePerDay(new BigDecimal("8999"))
                            .licensePlate("MP04GH3456").color("Midnight Blue").seats(5)
                            .imageUrl("https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=400")
                            .description("Luxury executive sedan with premium interiors").build(),

                    Car.builder().brand("Toyota").model("Innova Crysta").year(2023)
                            .category(Car.Category.VAN).pricePerDay(new BigDecimal("3499"))
                            .licensePlate("MP04IJ7890").color("Pearl White").seats(7)
                            .imageUrl("https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?w=400")
                            .description("7-seater MPV ideal for family trips").build(),

                    Car.builder().brand("Tata").model("Nexon EV").year(2024)
                            .category(Car.Category.COMPACT).pricePerDay(new BigDecimal("1999"))
                            .licensePlate("MP04KL1122").color("Daytona Grey").seats(5)
                            .imageUrl("https://images.unsplash.com/photo-1593941707882-a5bba14938c7?w=400")
                            .description("Electric SUV with 300km+ range").build()
            ));
            System.out.println("✓ Sample cars seeded");
        }
    }
}
