/**
 * Author: HoangTLT
 */
package com.fuminihotel.service;

import com.fuminihotel.dto.*;
import com.fuminihotel.entity.Customer;
import com.fuminihotel.exception.ApiException;
import com.fuminihotel.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import java.time.Instant;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {
    private final CustomerRepository customers;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final String staffEmail;
    private final String staffPasswordHash;
    private final long expirationSeconds;

    public AuthService(CustomerRepository customers, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder,
            @Value("${app.staff.email}") String staffEmail,
            @Value("${app.staff.password}") String staffPassword,
            @Value("${app.jwt.expiration-seconds}") long expirationSeconds) {
        this.customers = customers;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.staffEmail = staffEmail;
        this.staffPasswordHash = passwordEncoder.encode(staffPassword);
        this.expirationSeconds = expirationSeconds;
    }

    public AuthResponse login(LoginRequest request) {
        if (staffEmail.equalsIgnoreCase(request.email().trim())
                && passwordEncoder.matches(request.password(), staffPasswordHash)) {
            return issueToken("staff", "STAFF", null, "Staff");
        }
        Customer customer = customers.findByEmailAddressIgnoreCase(request.email().trim())
                .orElseThrow(this::invalidLogin);
        if (customer.getCustomerStatus() != 1 || !passwordEncoder.matches(request.password(), customer.getPassword())) {
            throw invalidLogin();
        }
        return issueToken(customer.getCustomerId().toString(), "CUSTOMER", customer.getCustomerId(),
                customer.getCustomerFullName());
    }

    private ApiException invalidLogin() {
        return new ApiException(UNAUTHORIZED, "Email hoáº·c máº­t kháº©u khÃ´ng Ä‘Ãºng, hoáº·c tÃ i khoáº£n Ä‘Ã£ bá»‹ khÃ³a");
    }

    private AuthResponse issueToken(String subject, String role, Long customerId, String name) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("fuminihotel").subject(subject)
                .issuedAt(now).expiresAt(now.plusSeconds(expirationSeconds)).claim("role", role).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new AuthResponse(token, role, customerId, name);
    }
}
