package com.moonlight.project.airBnbApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomPriceDto {
    private Long roomId;
    private BigDecimal pricePerNight;  // average dynamic price per night
    private BigDecimal totalForStay;    // total for the full stay (roomsCount applied)
}
