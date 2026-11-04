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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
public class BookingService {
    private final BookingRepository bookings;
    private final BookingDetailRepository details;
    private final RoomRepository rooms;
    private final CustomerService customers;

    public BookingService(BookingRepository bookings, BookingDetailRepository details,
            RoomRepository rooms, CustomerService customers) {
        this.bookings = bookings;
        this.details = details;
        this.rooms = rooms;
        this.customers = customers;
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> list(Long customerId) {
        List<BookingReservation> result = customerId == null ? bookings.findAllByOrderByBookingReservationIdDesc()
                : bookings.findByCustomerCustomerIdOrderByBookingReservationIdDesc(customerId);
        return result.stream().map(ResponseMapper::booking).toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse get(Long id, Long ownerId) {
        BookingReservation booking = find(id);
        if (ownerId != null && !booking.getCustomer().getCustomerId().equals(ownerId)) {
            throw new ApiException(NOT_FOUND, "Không tìm thấy đơn đặt phòng");
        }
        return ResponseMapper.booking(booking);
    }

    public BookingResponse create(BookingRequest request, Long authenticatedCustomerId) {
        // Customer ID always comes from JWT for customer requests.
        Long customerId = authenticatedCustomerId != null ? authenticatedCustomerId : request.customerId();
        if (customerId == null)
            throw new ApiException(BAD_REQUEST, "Cần chọn khách hàng");
        Customer customer = customers.findEntity(customerId);
        if (customer.getCustomerStatus() != 1)
            throw new ApiException(BAD_REQUEST, "Khách hàng đã ngừng hoạt động");

        BookingReservation booking = new BookingReservation();
        booking.setBookingDate(LocalDate.now());
        booking.setCustomer(customer);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        applyRooms(booking, request);
        return ResponseMapper.booking(bookings.save(booking));
    }

    public BookingResponse update(Long id, BookingRequest request) {
        BookingReservation booking = findForUpdate(id);
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new ApiException(CONFLICT, "Chỉ sửa được đơn đang xác nhận");
        }
        if (request.customerId() == null)
            throw new ApiException(BAD_REQUEST, "Cần chọn khách hàng");
        Customer customer = customers.findEntity(request.customerId());
        if (customer.getCustomerStatus() != 1)
            throw new ApiException(BAD_REQUEST, "Khách hàng đã ngừng hoạt động");
        booking.setCustomer(customer);
        applyRooms(booking, request);
        return ResponseMapper.booking(booking);
    }

    private void applyRooms(BookingReservation booking, BookingRequest request) {
        BigDecimal total = BigDecimal.ZERO;
        Set<Long> selectedIds = new HashSet<>();
        Map<Long, BookingDetail> previousDetails = new HashMap<>();
        booking.getBookingDetails().forEach(d -> previousDetails.put(d.getRoomInformation().getRoomId(), d));

        // Always acquire room locks in ascending ID order to reduce deadlocks.
        List<BookingRoomRequest> selected = request.rooms().stream()
                .sorted(Comparator.comparing(BookingRoomRequest::roomId)).toList();
        for (BookingRoomRequest item : selected) {
            if (!selectedIds.add(item.roomId()))
                throw new ApiException(BAD_REQUEST, "Không chọn cùng một phòng hai lần");
            if (item.startDate().isBefore(LocalDate.now()) || !item.endDate().isAfter(item.startDate())) {
                throw new ApiException(BAD_REQUEST, "Ngày trả phải sau ngày nhận; ngày nhận không được trong quá khứ");
            }
            RoomInformation room = rooms.findByIdForUpdate(item.roomId())
                    .orElseThrow(() -> new ApiException(NOT_FOUND, "Không tìm thấy phòng"));
            if (room.getRoomStatus() != 1)
                throw new ApiException(CONFLICT, "Phòng " + room.getRoomNumber() + " đang ngừng hoạt động");
            if (details.countOverlapping(room.getRoomId(), item.startDate(), item.endDate(), BookingStatus.CANCELLED,
                    booking.getBookingReservationId()) > 0) {
                throw new ApiException(CONFLICT,
                        "Phòng " + room.getRoomNumber() + " đã được đặt trong khoảng ngày này");
            }
            BookingDetail detail = previousDetails.get(room.getRoomId());
            if (detail == null) {
                detail = new BookingDetail();
                detail.setBookingReservation(booking);
                detail.setRoomInformation(room);
                detail.setActualPrice(room.getRoomPricePerDay());
                booking.getBookingDetails().add(detail);
            }
            detail.setStartDate(item.startDate());
            detail.setEndDate(item.endDate());
            // ActualPrice stores the daily rate at booking time; later room price changes
            // do not alter history.
            total = total.add(detail.getActualPrice()
                    .multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(item.startDate(), item.endDate()))));
        }
        booking.getBookingDetails().removeIf(d -> !selectedIds.contains(d.getRoomInformation().getRoomId()));
        booking.setTotalPrice(total);
    }

    public BookingResponse changeStatus(Long id, BookingStatus status) {
        BookingReservation booking = findForUpdate(id);
        if (booking.getBookingStatus() == status)
            return ResponseMapper.booking(booking);
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED || status == BookingStatus.CONFIRMED) {
            throw new ApiException(CONFLICT, "Chỉ có thể hoàn tất hoặc hủy đơn đang xác nhận");
        }
        if (status == BookingStatus.COMPLETED && booking.getBookingDetails().stream()
                .anyMatch(d -> d.getEndDate().isAfter(LocalDate.now()))) {
            throw new ApiException(BAD_REQUEST, "Chưa đến ngày trả phòng nên chưa thể hoàn tất");
        }
        booking.setBookingStatus(status);
        return ResponseMapper.booking(booking);
    }

    public void delete(Long id) {
        BookingReservation booking = findForUpdate(id);
        if (booking.getBookingStatus() != BookingStatus.CANCELLED) {
            throw new ApiException(CONFLICT, "Cần hủy đơn trước khi xóa");
        }
        bookings.delete(booking);
    }

    private BookingReservation find(Long id) {
        return bookings.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Không tìm thấy đơn đặt phòng"));
    }

    private BookingReservation findForUpdate(Long id) {
        return bookings.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Không tìm thấy đơn đặt phòng"));
    }
}
