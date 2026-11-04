/**
 * Author: HoangTLT
 */
package com.fuminihotel.controller;

import com.fuminihotel.dto.*;
import com.fuminihotel.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {
    private final CustomerService customers;
    private final BookingService bookings;

    public CustomerController(CustomerService customers, BookingService bookings) {
        this.customers = customers;
        this.bookings = bookings;
    }

    @GetMapping("/profile")
    public CustomerResponse profile(@AuthenticationPrincipal Jwt jwt) { return customers.get(Long.valueOf(jwt.getSubject())); }

    @PutMapping("/profile")
    public CustomerResponse updateProfile(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CustomerRequest request) {
        return customers.update(Long.valueOf(jwt.getSubject()), request, true);
    }

    @GetMapping("/bookings")
    public List<BookingResponse> history(@AuthenticationPrincipal Jwt jwt) { return bookings.list(Long.valueOf(jwt.getSubject())); }

    @GetMapping("/bookings/{id}")
    public BookingResponse booking(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) { return bookings.get(id, Long.valueOf(jwt.getSubject())); }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody BookingRequest request) {
        return bookings.create(request, Long.valueOf(jwt.getSubject()));
    }
}
