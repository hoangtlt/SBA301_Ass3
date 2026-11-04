/**
 * Author: HoangTLT
 */
package com.fuminihotel.dto;

public record AuthResponse(String token, String role, Long customerId, String fullName) {}
