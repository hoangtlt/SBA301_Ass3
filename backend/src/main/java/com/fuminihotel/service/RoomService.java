/**
 * Author: HoangTLT
 */
package com.fuminihotel.service;

import com.fuminihotel.dto.*;
import com.fuminihotel.entity.*;
import com.fuminihotel.exception.ApiException;
import com.fuminihotel.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
public class RoomService {
    private final RoomRepository rooms;
    private final RoomTypeRepository roomTypes;
    private final BookingDetailRepository details;

    public RoomService(RoomRepository rooms, RoomTypeRepository roomTypes, BookingDetailRepository details) {
        this.rooms = rooms;
        this.roomTypes = roomTypes;
        this.details = details;
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> search(String keyword, boolean staff, LocalDate startDate, LocalDate endDate) {
        if ((startDate == null) != (endDate == null))
            throw new ApiException(BAD_REQUEST, "Cáº§n nháº­p cáº£ ngÃ y nháº­n vÃ  tráº£ phÃ²ng");
        if (startDate != null && (startDate.isBefore(LocalDate.now()) || !endDate.isAfter(startDate))) {
            throw new ApiException(BAD_REQUEST, "NgÃ y tráº£ pháº£i sau ngÃ y nháº­n vÃ  ngÃ y nháº­n khÃ´ng Ä‘Æ°á»£c trong quÃ¡ khá»©");
        }
        return rooms.findByRoomNumberContainingIgnoreCaseOrderByRoomNumberAsc(keyword).stream()
                .filter(r -> staff || r.getRoomStatus() == 1)
                .filter(r -> startDate == null || details.countOverlapping(r.getRoomId(), startDate, endDate,
                        BookingStatus.CANCELLED, null) == 0)
                .map(ResponseMapper::room).toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse get(Long id) {
        RoomInformation room = rooms.findById(id).filter(r -> r.getRoomStatus() == 1)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y phÃ²ng Ä‘ang hoáº¡t Ä‘á»™ng"));
        return ResponseMapper.room(room);
    }

    @Transactional(readOnly = true)
    public List<RoomType> getRoomTypes() {
        return roomTypes.findAll();
    }

    public RoomResponse save(Long id, RoomRequest request) {
        RoomInformation room = id == null ? new RoomInformation()
                : rooms.findByIdForUpdate(id)
                        .orElseThrow(() -> new ApiException(NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y phÃ²ng"));
        String roomNumber = request.roomNumber().trim();
        if ((id == null || !room.getRoomNumber().equalsIgnoreCase(roomNumber))
                && rooms.existsByRoomNumberIgnoreCase(roomNumber)) {
            throw new ApiException(CONFLICT, "Sá»‘ phÃ²ng Ä‘Ã£ tá»“n táº¡i");
        }
        room.setRoomNumber(roomNumber);
        room.setRoomDetailDescription(request.roomDetailDescription());
        room.setRoomMaxCapacity(request.roomMaxCapacity());
        room.setRoomType(roomTypes.findById(request.roomTypeId())
                .orElseThrow(() -> new ApiException(BAD_REQUEST, "Loáº¡i phÃ²ng khÃ´ng tá»“n táº¡i")));
        room.setRoomStatus(request.roomStatus());
        room.setRoomPricePerDay(request.roomPricePerDay());
        return ResponseMapper.room(rooms.save(room));
    }

    public String delete(Long id) {
        RoomInformation room = rooms.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y phÃ²ng"));
        if (details.existsByRoomInformationRoomId(id)) {
            room.setRoomStatus(0);
            return "PhÃ²ng Ä‘Ã£ cÃ³ lá»‹ch sá»­ Ä‘áº·t: Ä‘Ã£ chuyá»ƒn sang ngá»«ng hoáº¡t Ä‘á»™ng";
        }
        rooms.delete(room);
        return "ÄÃ£ xÃ³a phÃ²ng chÆ°a cÃ³ lá»‹ch sá»­ Ä‘áº·t";
    }
}
