/**
 * Author: HoangTLT
 */
package com.fuminihotel.controller;

import com.fuminihotel.dto.*;
import com.fuminihotel.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
public class StaffController {
    private final CustomerService customers;
    private final RoomService rooms;
    private final BookingService bookings;

    public StaffController(CustomerService customers, RoomService rooms, BookingService bookings) {
        this.customers = customers;
        this.rooms = rooms;
        this.bookings = bookings;
    }

    @GetMapping("/customers")
    public List<CustomerResponse> customers(@RequestParam(defaultValue = "") String keyword) { return customers.search(keyword); }

    @GetMapping("/customers/{id}")
    public CustomerResponse customer(@PathVariable Long id) { return customers.get(id); }

    @PostMapping("/customers")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest request) { return customers.create(request, false); }

    @PutMapping("/customers/{id}")
    public CustomerResponse updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) { return customers.update(id, request, false); }

    @DeleteMapping("/customers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable Long id) { customers.delete(id); }

    @GetMapping("/rooms")
    public List<RoomResponse> rooms(@RequestParam(defaultValue = "") String keyword) { return rooms.search(keyword, true, null, null); }

    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse createRoom(@Valid @RequestBody RoomRequest request) { return rooms.save(null, request); }

    @PutMapping("/rooms/{id}")
    public RoomResponse updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest request) { return rooms.save(id, request); }

    @DeleteMapping("/rooms/{id}")
    public Map<String, String> deleteRoom(@PathVariable Long id) { return Map.of("message", rooms.delete(id)); }

    @GetMapping("/bookings")
    public List<BookingResponse> bookings() { return bookings.list(null); }

    @GetMapping("/bookings/{id}")
    public BookingResponse booking(@PathVariable Long id) { return bookings.get(id, null); }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@Valid @RequestBody BookingRequest request) { return bookings.create(request, null); }

    @PatchMapping("/bookings/{id}/status")
    public BookingResponse changeStatus(@PathVariable Long id, @Valid @RequestBody BookingStatusRequest request) {
        return bookings.changeStatus(id, request.bookingStatus());
    }

    @PutMapping("/bookings/{id}")
    public BookingResponse updateBooking(@PathVariable Long id, @Valid @RequestBody BookingRequest request) {
        return bookings.update(id, request);
    }

    @DeleteMapping("/bookings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBooking(@PathVariable Long id) { bookings.delete(id); }
}
