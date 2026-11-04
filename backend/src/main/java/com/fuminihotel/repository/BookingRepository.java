/**
 * Author: HoangTLT
 */
package com.fuminihotel.repository;

import com.fuminihotel.entity.BookingReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;

public interface BookingRepository extends JpaRepository<BookingReservation, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BookingReservation b where b.bookingReservationId = :id")
    Optional<BookingReservation> findByIdForUpdate(Long id);
    List<BookingReservation> findAllByOrderByBookingReservationIdDesc();
    List<BookingReservation> findByCustomerCustomerIdOrderByBookingReservationIdDesc(Long customerId);
    boolean existsByCustomerCustomerId(Long customerId);
}
