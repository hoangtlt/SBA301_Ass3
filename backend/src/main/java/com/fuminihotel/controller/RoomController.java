/**
 * Author: HoangTLT
 */
package com.fuminihotel.controller;

import com.fuminihotel.dto.RoomResponse;
import com.fuminihotel.entity.RoomType;
import com.fuminihotel.service.RoomService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RoomController {
    private final RoomService rooms;

    public RoomController(RoomService rooms) { this.rooms = rooms; }

    @GetMapping("/rooms")
    public List<RoomResponse> list(@RequestParam(defaultValue = "") String keyword,
                                  @RequestParam(required = false) LocalDate startDate,
                                  @RequestParam(required = false) LocalDate endDate) {
        return rooms.search(keyword, false, startDate, endDate);
    }

    @GetMapping("/rooms/{id}")
    public RoomResponse get(@PathVariable Long id) { return rooms.get(id); }

    @GetMapping("/room-types")
    public List<RoomType> types() { return rooms.getRoomTypes(); }
}
