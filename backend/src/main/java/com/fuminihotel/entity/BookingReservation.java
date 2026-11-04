/**
 * Author: HoangTLT
 */
package com.fuminihotel.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "booking_reservation")
public class BookingReservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingReservationId;

    @Column(nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus bookingStatus;

    @OneToMany(mappedBy = "bookingReservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingDetail> bookingDetails = new ArrayList<>();

    public BookingReservation() {}

    public Long getBookingReservationId() { return bookingReservationId; }
    public void setBookingReservationId(Long bookingReservationId) { this.bookingReservationId = bookingReservationId; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }

    public List<BookingDetail> getBookingDetails() { return bookingDetails; }
    public void setBookingDetails(List<BookingDetail> bookingDetails) { this.bookingDetails = bookingDetails; }
}

