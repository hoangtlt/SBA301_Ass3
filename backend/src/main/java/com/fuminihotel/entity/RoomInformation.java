/**
 * Author: HoangTLT
 */
package com.fuminihotel.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room_information")
public class RoomInformation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @Column(nullable = false, unique = true, length = 20)
    @Nationalized
    private String roomNumber;

    @Column(length = 500)
    @Nationalized
    private String roomDetailDescription;

    @Column(nullable = false)
    private Integer roomMaxCapacity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    @Column(nullable = false)
    private Integer roomStatus;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal roomPricePerDay;

    public RoomInformation() {}

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomDetailDescription() { return roomDetailDescription; }
    public void setRoomDetailDescription(String roomDetailDescription) { this.roomDetailDescription = roomDetailDescription; }

    public Integer getRoomMaxCapacity() { return roomMaxCapacity; }
    public void setRoomMaxCapacity(Integer roomMaxCapacity) { this.roomMaxCapacity = roomMaxCapacity; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public Integer getRoomStatus() { return roomStatus; }
    public void setRoomStatus(Integer roomStatus) { this.roomStatus = roomStatus; }

    public BigDecimal getRoomPricePerDay() { return roomPricePerDay; }
    public void setRoomPricePerDay(BigDecimal roomPricePerDay) { this.roomPricePerDay = roomPricePerDay; }
}

