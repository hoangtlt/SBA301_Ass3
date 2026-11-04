/**
 * Author: HoangTLT
 */
package com.fuminihotel.dto;

import java.time.LocalDate;

public record CustomerResponse(Long customerId, String customerFullName, String telephone,
                               String emailAddress, LocalDate customerBirthday, Integer customerStatus) {}
