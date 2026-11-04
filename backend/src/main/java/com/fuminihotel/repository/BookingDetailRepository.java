/**
 * Author: HoangTLT
 */
package com.fuminihotel.repository;

import com.fuminihotel.entity.BookingDetail;
import com.fuminihotel.entity.BookingDetailId;
import com.fuminihotel.entity.BookingStatus;
import org.springframework.data.jpa.repository.*;
import java.time.LocalDate;

public interface BookingDetailRepository extends JpaRepository<BookingDetail, BookingDetailId> {
    boolean existsByRoomInformationRoomId(Long roomId);

    @Query("""
        select count(d) from BookingDetail d
        where d.roomInformation.roomId = :roomId
          and d.bookingReservation.bookingStatus <> :cancelled
          and d.startDate < :endDate and d.endDate > :startDate
          and (:excludedId is null or d.bookingReservation.bookingReservationId <> :excludedId)
        """)
    long countOverlapping(Long roomId, LocalDate startDate, LocalDate endDate,
                          BookingStatus cancelled, Long excludedId);
}
