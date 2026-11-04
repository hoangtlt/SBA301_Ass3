/**
 * Author: HoangTLT
 */
package com.fuminihotel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record BookingRequest(@Positive Long customerId,
                             @NotEmpty @Size(max = 20) List<@NotNull @Valid BookingRoomRequest> rooms) {}
