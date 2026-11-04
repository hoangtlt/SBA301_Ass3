/**
 * Author: HoangTLT
 */
package com.fuminihotel.dto;

import com.fuminihotel.entity.BookingStatus;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

public record BookingResponse(Long bookingReservationId, LocalDate bookingDate, BigDecimal totalPrice,
                              Long customerId, String customerFullName, BookingStatus bookingStatus,
                              List<Detail> bookingDetails) {
    public record Detail(Long roomId, String roomNumber, LocalDate startDate,
                         LocalDate endDate, BigDecimal actualPrice) {}
}
