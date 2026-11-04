/**
 * Author: HoangTLT
 */
package com.fuminihotel.config;

import com.fuminihotel.entity.*;
import com.fuminihotel.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
public class DataInitializer implements CommandLineRunner {
    private final RoomTypeRepository types;
    private final RoomRepository rooms;
    private final CustomerRepository customers;
    private final PasswordEncoder encoder;

    public DataInitializer(RoomTypeRepository types, RoomRepository rooms, CustomerRepository customers, PasswordEncoder encoder) {
        this.types = types;
        this.rooms = rooms;
        this.customers = customers;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (types.count() == 0) {
            addType("Single", "Phòng một người");
            addType("Double", "Phòng hai người");
            addType("Family", "Phòng gia đình");
        }
        if (rooms.count() == 0) {
            var availableTypes = types.findAll();
            for (int i = 0; i < availableTypes.size(); i++) {
                RoomInformation room = new RoomInformation();
                room.setRoomNumber(String.valueOf(101 + i));
                room.setRoomDetailDescription("Phòng có điều hòa, Wi-Fi và phòng tắm riêng.");
                room.setRoomMaxCapacity(i == 0 ? 1 : i == 1 ? 2 : 4);
                room.setRoomType(availableTypes.get(i));
                room.setRoomStatus(1);
                room.setRoomPricePerDay(BigDecimal.valueOf(300000L + i * 200000L));
                rooms.save(room);
            }
        }
        if (!customers.existsByEmailAddressIgnoreCase("customer@example.com")) {
            Customer customer = new Customer();
            customer.setCustomerFullName("Nguyễn Văn An");
            customer.setEmailAddress("customer@example.com");
            customer.setTelephone("0901234567");
            customer.setCustomerBirthday(LocalDate.of(2000, 1, 1));
            customer.setCustomerStatus(1);
            customer.setPassword(encoder.encode("Customer@123"));
            customers.save(customer);
        }
    }

    private void addType(String name, String description) {
        RoomType type = new RoomType();
        type.setRoomTypeName(name);
        type.setTypeDescription(description);
        type.setTypeNote("");
        types.save(type);
    }
}
