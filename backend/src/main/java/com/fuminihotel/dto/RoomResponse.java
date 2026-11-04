/**
 * Author: HoangTLT
 */
package com.fuminihotel.dto;

import java.math.BigDecimal;

public record RoomResponse(Long roomId, String roomNumber, String roomDetailDescription,
                           Integer roomMaxCapacity, Long roomTypeId, String roomTypeName,
                           Integer roomStatus, BigDecimal roomPricePerDay) {}
