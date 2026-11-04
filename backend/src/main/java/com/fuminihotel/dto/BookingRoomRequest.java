/**
 * Author: HoangTLT
 */
package com.fuminihotel.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record BookingRoomRequest(@NotNull @Positive Long roomId,
                                 @NotNull @FutureOrPresent LocalDate startDate,
                                 @NotNull @Future LocalDate endDate) {}
