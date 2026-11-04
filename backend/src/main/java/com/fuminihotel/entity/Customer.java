/**
 * Author: HoangTLT
 */
package com.fuminihotel.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer")
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @Column(nullable = false, length = 100)
    @Nationalized
    private String customerFullName;

    @Column(nullable = false, length = 20)
    private String telephone;

    @Column(nullable = false, unique = true, length = 100)
    private String emailAddress;

    @Column(nullable = false)
    private LocalDate customerBirthday;

    @Column(nullable = false)
    private Integer customerStatus;

    @Column(nullable = false)
    private String password;

    public Customer() {}

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerFullName() { return customerFullName; }
    public void setCustomerFullName(String customerFullName) { this.customerFullName = customerFullName; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public LocalDate getCustomerBirthday() { return customerBirthday; }
    public void setCustomerBirthday(LocalDate customerBirthday) { this.customerBirthday = customerBirthday; }

    public Integer getCustomerStatus() { return customerStatus; }
    public void setCustomerStatus(Integer customerStatus) { this.customerStatus = customerStatus; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

