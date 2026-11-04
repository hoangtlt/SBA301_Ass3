/**
 * Author: HoangTLT
 */
package com.fuminihotel.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(@NotBlank @Email @Size(max = 100) String email,
                           @NotBlank @Size(max = 72) String password) {}
