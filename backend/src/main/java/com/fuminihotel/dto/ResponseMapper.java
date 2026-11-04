/**
 * Author: HoangTLT
 */
package com.fuminihotel.dto;

import com.fuminihotel.entity.*;

// Return DTOs instead of entities: hide passwords and avoid recursive JPA JSON.
public final class ResponseMapper {
    private ResponseMapper() {}

    public static CustomerResponse customer(Customer c) {
        return new CustomerResponse(c.getCustomerId(), c.getCustomerFullName(), c.getTelephone(),
                c.getEmailAddress(), c.getCustomerBirthday(), c.getCustomerStatus());
    }

    public static RoomResponse room(RoomInformation r) {
        return new RoomResponse(r.getRoomId(), r.getRoomNumber(), r.getRoomDetailDescription(),
                r.getRoomMaxCapacity(), r.getRoomType().getRoomTypeId(), r.getRoomType().getRoomTypeName(),
                r.getRoomStatus(), r.getRoomPricePerDay());
    }

    public static BookingResponse booking(BookingReservation b) {
        return new BookingResponse(b.getBookingReservationId(), b.getBookingDate(), b.getTotalPrice(),
                b.getCustomer().getCustomerId(), b.getCustomer().getCustomerFullName(), b.getBookingStatus(),
                b.getBookingDetails().stream().map(d -> new BookingResponse.Detail(
                        d.getRoomInformation().getRoomId(), d.getRoomInformation().getRoomNumber(),
                        d.getStartDate(), d.getEndDate(), d.getActualPrice())).toList());
    }
}
