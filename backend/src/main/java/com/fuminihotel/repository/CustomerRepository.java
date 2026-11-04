/**
 * Author: HoangTLT
 */
package com.fuminihotel.repository;

import com.fuminihotel.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmailAddressIgnoreCase(String email);
    boolean existsByEmailAddressIgnoreCase(String email);
    List<Customer> findByCustomerFullNameContainingIgnoreCaseOrEmailAddressContainingIgnoreCaseOrderByCustomerIdDesc(String name, String email);
}
