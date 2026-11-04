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
@Table(name = "room_type")
public class RoomType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomTypeId;

    @Column(nullable = false, length = 50)
    @Nationalized
    private String roomTypeName;

    @Column(length = 250)
    @Nationalized
    private String typeDescription;

    @Column(length = 250)
    @Nationalized
    private String typeNote;

    public RoomType() {}

    public Long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(Long roomTypeId) { this.roomTypeId = roomTypeId; }

    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }

    public String getTypeDescription() { return typeDescription; }
    public void setTypeDescription(String typeDescription) { this.typeDescription = typeDescription; }

    public String getTypeNote() { return typeNote; }
    public void setTypeNote(String typeNote) { this.typeNote = typeNote; }
}

