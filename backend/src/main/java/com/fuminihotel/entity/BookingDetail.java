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
@Table(name = "booking_detail")
public class BookingDetail {
    @EmbeddedId
    private BookingDetailId id = new BookingDetailId();

    @MapsId("bookingReservationId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_reservation_id")
    private BookingReservation bookingReservation;

    @MapsId("roomId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id")
    private RoomInformation roomInformation;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal actualPrice;

    public BookingDetail() {}

    public BookingDetailId getId() { return id; }
    public void setId(BookingDetailId id) { this.id = id; }

    public BookingReservation getBookingReservation() { return bookingReservation; }
    public void setBookingReservation(BookingReservation bookingReservation) { this.bookingReservation = bookingReservation; }

    public RoomInformation getRoomInformation() { return roomInformation; }
    public void setRoomInformation(RoomInformation roomInformation) { this.roomInformation = roomInformation; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public BigDecimal getActualPrice() { return actualPrice; }
    public void setActualPrice(BigDecimal actualPrice) { this.actualPrice = actualPrice; }
}

