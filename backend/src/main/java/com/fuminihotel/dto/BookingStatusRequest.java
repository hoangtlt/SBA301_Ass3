/**
 * Author: HoangTLT
 */
package com.fuminihotel.dto;

import com.fuminihotel.entity.BookingStatus;
import jakarta.validation.constraints.NotNull;

public record BookingStatusRequest(@NotNull BookingStatus bookingStatus) {}
