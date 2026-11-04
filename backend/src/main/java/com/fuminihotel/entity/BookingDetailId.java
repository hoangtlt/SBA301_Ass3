/**
 * Author: HoangTLT
 */
package com.fuminihotel.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

// BookingDetail has a composite primary key as shown in the assignment diagram.
@Embeddable
public class BookingDetailId implements Serializable {
    private Long bookingReservationId;
    private Long roomId;

    public BookingDetailId() {}

    public BookingDetailId(Long bookingReservationId, Long roomId) {
        this.bookingReservationId = bookingReservationId;
        this.roomId = roomId;
    }

    public Long getBookingReservationId() { return bookingReservationId; }
    public void setBookingReservationId(Long value) { this.bookingReservationId = value; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof BookingDetailId other)) return false;
        return Objects.equals(bookingReservationId, other.bookingReservationId)
                && Objects.equals(roomId, other.roomId);
    }

    @Override
    public int hashCode() { return Objects.hash(bookingReservationId, roomId); }
}
