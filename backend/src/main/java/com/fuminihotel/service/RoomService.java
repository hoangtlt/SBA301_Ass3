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
            throw new ApiException(BAD_REQUEST, "Cần nhập cả ngày nhận và trả phòng");
        if (startDate != null && (startDate.isBefore(LocalDate.now()) || !endDate.isAfter(startDate))) {
            throw new ApiException(BAD_REQUEST, "Ngày trả phải sau ngày nhận và ngày nhận không được trong quá khứ");
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
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Không tìm thấy phòng đang hoạt động"));
        return ResponseMapper.room(room);
    }

    @Transactional(readOnly = true)
    public List<RoomType> getRoomTypes() {
        return roomTypes.findAll();
    }

    public RoomResponse save(Long id, RoomRequest request) {
        RoomInformation room = id == null ? new RoomInformation()
                : rooms.findByIdForUpdate(id)
                        .orElseThrow(() -> new ApiException(NOT_FOUND, "Không tìm thấy phòng"));
        String roomNumber = request.roomNumber().trim();
        if ((id == null || !room.getRoomNumber().equalsIgnoreCase(roomNumber))
                && rooms.existsByRoomNumberIgnoreCase(roomNumber)) {
            throw new ApiException(CONFLICT, "Số phòng đã tồn tại");
        }
        room.setRoomNumber(roomNumber);
        room.setRoomDetailDescription(request.roomDetailDescription());
        room.setRoomMaxCapacity(request.roomMaxCapacity());
        room.setRoomType(roomTypes.findById(request.roomTypeId())
                .orElseThrow(() -> new ApiException(BAD_REQUEST, "Loại phòng không tồn tại")));
        room.setRoomStatus(request.roomStatus());
        room.setRoomPricePerDay(request.roomPricePerDay());
        return ResponseMapper.room(rooms.save(room));
    }

    public String delete(Long id) {
        RoomInformation room = rooms.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Không tìm thấy phòng"));
        if (details.existsByRoomInformationRoomId(id)) {
            room.setRoomStatus(0);
            return "Phòng đã có lịch sử đặt: đã chuyển sang ngừng hoạt động";
        }
        rooms.delete(room);
        return "Đã xóa phòng chưa có lịch sử đặt";
    }
}
