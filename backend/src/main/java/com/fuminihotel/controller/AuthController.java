/**
 * Author: HoangTLT
 */
package com.fuminihotel.controller;

import com.fuminihotel.dto.*;
import com.fuminihotel.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    private final CustomerService customers;

    public AuthController(AuthService auth, CustomerService customers) {
        this.auth = auth;
        this.customers = customers;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse register(@Valid @RequestBody CustomerRequest request) { return customers.create(request, true); }
}
