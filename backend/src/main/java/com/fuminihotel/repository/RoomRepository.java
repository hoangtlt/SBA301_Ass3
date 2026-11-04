/**
 * Author: HoangTLT
 */
package com.fuminihotel.repository;

import com.fuminihotel.entity.RoomInformation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;
import java.util.List;

public interface RoomRepository extends JpaRepository<RoomInformation, Long> {
    List<RoomInformation> findByRoomNumberContainingIgnoreCaseOrderByRoomNumberAsc(String keyword);
    boolean existsByRoomNumberIgnoreCase(String roomNumber);

    // Lock the room until the transaction ends to prevent concurrent double booking.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RoomInformation r where r.roomId = :id")
    Optional<RoomInformation> findByIdForUpdate(Long id);
}
