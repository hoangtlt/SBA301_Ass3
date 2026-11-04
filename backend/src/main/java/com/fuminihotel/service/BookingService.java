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
            throw new ApiException(NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y Ä‘Æ¡n Ä‘áº·t phÃ²ng");
        }
        return ResponseMapper.booking(booking);
    }

    public BookingResponse create(BookingRequest request, Long authenticatedCustomerId) {
        // Customer ID always comes from JWT for customer requests.
        Long customerId = authenticatedCustomerId != null ? authenticatedCustomerId : request.customerId();
        if (customerId == null)
            throw new ApiException(BAD_REQUEST, "Cáº§n chá»n khÃ¡ch hÃ ng");
        Customer customer = customers.findEntity(customerId);
        if (customer.getCustomerStatus() != 1)
            throw new ApiException(BAD_REQUEST, "KhÃ¡ch hÃ ng Ä‘Ã£ ngá»«ng hoáº¡t Ä‘á»™ng");

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
            throw new ApiException(CONFLICT, "Chá»‰ sá»­a Ä‘Æ°á»£c Ä‘Æ¡n Ä‘ang xÃ¡c nháº­n");
        }
        if (request.customerId() == null)
            throw new ApiException(BAD_REQUEST, "Cáº§n chá»n khÃ¡ch hÃ ng");
        Customer customer = customers.findEntity(request.customerId());
        if (customer.getCustomerStatus() != 1)
            throw new ApiException(BAD_REQUEST, "KhÃ¡ch hÃ ng Ä‘Ã£ ngá»«ng hoáº¡t Ä‘á»™ng");
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
                throw new ApiException(BAD_REQUEST, "KhÃ´ng chá»n cÃ¹ng má»™t phÃ²ng hai láº§n");
            if (item.startDate().isBefore(LocalDate.now()) || !item.endDate().isAfter(item.startDate())) {
                throw new ApiException(BAD_REQUEST, "NgÃ y tráº£ pháº£i sau ngÃ y nháº­n; ngÃ y nháº­n khÃ´ng Ä‘Æ°á»£c trong quÃ¡ khá»©");
            }
            RoomInformation room = rooms.findByIdForUpdate(item.roomId())
                    .orElseThrow(() -> new ApiException(NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y phÃ²ng"));
            if (room.getRoomStatus() != 1)
                throw new ApiException(CONFLICT, "PhÃ²ng " + room.getRoomNumber() + " Ä‘ang ngá»«ng hoáº¡t Ä‘á»™ng");
            if (details.countOverlapping(room.getRoomId(), item.startDate(), item.endDate(), BookingStatus.CANCELLED,
                    booking.getBookingReservationId()) > 0) {
                throw new ApiException(CONFLICT,
                        "PhÃ²ng " + room.getRoomNumber() + " Ä‘Ã£ Ä‘Æ°á»£c Ä‘áº·t trong khoáº£ng ngÃ y nÃ y");
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
            throw new ApiException(CONFLICT, "Chá»‰ cÃ³ thá»ƒ hoÃ n táº¥t hoáº·c há»§y Ä‘Æ¡n Ä‘ang xÃ¡c nháº­n");
        }
        if (status == BookingStatus.COMPLETED && booking.getBookingDetails().stream()
                .anyMatch(d -> d.getEndDate().isAfter(LocalDate.now()))) {
            throw new ApiException(BAD_REQUEST, "ChÆ°a Ä‘áº¿n ngÃ y tráº£ phÃ²ng nÃªn chÆ°a thá»ƒ hoÃ n táº¥t");
        }
        booking.setBookingStatus(status);
        return ResponseMapper.booking(booking);
    }

    public void delete(Long id) {
        BookingReservation booking = findForUpdate(id);
        if (booking.getBookingStatus() != BookingStatus.CANCELLED) {
            throw new ApiException(CONFLICT, "Cáº§n há»§y Ä‘Æ¡n trÆ°á»›c khi xÃ³a");
        }
        bookings.delete(booking);
    }

    private BookingReservation find(Long id) {
        return bookings.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y Ä‘Æ¡n Ä‘áº·t phÃ²ng"));
    }

    private BookingReservation findForUpdate(Long id) {
        return bookings.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y Ä‘Æ¡n Ä‘áº·t phÃ²ng"));
    }
}
