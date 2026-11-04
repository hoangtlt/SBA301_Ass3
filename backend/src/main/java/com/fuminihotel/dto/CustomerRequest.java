/**
 * Author: HoangTLT
 */
package com.fuminihotel.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CustomerRequest(
        @NotBlank @Size(max = 100) String customerFullName,
        @NotBlank @Pattern(regexp = "[0-9+() -]{8,20}") String telephone,
        @NotBlank @Email @Size(max = 100) String emailAddress,
        @NotNull @Past LocalDate customerBirthday,
        @NotNull @Min(0) @Max(1) Integer customerStatus,
        // Blank means keep the current password when updating.
        @Size(max = 72) String password) {}
