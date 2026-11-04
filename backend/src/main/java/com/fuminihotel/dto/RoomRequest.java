/**
 * Author: HoangTLT
 */
package com.fuminihotel.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record RoomRequest(
        @NotBlank @Size(max = 20) String roomNumber,
        @Size(max = 500) String roomDetailDescription,
        @NotNull @Min(1) @Max(100) Integer roomMaxCapacity,
        @NotNull @Positive Long roomTypeId,
        @NotNull @Min(0) @Max(1) Integer roomStatus,
        @NotNull @DecimalMin("0.01") @Digits(integer = 16, fraction = 2) BigDecimal roomPricePerDay) {}
