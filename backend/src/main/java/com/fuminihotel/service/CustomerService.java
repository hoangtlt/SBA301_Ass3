/**
 * Author: HoangTLT
 */
package com.fuminihotel.service;

import com.fuminihotel.dto.*;
import com.fuminihotel.entity.Customer;
import com.fuminihotel.exception.ApiException;
import com.fuminihotel.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
public class CustomerService {
    private final CustomerRepository customers;
    private final BookingRepository bookings;
    private final PasswordEncoder encoder;
    private final String staffEmail;

    public CustomerService(CustomerRepository customers, BookingRepository bookings, PasswordEncoder encoder,
            @Value("${app.staff.email}") String staffEmail) {
        this.customers = customers;
        this.bookings = bookings;
        this.encoder = encoder;
        this.staffEmail = staffEmail;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> search(String keyword) {
        return customers
                .findByCustomerFullNameContainingIgnoreCaseOrEmailAddressContainingIgnoreCaseOrderByCustomerIdDesc(
                        keyword, keyword)
                .stream().map(ResponseMapper::customer).toList();
    }

    public Customer findEntity(Long id) {
        return customers.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng"));
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(Long id) {
        return ResponseMapper.customer(findEntity(id));
    }

    public CustomerResponse create(CustomerRequest request, boolean selfRegister) {
        Customer customer = new Customer();
        copy(customer, request);
        customer.setCustomerStatus(selfRegister ? 1 : request.customerStatus());
        changePassword(customer, request.password(), true);
        return ResponseMapper.customer(customers.save(customer));
    }

    public CustomerResponse update(Long id, CustomerRequest request, boolean ownProfile) {
        Customer customer = findEntity(id);
        copy(customer, request);
        if (!ownProfile)
            customer.setCustomerStatus(request.customerStatus());
        changePassword(customer, request.password(), false);
        return ResponseMapper.customer(customer);
    }

    private void copy(Customer customer, CustomerRequest request) {
        String email = request.emailAddress().trim().toLowerCase(Locale.ROOT);
        if (email.equalsIgnoreCase(staffEmail))
            throw new ApiException(CONFLICT, "Email nÃ y dÃ nh cho Staff");
        customers.findByEmailAddressIgnoreCase(email).ifPresent(existing -> {
            if (!existing.getCustomerId().equals(customer.getCustomerId())) {
                throw new ApiException(CONFLICT, "Email Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng");
            }
        });
        customer.setCustomerFullName(request.customerFullName().trim());
        customer.setTelephone(request.telephone().trim());
        customer.setEmailAddress(email);
        customer.setCustomerBirthday(request.customerBirthday());
    }

    private void changePassword(Customer customer, String password, boolean required) {
        if (password == null || password.isBlank()) {
            if (required)
                throw new ApiException(BAD_REQUEST, "Cáº§n nháº­p máº­t kháº©u tá»« 8 Ä‘áº¿n 72 kÃ½ tá»±");
            return;
        }
        if (password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(BAD_REQUEST, "Máº­t kháº©u cáº§n Ã­t nháº¥t 8 kÃ½ tá»± vÃ  tá»‘i Ä‘a 72 byte");
        }
        customer.setPassword(encoder.encode(password));
    }

    public void delete(Long id) {
        Customer customer = findEntity(id);
        // Preserve customer references in booking history.
        if (bookings.existsByCustomerCustomerId(id))
            customer.setCustomerStatus(0);
        else
            customers.delete(customer);
    }
}
